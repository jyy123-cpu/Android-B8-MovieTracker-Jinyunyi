package com.example.myapplication.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.local.entity.WatchStatus
import com.example.myapplication.data.remote.dto.MovieDetailDto
import com.example.myapplication.data.remote.dto.toEntity
import com.example.myapplication.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 详情页 ViewModel：加载电影详情、监听本地看单记录，并处理加入看单、
 * 切换状态、我的评分等操作。
 */
class DetailViewModel(
    private val movieId: Int,
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    /** 加载详情，重试时也调用 */
    fun loadDetail() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            repository.getMovieDetail(movieId)
                .onSuccess { detail ->
                    // 再请求一次中文详情，只取剧情简介；失败或无翻译时为 null，不影响主流程
                    val overviewZh = repository.getMovieDetail(movieId, LANGUAGE_ZH)
                        .getOrNull()
                        ?.overview
                        ?.ifBlank { null }
                    // 详情拿到后，持续监听本地看单记录；加入/改状态/打分都会自动刷新界面
                    repository.observeMovieById(movieId).collect { savedMovie ->
                        _uiState.value = DetailUiState.Success(
                            detail = detail,
                            overviewZh = overviewZh,
                            savedMovie = savedMovie
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = DetailUiState.Error(
                        throwable.message ?: "详情加载失败，请稍后重试"
                    )
                }
        }
    }

    /** 把电影加入看单，初始状态为"想看" */
    fun addToWatchlist(detail: MovieDetailDto) {
        viewModelScope.launch {
            val entity = detail.toEntity(
                status = WatchStatus.PLAN,
                addedAt = System.currentTimeMillis()
            )
            repository.addToWatchlist(entity)
        }
    }

    /** 切换看单状态（想看 / 在看 / 看过） */
    fun changeStatus(status: WatchStatus) {
        viewModelScope.launch {
            repository.updateStatus(movieId, status)
        }
    }

    /** 保存我的评分，评分范围 0-10 */
    fun rate(rating: Float) {
        viewModelScope.launch {
            repository.updateUserRating(movieId, rating)
        }
    }

    companion object {
        /** TMDb 简体中文语言代码 */
        private const val LANGUAGE_ZH = "zh-CN"

        /** 手动注入电影 id 和仓库的工厂 */
        fun provideFactory(
            movieId: Int,
            repository: MovieRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { DetailViewModel(movieId, repository) }
        }
    }
}
