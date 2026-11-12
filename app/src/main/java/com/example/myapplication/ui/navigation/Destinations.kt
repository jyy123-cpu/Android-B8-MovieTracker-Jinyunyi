package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 路由常量集中管理。
 */
object Routes {
    const val DISCOVER = "discover"
    const val SEARCH = "search"
    const val WATCHLIST = "watchlist"
    const val REMINDER = "reminder"

    // 详情页带电影 id 参数
    const val DETAIL_ARG_MOVIE_ID = "movieId"
    const val DETAIL = "detail/{$DETAIL_ARG_MOVIE_ID}"
    fun detail(movieId: Int): String = "detail/$movieId"
}

/**
 * 底部导航栏的一级入口。
 */
sealed class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Discover : BottomItem(Routes.DISCOVER, "发现", Icons.Filled.Home)
    data object Search : BottomItem(Routes.SEARCH, "搜索", Icons.Filled.Search)
    data object Watchlist : BottomItem(Routes.WATCHLIST, "看单", Icons.Filled.Bookmark)
    data object Reminder : BottomItem(Routes.REMINDER, "提醒", Icons.Filled.Notifications)
}

val bottomItems = listOf(
    BottomItem.Discover,
    BottomItem.Search,
    BottomItem.Watchlist,
    BottomItem.Reminder
)
