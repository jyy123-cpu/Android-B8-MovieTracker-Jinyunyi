package com.example.myapplication.ui.discover

/**
 * 发现页界面状态，界面据此渲染加载中 / 成功 / 空数据 / 错误四种情况。
 */
sealed interface DiscoverUiState {

    /** 首次加载中 */
    data object Loading : DiscoverUiState

    /** 加载成功且有数据 */
    data object Success : DiscoverUiState

    /** 加载成功但列表为空 */
    data object Empty : DiscoverUiState

    /** 加载失败，message 用于提示原因 */
    data class Error(val message: String) : DiscoverUiState
}
