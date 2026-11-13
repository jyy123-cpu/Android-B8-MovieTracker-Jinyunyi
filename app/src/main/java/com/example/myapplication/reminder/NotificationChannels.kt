package com.example.myapplication.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * 通知渠道集中管理。
 * Android 8.0（API 26）起，所有通知必须归属某个渠道，
 * 渠道创建后用户可以在系统设置里单独控制它的开关和提醒方式。
 */
object NotificationChannels {

    /** 上映提醒渠道 id，发通知时要带上同一个 id */
    const val RELEASE_CHANNEL_ID = "release_reminder"

    private const val RELEASE_CHANNEL_NAME = "上映提醒"
    private const val RELEASE_CHANNEL_DESC = "影片上映或临近上映时弹出的提醒"

    /**
     * 在应用启动时调用，创建渠道。重复创建不会覆盖用户已改的设置，是安全的。
     */
    fun createChannels(context: Context) {
        // 低版本系统没有渠道概念，直接跳过
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            RELEASE_CHANNEL_ID,
            RELEASE_CHANNEL_NAME,
            // HIGH 重要性：会弹窗、有声音，适合提醒类通知
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = RELEASE_CHANNEL_DESC
            enableVibration(true)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
