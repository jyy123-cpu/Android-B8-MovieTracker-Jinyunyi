package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.data.local.entity.WatchStatus
import kotlinx.coroutines.flow.Flow

/**
 * 电影看单数据访问对象，集中定义所有数据库操作。
 * 查询返回 Flow，数据变化时界面会自动收到新结果。
 */
@Dao
interface MovieDao {

    // 插入或替换：同一 id 已存在时覆盖更新
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(movie: MovieEntity)

    @Delete
    suspend fun delete(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteById(movieId: Int)

    @Query("SELECT * FROM movies WHERE id = :movieId")
    fun observeMovieById(movieId: Int): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    // 按看单状态查询，加入时间倒序
    @Query("SELECT * FROM movies WHERE status = :status ORDER BY addedAt DESC")
    fun observeByStatus(status: WatchStatus): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<MovieEntity>>

    @Query("UPDATE movies SET status = :status WHERE id = :movieId")
    suspend fun updateStatus(movieId: Int, status: WatchStatus)

    @Query("UPDATE movies SET userRating = :rating WHERE id = :movieId")
    suspend fun updateUserRating(movieId: Int, rating: Float)

    @Query("UPDATE movies SET reminderEnabled = :enabled, reminderTime = :time WHERE id = :movieId")
    suspend fun updateReminder(movieId: Int, enabled: Boolean, time: Long?)

    // 统计：各状态数量
    @Query("SELECT COUNT(*) FROM movies WHERE status = :status")
    fun countByStatus(status: WatchStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM movies")
    fun countAll(): Flow<Int>
}
