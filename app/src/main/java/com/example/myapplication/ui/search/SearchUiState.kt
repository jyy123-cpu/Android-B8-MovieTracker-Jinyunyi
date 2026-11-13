package com.example.myapplication.ui.search

/**
 * 搜索页界面状态，界面据此渲染不同内容。
 */
sealed interface SearchUiState {

    /** 尚未发起搜索：提示用户输入关键词 */
    data object Idle : SearchUiState

    /** 搜索请求进行中 */
    data object Loading : SearchUiState

    /** 搜索成功但没有匹配结果 */
    data object Empty : SearchUiState

    /** 搜索失败，message 用于提示原因 */
    data class Error(val message: String) : SearchUiState

    /** 搜索成功且有结果，具体列表由 ViewModel 单独持有 */
    data object Success : SearchUiState
}
