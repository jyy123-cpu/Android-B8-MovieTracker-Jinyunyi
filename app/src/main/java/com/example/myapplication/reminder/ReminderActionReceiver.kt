package com.example.myapplication.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.util.Log
import com.example.myapplication.MovieTrackerApplication
import com.example.myapplication.data.local.entity.WatchStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 通知内直接操作的接收器。
 * 用户在通知上点「标记看过 / 取消提醒」时，无需打开 App，
 * 这里直接改看单数据，满足伴生模块「通知内直接操作」的要求。
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive 进入，action=${intent.action}")

        val movieId = intent.getIntExtra(EXTRA_MOVIE_ID, -1)
        if (movieId == -1) {
            Log.d(TAG, "缺少 movieId，直接返回")
            return
        }

        val repository =
            (context.applicationContext as MovieTrackerApplication).container.movieRepository
        val notificationManager =
            context.getSystemService(NotificationManager::class.java)

        // 广播默认生命周期很短，goAsync 给一个异步窗口，让我们能完成数据库操作
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "协程开始，movieId=$movieId，action=${intent.action}")
                when (intent.action) {
                    ACTION_MARK_WATCHED -> {
                        // 直接把状态改成看过
                        repository.updateStatus(movieId, WatchStatus.WATCHED)
                        Log.d(TAG, "已更新为看过")
                    }

                    ACTION_CANCEL_REMINDER -> {
                        // 取消后台任务并清掉提醒标记
                        ReminderScheduler.cancel(context, movieId)
                        repository.updateReminder(movieId, enabled = false, time = null)
                        Log.d(TAG, "已取消提醒")
                    }
                }
                // 操作完成后移除这条通知
                notificationManager.cancel(movieId)
            } catch (e: Exception) {
                // 显式捕获，避免异常被协程静默吞掉
                Log.e(TAG, "处理通知操作失败", e)
            } finally {
                // 必须调用，告诉系统异步处理结束
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "ReminderAction"
        const val ACTION_MARK_WATCHED = "com.example.myapplication.action.MARK_WATCHED"
        const val ACTION_CANCEL_REMINDER = "com.example.myapplication.action.CANCEL_REMINDER"
        const val EXTRA_MOVIE_ID = "extra_movie_id"

        /**
         * 构建通知按钮用的 PendingIntent。
         * @param requestCode 不同按钮用不同值，进一步避免 PendingIntent 互相覆盖
         */
        private fun buildPendingIntent(
            context: Context,
            action: String,
            movieId: Int,
            requestCode: Int
        ): PendingIntent {
            val intent = Intent(context, ReminderActionReceiver::class.java).apply {
                this.action = action
                putExtra(EXTRA_MOVIE_ID, movieId)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun markWatchedPendingIntent(context: Context, movieId: Int): PendingIntent =
            buildPendingIntent(context, ACTION_MARK_WATCHED, movieId, requestCode = movieId)

        fun cancelReminderPendingIntent(context: Context, movieId: Int): PendingIntent =
            buildPendingIntent(context, ACTION_CANCEL_REMINDER, movieId, requestCode = movieId + 1)
    }
}
