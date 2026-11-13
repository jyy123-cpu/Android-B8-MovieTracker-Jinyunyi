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
        notificationId: Int,
        title: String,
        content: String
    ) {
        val notification = buildReleaseNotification(context, title, content)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    /**
     * 构建通知。通知内的操作按钮在后续「通知内直接操作」阶段再追加。
     */
    private fun buildReleaseNotification(
        context: Context,
        title: String,
        content: String
    ): Notification {
        // 点击通知打开主界面
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            // M（API 23）起必须显式声明可变/不可变，这里只打开界面，用不可变
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationChannels.RELEASE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            // 长文本展开样式，避免内容被截断
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // 点击后自动消失
            .setContentIntent(contentPendingIntent)
            .build()
    }
}
