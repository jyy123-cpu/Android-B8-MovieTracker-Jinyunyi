package com.example.myapplication.data.repository

import com.example.myapplication.core.safeApiCall
import com.example.myapplication.data.local.dao.MovieDao
import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.data.local.entity.WatchStatus
import com.example.myapplication.data.remote.api.TmdbApiService
import com.example.myapplication.data.remote.dto.MovieDetailDto
import com.example.myapplication.data.remote.dto.MovieResponseDto
import com.example.myapplication.data.remote.dto.TrailerPicker
import kotlinx.coroutines.flow.Flow

/**
 * 仓库层：界面只和仓库打交道，不直接关心数据来自网络还是数据库。
 */
class MovieRepository(
    private val apiService: TmdbApiService,
    private val movieDao: MovieDao
) {
    // ---------- 网络数据 ----------

    suspend fun getPopularMovies(page: Int): Result<MovieResponseDto> = safeApiCall {
        apiService.getPopularMovies(page)
    }

    suspend fun searchMovies(query: String, page: Int): Result<MovieResponseDto> = safeApiCall {
        apiService.searchMovies(query, page)
    }

    suspend fun getMovieDetail(
        movieId: Int,
        language: String? = null
    ): Result<MovieDetailDto> = safeApiCall {
        apiService.getMovieDetail(movieId, language)
    }

    /**
     * 取预告片的 YouTube key；没有可用预告片或请求失败时返回 null。
     * 失败不抛出、不阻塞详情页，界面上只表现为不显示「看预告」按钮。
     */
    suspend fun getTrailerKey(movieId: Int): String? =
        safeApiCall { apiService.getMovieVideos(movieId) }
            .getOrNull()
            ?.let { TrailerPicker.pick(it.results) }

    // ---------- 本地看单 ----------

    fun observeByStatus(status: WatchStatus): Flow<List<MovieEntity>> =
        movieDao.observeByStatus(status)

    fun observeAll(): Flow<List<MovieEntity>> = movieDao.observeAll()

    fun observeMovieById(movieId: Int): Flow<MovieEntity?> =
        movieDao.observeMovieById(movieId)

    fun countByStatus(status: WatchStatus): Flow<Int> = movieDao.countByStatus(status)

    fun countAll(): Flow<Int> = movieDao.countAll()

    suspend fun getMovieById(movieId: Int): MovieEntity? = movieDao.getMovieById(movieId)

    suspend fun addToWatchlist(movie: MovieEntity) = movieDao.upsert(movie)

    suspend fun removeFromWatchlist(movieId: Int) = movieDao.deleteById(movieId)

    suspend fun updateStatus(movieId: Int, status: WatchStatus) =
        movieDao.updateStatus(movieId, status)

    suspend fun updateUserRating(movieId: Int, rating: Float) =
        movieDao.updateUserRating(movieId, rating)

    suspend fun updateReminder(movieId: Int, enabled: Boolean, time: Long?) =
        movieDao.updateReminder(movieId, enabled, time)
}
