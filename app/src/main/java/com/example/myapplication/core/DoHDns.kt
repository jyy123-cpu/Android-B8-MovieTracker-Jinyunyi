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
 * 自定义 DNS 解析器：DoH 加密解析 + 可信段白名单 + 内置 IP 兜底。
 *
 * 背景演进：
 * 1. 系统明文 DNS 把 api.themoviedb.org 解析到假地址，于是改用 DoH；
 * 2. 污染加剧后，国内 DoH 上游也返回假 IP，且假地址来自不断变化的多个段：
 *    Facebook（157.240.x）、Dropbox（108.160.x）、域名停放（199.59.x）、
 *    Twitter（104.244.x）等，靠「黑名单」逐个排除永远追不全；
 * 3. 而 TMDb 真实 CloudFront 节点（108.138.246.x）经实测稳定可连、返回 HTTP 200。
 *
 * 因此改用「白名单」思路：对 TMDb 域名，DoH 结果只有落在可信段
 * （108.138.246.x）才采用，否则直接用内置的可信 IP 列表兜底。
 * 白名单天然排除一切未知的污染段，比黑名单可靠。
 */
class DoHDns : Dns {

    private companion object {
        // TMDb 接口域名，需要做可信段校验与 IP 兜底
        const val TMDB_API_HOST = "api.themoviedb.org"

        // 已实测可用的真实 CloudFront IP（均返回 HTTP 200），按优先级排列
        val TRUSTED_TMDB_IPS = listOf(
            "108.138.246.55",
            "108.138.246.56",
            "108.138.246.125",
            "108.138.246.57"
        )

        // 可信 CloudFront 段前缀，DoH 结果只有落在这个段才被接受
        val TRUSTED_PREFIX = listOf(108, 138, 246)
    }

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

        if (hostname == TMDB_API_HOST) {
            // TMDb 域名：DoH 只接受可信段结果，拿不到就用内置 IP 兜底
            for (endpoint in dohEndpoints) {
                try {
                    val candidates = queryViaDoH(endpoint, hostname)
                    val trusted = candidates.filter { isInTrustedRange(it) }
                    if (trusted.isNotEmpty()) {
                        return trusted
                    }
                } catch (e: Exception) {
                    // 当前端点出错，忽略后继续尝试下一个
                }
            }
            // DoH 没有可信结果，用内置可信 IP 兜底，保证演示稳定
            return buildTrustedAddresses(hostname)
        }

        // 其他域名：优先用 DoH 的任意结果，全部失败再回退系统默认解析
        for (endpoint in dohEndpoints) {
            try {
                val result = queryViaDoH(endpoint, hostname)
                if (result.isNotEmpty()) {
                    return result
                }
            } catch (e: Exception) {
                // 出错继续尝试下一个端点
            }
        }
        return try {
            Dns.SYSTEM.lookup(hostname)
        } catch (e: UnknownHostException) {
            throw e
        }
    }

    /**
     * 判断地址是否落在可信 CloudFront 段（108.138.246.x）。
     */
    private fun isInTrustedRange(address: InetAddress): Boolean {
        val bytes = address.address
        if (bytes.size != 4) return false // 只处理 IPv4
        val parts = bytes.map { it.toInt() and 0xFF }
        // 可信前缀的每一段都要匹配
        return TRUSTED_PREFIX.indices.all { index -> parts[index] == TRUSTED_PREFIX[index] }
    }

    /**
     * 把内置可信 IP 字符串转成带原始主机名的 InetAddress 列表，
     * 保留主机名是为了 TLS 证书校验和 SNI 正常工作。
     */
    private fun buildTrustedAddresses(hostname: String): List<InetAddress> =
        TRUSTED_TMDB_IPS.map { ip ->
            InetAddress.getByAddress(hostname, InetAddress.getByName(ip).address)
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
