package com.example.myapplication.core

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.remote.NetworkModule
import com.example.myapplication.data.repository.MovieRepository

/**
 * 手动依赖容器：
 * 在这里统一创建数据库、网络和仓库，界面通过它获取仓库，避免到处直接 new。
 */
class AppContainer(context: Context) {

    private val appDatabase = AppDatabase.getInstance(context.applicationContext)
    private val movieDao = appDatabase.movieDao()
    private val apiService = NetworkModule.apiService

    val movieRepository: MovieRepository = MovieRepository(
        apiService = apiService,
        movieDao = movieDao
    )
}
