package com.example.myapplication

import android.app.Application

/**
 * 应用入口类。
 * 后续在这里初始化 AppContainer（数据库、网络、仓库的手动依赖容器）。
 */
class MovieTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 依赖容器将在数据层开发阶段初始化
    }
}
