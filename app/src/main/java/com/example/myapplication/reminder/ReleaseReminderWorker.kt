package com.example.myapplication.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.MovieTrackerApplication

/**
 * 上映提醒后台任务。
 *
 * WorkManager 会在约定时间（即使 App 没在前台）启动它，
 * 这里用 CoroutineWorker，doWork 是挂起函数，可以直接调用数据库。
 */
class ReleaseReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        /** 输入数据里电影 id 的键名，调度方通过它告诉 Worker 提醒哪部电影 */
        const val KEY_MOVIE_ID = "movie_id"
    }

    override suspend fun doWork(): Result {
        // 1. 取出要提醒的电影 id，缺省则视为无效任务
        val movieId = inputData.getInt(KEY_MOVIE_ID, -1)
        if (movieId == -1) {
            return Result.failure()
        }

        // 2. 从应用容器拿仓库，查出电影信息
        val repository =
            (applicationContext as MovieTrackerApplication).container.movieRepository
        val movie = repository.getMovieById(movieId)
            // 电影已被移除看单，就没有提醒的必要了，正常结束即可
            ?: return Result.success()

        // 3. 弹出通知；通知 id 用电影 id，保证同一部电影不重复堆叠
        Notifications.showReleaseNotification(
            context = applicationContext,
            notificationId = movie.id,
            title = movie.title,
            content = "《${movie.title}》即将上映，点通知查看详情并安排观影"
        )

        return Result.success()
    }
}
