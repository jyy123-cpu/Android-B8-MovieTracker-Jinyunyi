package com.example.myapplication.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * 提醒调度器：界面只跟它打交道，不直接操作 WorkManager。
 * 用「唯一工作」保证同一部电影重复设置时，旧任务被替换、不会叠加。
 */
object ReminderScheduler {

    private const val WORK_NAME_PREFIX = "release_reminder_"

    /**
     * 安排一次上映提醒。
     * @param movieId 电影 id
     * @param triggerAtMillis 期望触发的时间戳
     */
    fun schedule(context: Context, movieId: Int, triggerAtMillis: Long) {
        // 距触发还剩多久；若时间已过，取 0 让任务尽快执行
        val delayMillis = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)

        // 把电影 id 放进任务输入数据
        val inputData = workDataOf(
            ReleaseReminderWorker.KEY_MOVIE_ID to movieId
        )

        val request = OneTimeWorkRequestBuilder<ReleaseReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        // REPLACE：同一部电影已有提醒时，用新任务替换旧的
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(movieId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** 取消某部电影的提醒 */
    fun cancel(context: Context, movieId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(movieId))
    }

    /** 每部电影对应的唯一工作名 */
    private fun workName(movieId: Int): String = "$WORK_NAME_PREFIX$movieId"
}
