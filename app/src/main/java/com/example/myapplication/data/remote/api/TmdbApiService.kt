package com.example.myapplication.data.remote.api

import com.example.myapplication.data.remote.dto.MovieDetailDto
import com.example.myapplication.data.remote.dto.MovieResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TMDb 网络接口，Retrofit 根据注解自动生成实现。
 * api_key 由 OkHttp 拦截器统一添加，这里不用每个方法都写。
 */
interface TmdbApiService {

    /** 热门电影分页列表 */
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): MovieResponseDto

    /** 按关键词搜索电影 */
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): MovieResponseDto

    /**
     * 电影详情。
     * @param language 语言代码，如 zh-CN 取中文；为 null 时使用 TMDb 默认英文
     */
    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String? = null
    ): MovieDetailDto
}
