package com.example.myapplication.data.remote

import com.example.myapplication.BuildConfig
import com.example.myapplication.core.DoHDns
import com.example.myapplication.data.remote.api.TmdbApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络模块单例：集中创建 OkHttp 与 Retrofit。
 */
object NetworkModule {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    val apiService: TmdbApiService by lazy {
        // 认证拦截器：给每个请求自动补上 api_key 参数
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newUrl = originalRequest.url.newBuilder()
                .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                .build()
            chain.proceed(originalRequest.newBuilder().url(newUrl).build())
        }

        // 日志拦截器：调试时在 Logcat 查看请求和响应内容
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .dns(DoHDns()) // 用 DoH 加密解析，绕过系统 DNS 污染
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }
}
