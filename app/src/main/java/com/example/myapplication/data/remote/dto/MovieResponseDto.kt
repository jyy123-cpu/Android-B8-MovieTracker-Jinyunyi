package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * TMDb 分页列表响应的外层结构。
 */
data class MovieResponseDto(
    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("results")
    val results: List<MovieDto> = emptyList(),

    @SerializedName("total_pages")
    val totalPages: Int = 1,

    @SerializedName("total_results")
    val totalResults: Int = 0
)
