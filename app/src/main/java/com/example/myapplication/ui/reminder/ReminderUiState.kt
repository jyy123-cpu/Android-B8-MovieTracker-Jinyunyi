package com.example.myapplication.ui.reminder

import com.example.myapplication.data.local.entity.MovieEntity

/**
 * 提醒中心界面状态。
 * 这里用一个数据类承载即可：首次加载用 loading 区分，空列表时界面显示空态。
 */
data class ReminderUiState(
    val loading: Boolean = true,
    val movies: List<MovieEntity> = emptyList()
)
