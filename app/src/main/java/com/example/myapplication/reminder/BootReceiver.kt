package com.example.myapplication.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myapplication.MovieTrackerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 开机重启接收器。
 * WorkManager 本身会持久化任务，这里再加一层保险：
 * 开机后把看单里仍有效的提醒重新注册一遍，并清理时间已过的无效提醒。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 正式只认系统开机广播；额外保留一个自定义 action，方便在不重启设备的情况下验证逻辑
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            intent.action != ACTION_TEST_REBOOT
        ) {
            return
        }

        Log.d(TAG, "onReceive 进入，action=${intent.action}")

        val repository =
            (context.applicationContext as MovieTrackerApplication).container.movieRepository
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // first()：只取一次当前看单数据，不需要持续监听
                val movies = repository.observeAll().first()
                val now = System.currentTimeMillis()

                val enabled = movies.filter { it.reminderEnabled }
                Log.d(TAG, "看单共 ${movies.size} 部，其中待重调度 ${enabled.size} 部")

                enabled.forEach { movie ->
                    val triggerTime = movie.reminderTime
                    if (triggerTime != null && triggerTime > now) {
                        // 提醒时间还在未来，重新调度
                        ReminderScheduler.schedule(context, movie.id, triggerTime)
                        Log.d(TAG, "已重新调度 movieId=${movie.id}，触发时间=$triggerTime")
                    } else {
                        // 时间已过或缺失，清掉提醒标记，避免留下无效任务
                        repository.updateReminder(movie.id, enabled = false, time = null)
                        Log.d(TAG, "提醒已过期，清理 movieId=${movie.id}")
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"

        // 仅用于本地验证开机重调度逻辑的自定义 action
        const val ACTION_TEST_REBOOT = "com.example.myapplication.action.TEST_REBOOT"
    }
}
