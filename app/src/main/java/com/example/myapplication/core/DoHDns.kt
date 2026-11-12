package com.example.myapplication.core

import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONObject
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * 自定义 DNS 解析器：优先走 DoH（DNS over HTTPS，加密 DNS）。
 *
 * 背景：系统默认的 DNS 查询是明文传输的，在当前网络环境下，
 * api.themoviedb.org 会被解析到一个无法连接的假地址（DNS 污染），
 * 于是 App 报 "Unable to resolve host" 或连接超时。
 * DoH 把 DNS 查询包进 HTTPS 加密通道里，网络中间设备无法篡改，
 * 因此能拿到真实的 CloudFront 节点 IP。
 *
 * 这里选用国内可稳定访问的两个 DoH 服务商，按顺序尝试：
 * 腾讯 doh.pub 优先，阿里 dns.alidns.com 备用。
 *
 * 关键设计：
 * 1. 发 DoH 请求用的 [dohClient] 不嵌套自定义 DNS，走系统解析；
 *    而 DoH 服务器是国内域名、系统能正常解析，避免自己调自己的死循环。
 * 2. 任何一步失败都回退到系统默认解析 [Dns.SYSTEM]，
 *    保证在网络正常的环境下 App 行为不受影响。
 */
class DoHDns : Dns {

    // 专门用来发 DoH 请求的客户端：超时设短一些，失败好快速切换下一个
    private val dohClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // DoH 服务端点，均返回标准的 DNS-JSON 格式
    private val dohEndpoints: List<String> = listOf(
        "https://doh.pub/dns-query",
        "https://dns.alidns.com/resolve"
    )

    /**
     * OkHttp 解析域名时回调本方法，返回该域名对应的 IP 地址列表。
     */
    override fun lookup(hostname: String): List<InetAddress> {
        // 本机回环、DoH 服务器自身直接交给系统解析，避免递归调用
        if (hostname == "localhost" ||
            hostname.endsWith("doh.pub") ||
            hostname.endsWith("alidns.com")
        ) {
            return Dns.SYSTEM.lookup(hostname)
        }

        // 依次尝试各 DoH 端点，谁先拿到有效结果就用谁
        for (endpoint in dohEndpoints) {
            try {
                val addresses = queryViaDoH(endpoint, hostname)
                if (addresses.isNotEmpty()) {
                    return addresses
                }
            } catch (e: Exception) {
                // 当前端点出错（网络异常、JSON 异常等），忽略后继续尝试下一个
            }
        }

        // 所有 DoH 都失败，回退到系统默认解析，保持基本可用性
        return try {
            Dns.SYSTEM.lookup(hostname)
        } catch (e: UnknownHostException) {
            throw e
        }
    }

    /**
     * 向指定 DoH 端点查询 [hostname] 的 IPv4 地址（A 记录，type=1）。
     * 返回空列表表示该端点没有给出可用结果。
     */
    private fun queryViaDoH(endpoint: String, hostname: String): List<InetAddress> {
        // 拼出形如 https://doh.pub/dns-query?name=xxx&type=A 的查询地址
        val url = endpoint.toHttpUrl().newBuilder()
            .addQueryParameter("name", hostname)
            .addQueryParameter("type", "A")
            .build()

        val request = Request.Builder()
            .url(url)
            .header("accept", "application/dns-json") // 声明要 JSON 格式结果
            .get()
            .build()

        // use 会在请求结束后自动关闭响应体
        dohClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return emptyList()
            }
            val body = response.body?.string() ?: return emptyList()

            val json = JSONObject(body)
            val answers = json.optJSONArray("Answer") ?: return emptyList()

            val ipList = mutableListOf<String>()
            for (i in 0 until answers.length()) {
                val record = answers.getJSONObject(i)
                // type=1 是 A 记录（IPv4）；type=5 是 CNAME，这里跳过
                if (record.optInt("type") == 1) {
                    ipList.add(record.getString("data"))
                }
            }

            // 把 IP 字符串转成 InetAddress，并保留原始主机名，便于证书/SNI 校验
            return ipList.map { ip ->
                InetAddress.getByAddress(hostname, InetAddress.getByName(ip).address)
            }
        }
    }
}
