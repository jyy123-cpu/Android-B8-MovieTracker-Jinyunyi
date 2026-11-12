package com.example.myapplication.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.repository.MovieRepository
import com.example.myapplication.data.remote.dto.MovieDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 发现页 ViewModel：负责热门电影的加载、分页和状态管理。
 */
class DiscoverViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _movies = MutableStateFlow<List<MovieDto>>(emptyList())
    val movies: StateFlow<List<MovieDto>> = _movies.asStateFlow()

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    // 分页加载更多时的标记，避免重复请求
    private var isLoadingMore = false
    private var currentPage = 1
    private var totalPages = 1

    init {
        loadFirstPage()
    }

    /** 加载第一页，下拉重试时也调用它 */
    fun loadFirstPage() {
        _uiState.value = DiscoverUiState.Loading
        viewModelScope.launch {
            repository.getPopularMovies(page = 1)
                .onSuccess { response ->
                    _movies.value = response.results
                    currentPage = 1
                    totalPages = response.totalPages
                    _uiState.value = if (response.results.isEmpty()) {
                        DiscoverUiState.Empty
                    } else {
                        DiscoverUiState.Success
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = DiscoverUiState.Error(
                        throwable.message ?: "网络加载失败，请稍后重试"
                    )
                }
        }
    }

    /** 滚动接近底部时加载下一页 */
    fun loadMore() {
        if (isLoadingMore || currentPage >= totalPages) return
        isLoadingMore = true
        viewModelScope.launch {
            val nextPage = currentPage + 1
            repository.getPopularMovies(page = nextPage)
                .onSuccess { response ->
                    _movies.value = _movies.value + response.results
                    currentPage = nextPage
                    totalPages = response.totalPages
                }
            // 失败时静默处理，不影响已加载的数据
            isLoadingMore = false
        }
    }

    companion object {
        /** 手动注入仓库的工厂方法 */
        fun provideFactory(repository: MovieRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { DiscoverViewModel(repository) }
            }
    }
}
