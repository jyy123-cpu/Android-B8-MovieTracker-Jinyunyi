package com.example.myapplication.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 看单页 ViewModel：监听 Room 全部看单，结合当前筛选和排序，
 * 自动计算要展示的列表和顶部统计。
 */
class WatchlistViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    // 数据库里的全部看单电影
    private val allMovies = MutableStateFlow<List<MovieEntity>>(emptyList())

    // 当前筛选与排序，界面点击会修改这两个值
    private val filter = MutableStateFlow(StatusFilter.ALL)
    private val sortMode = MutableStateFlow(SortMode.COMPOSITE)

    /**
     * 把三份数据组合起来：任意一份变化都会重新计算界面状态。
     */
    val uiState: StateFlow<WatchlistUiState> = combine(
        allMovies, filter, sortMode
    ) { movies, currentFilter, currentSort ->
        // 1. 按状态筛选
        val targetStatus = currentFilter.toWatchStatus()
        val filtered = if (targetStatus == null) {
            movies
        } else {
            movies.filter { it.status == targetStatus }
        }

        // 2. 按所选方式排序
        val sorted = when (currentSort) {
            SortMode.COMPOSITE ->
                filtered.sortedByDescending { Scoring.compositeScore(it) }
            SortMode.ADDED ->
                filtered.sortedByDescending { it.addedAt }
            SortMode.USER_RATING ->
                filtered.sortedByDescending { it.userRating ?: -1f }
        }

        // 3. 统计：平均我的评分（只算已评分的）和平均综合推荐指数
        val ratedValues = movies.mapNotNull { it.userRating }
        val averageUserRating = if (ratedValues.isEmpty()) null else ratedValues.average()
        val averageComposite = if (movies.isEmpty()) {
            0.0
        } else {
            movies.map { Scoring.compositeScore(it) }.average()
        }

        WatchlistUiState.Success(
            allMovies = movies,
            filteredMovies = sorted,
            filter = currentFilter,
            sortMode = currentSort,
            averageUserRating = averageUserRating,
            averageComposite = averageComposite
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WatchlistUiState.Loading
    )

    init {
        // 订阅数据库，看单有增删改时 allMovies 自动更新
        viewModelScope.launch {
            repository.observeAll().collect { allMovies.value = it }
        }
    }

    /** 切换状态筛选 */
    fun setFilter(statusFilter: StatusFilter) {
        filter.value = statusFilter
    }

    /** 切换排序方式 */
    fun setSortMode(mode: SortMode) {
        sortMode.value = mode
    }

    /** 从看单移除 */
    fun remove(movieId: Int) {
        viewModelScope.launch {
            repository.removeFromWatchlist(movieId)
        }
    }

    companion object {
        /** 手动注入仓库的工厂 */
        fun provideFactory(repository: MovieRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { WatchlistViewModel(repository) }
            }
    }
}
