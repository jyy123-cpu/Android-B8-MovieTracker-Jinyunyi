package com.example.myapplication

import android.app.Application
import com.example.myapplication.core.AppContainer

/**
 * 应用入口类。
 * 初始化 AppContainer，界面可通过 (application as MovieTrackerApplication).container 访问仓库。
 */
class MovieTrackerApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
