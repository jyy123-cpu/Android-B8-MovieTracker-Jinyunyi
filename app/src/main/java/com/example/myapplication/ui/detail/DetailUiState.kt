package com.example.myapplication.ui.detail

import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.data.remote.dto.MovieDetailDto

/**
 * 详情页界面状态。
 */
sealed interface DetailUiState {

    /** 加载中：显示转圈 */
    data object Loading : DetailUiState

    /** 加载失败：显示错误信息和重试 */
    data class Error(val message: String) : DetailUiState

    /**
     * 加载成功。
     * @param detail 网络拿到的电影详情（英文原文）
     * @param overviewZh 中文剧情简介；没有翻译或请求失败时为 null
     * @param savedMovie 本地看单记录；为 null 表示还没加入看单
     */
    data class Success(
        val detail: MovieDetailDto,
        val overviewZh: String?,
        val savedMovie: MovieEntity?
    ) : DetailUiState
}
