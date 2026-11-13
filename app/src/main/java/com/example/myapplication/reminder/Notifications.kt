package com.example.myapplication.reminder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

/**
 * 通知构建与发送工具。
 * 用 NotificationCompat 兼容不同系统版本，渠道 id 统一从 [NotificationChannels] 取。
 */
object Notifications {

    /**
     * 弹出一条上映提醒通知。
     * @param notificationId 通知 id，用电影 id 保证同一部电影不会重复堆叠
     */
    fun showReleaseNotification(
        context: Context,
        movieId: Int,
        title: String,
        content: String
    ) {
        // 通知 id 直接用电影 id，同一部电影始终只有一条通知，天然防堆叠
        val notification = buildReleaseNotification(context, movieId, title, content)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(movieId, notification)
    }

    /**
     * 构建通知，包含点击打开 App 和两个通知内直接操作按钮。
     */
    private fun buildReleaseNotification(
        context: Context,
        movieId: Int,
        title: String,
        content: String
    ): Notification {
        // 点击通知主体打开主界面
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationChannels.RELEASE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            // 通知内直接操作：不打开 App 就能标记看过或取消提醒
            .addAction(
                R.drawable.ic_notification,
                "标记看过",
                ReminderActionReceiver.markWatchedPendingIntent(context, movieId)
            )
            .addAction(
                R.drawable.ic_notification,
                "取消提醒",
                ReminderActionReceiver.cancelReminderPendingIntent(context, movieId)
            )
            .build()
    }
}
