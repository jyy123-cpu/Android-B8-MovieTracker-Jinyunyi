package com.example.myapplication.ui.watchlist

import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.data.local.entity.WatchStatus

/** 看单状态筛选方式 */
enum class StatusFilter(val label: String) {
    ALL("全部"),
    PLAN("想看"),
    WATCHING("在看"),
    WATCHED("看过")
}

/** 列表排序方式 */
enum class SortMode(val label: String) {
    COMPOSITE("综合推荐"),
    ADDED("最近加入"),
    USER_RATING("我的评分")
}

/** 看单页界面状态 */
sealed interface WatchlistUiState {

    /** 首次加载（数据库 Flow 首次返回前） */
    data object Loading : WatchlistUiState

    /**
     * 数据就绪。
     * @param allMovies 看单里的全部电影，用于顶部统计
     * @param filteredMovies 经过筛选和排序、当前要展示的列表
     * @param filter 当前状态筛选
     * @param sortMode 当前排序方式
     * @param averageUserRating 已评分电影的平均我的评分，没有评分时为 null
     * @param averageComposite 全部电影的平均综合推荐指数
     */
    data class Success(
        val allMovies: List<MovieEntity>,
        val filteredMovies: List<MovieEntity>,
        val filter: StatusFilter,
        val sortMode: SortMode,
        val averageUserRating: Double?,
        val averageComposite: Double
    ) : WatchlistUiState
}

/** 把筛选枚举映射到 Room 的看单状态，ALL 对应 null */
fun StatusFilter.toWatchStatus(): WatchStatus? = when (this) {
    StatusFilter.ALL -> null
    StatusFilter.PLAN -> WatchStatus.PLAN
    StatusFilter.WATCHING -> WatchStatus.WATCHING
    StatusFilter.WATCHED -> WatchStatus.WATCHED
}
