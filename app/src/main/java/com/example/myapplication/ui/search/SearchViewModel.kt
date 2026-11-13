package com.example.myapplication.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.repository.MovieRepository
import com.example.myapplication.data.remote.dto.MovieDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 搜索页 ViewModel：管理关键词、防抖搜索、结果列表、分页和界面状态。
 */
class SearchViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    // 搜索框当前文本
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // 当前搜索结果
    private val _movies = MutableStateFlow<List<MovieDto>>(emptyList())
    val movies: StateFlow<List<MovieDto>> = _movies.asStateFlow()

    // 界面状态
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // 防抖搜索任务，输入变化时取消旧的、启动新的
    private var searchJob: Job? = null

    // 分页相关
    private var isLoadingMore = false
    private var currentPage = 1
    private var totalPages = 1

    // 已提交搜索的关键词，分页加载和重试都基于它
    private var submittedQuery = ""

    /**
     * 输入框内容变化：更新文本并安排一次 500ms 后的防抖搜索，
     * 避免用户每敲一个字就发一次请求。
     */
    fun onQueryChange(value: String) {
        _query.value = value
        searchJob?.cancel()

        if (value.isBlank()) {
            // 清空输入：回到初始状态，清掉旧结果
            _movies.value = emptyList()
            submittedQuery = ""
            _uiState.value = SearchUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            doSearch(value.trim())
        }
    }

    /** 点击键盘上的搜索按钮时立即搜索，不再等待防抖 */
    fun search() {
        val keyword = _query.value.trim()
        if (keyword.isEmpty()) return
        searchJob?.cancel()
        viewModelScope.launch { doSearch(keyword) }
    }

    /** 错误态点击重试：用最近一次的关键词重新搜索 */
    fun retry() {
        if (submittedQuery.isBlank()) return
        viewModelScope.launch { doSearch(submittedQuery) }
    }

    /**
     * 真正发起搜索请求并更新界面状态。
     */
    private suspend fun doSearch(keyword: String) {
        _uiState.value = SearchUiState.Loading
        repository.searchMovies(query = keyword, page = 1)
            .onSuccess { response ->
                submittedQuery = keyword
                _movies.value = response.results
                currentPage = 1
                totalPages = response.totalPages
                _uiState.value = if (response.results.isEmpty()) {
                    SearchUiState.Empty
                } else {
                    SearchUiState.Success
                }
            }
            .onFailure { throwable ->
                _uiState.value = SearchUiState.Error(
                    throwable.message ?: "搜索失败，请稍后重试"
                )
            }
    }

    /** 滚动接近底部时加载下一页 */
    fun loadMore() {
        if (isLoadingMore || currentPage >= totalPages || submittedQuery.isEmpty()) return
        isLoadingMore = true
        viewModelScope.launch {
            repository.searchMovies(query = submittedQuery, page = currentPage + 1)
                .onSuccess { response ->
                    _movies.value = _movies.value + response.results
                    currentPage += 1
                    totalPages = response.totalPages
                }
            // 失败静默处理，不影响已加载的结果
            isLoadingMore = false
        }
    }

    companion object {
        /** 手动注入仓库的工厂 */
        fun provideFactory(repository: MovieRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { SearchViewModel(repository) }
            }
    }
}
