package com.example.myapplication.ui.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.repository.MovieRepository
import com.example.myapplication.reminder.Notifications
import com.example.myapplication.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * 提醒中心 ViewModel：监听看单电影，处理提醒的开关、取消和手动测试。
 */
class ReminderViewModel(
    private val repository: MovieRepository,
    // 调度器需要 Context，这里保存应用级 Context，不会持有 Activity，避免泄漏
    private val appContext: Context
) : ViewModel() {

    // 把仓库的看单数据映射成界面状态
    val uiState = repository.observeAll()
        .map { movies -> ReminderUiState(loading = false, movies = movies) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReminderUiState(loading = true)
        )

    /**
     * 切换某部电影的提醒开关。
     */
    fun toggleReminder(movieId: Int) {
        viewModelScope.launch {
            val movie = repository.getMovieById(movieId) ?: return@launch

            if (movie.reminderEnabled) {
                // 已开 → 关闭：取消后台任务并清掉数据库里的提醒信息
                ReminderScheduler.cancel(appContext, movie.id)
                repository.updateReminder(movie.id, enabled = false, time = null)
            } else {
                // 未开 → 开启：算出触发时间并调度
                val triggerTime = resolveTriggerTime(movie.releaseDate)
                ReminderScheduler.schedule(appContext, movie.id, triggerTime)
                repository.updateReminder(movie.id, enabled = true, time = triggerTime)
            }
        }
    }

    /**
     * 立即发一条测试通知，用来验证通知权限和渠道是否正常，不经过 WorkManager。
     */
    fun sendTestNotification(movieId: Int) {
        viewModelScope.launch {
            val movie = repository.getMovieById(movieId) ?: return@launch
            Notifications.showReleaseNotification(
                context = appContext,
                // 通知 id 统一用电影 id，和正式提醒是同一条，不会出现 id 错位
                movieId = movie.id,
                title = movie.title,
                content = "这是一条手动测试通知，用于确认提醒功能正常"
            )
        }
    }

    /**
     * 计算提醒触发时间：
     * 上映日期在未来就定在上映当天上午 9 点；已上映或日期无效，则 10 秒后触发，方便演示。
     */
    private fun resolveTriggerTime(releaseDate: String?): Long {
        val releaseTime = parseReleaseDay(releaseDate)
        val now = System.currentTimeMillis()
        return if (releaseTime != null && releaseTime > now) {
            releaseTime
        } else {
            now + QUICK_TEST_DELAY_MS
        }
    }

    /** 把 yyyy-MM-dd 解析成当天上午 9 点的时间戳，解析失败返回 null */
    private fun parseReleaseDay(releaseDate: String?): Long? {
        if (releaseDate.isNullOrBlank()) return null
        return try {
            LocalDate.parse(releaseDate) // 严格按 yyyy-MM-dd 解析
                .atTime(9, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        // 已上映影片的快速演示延迟：10 秒
        private const val QUICK_TEST_DELAY_MS = 10_000L

        /** 手动注入仓库和应用 Context 的工厂 */
        fun provideFactory(
            repository: MovieRepository,
            appContext: Context
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ReminderViewModel(repository, appContext) }
        }
    }
}
