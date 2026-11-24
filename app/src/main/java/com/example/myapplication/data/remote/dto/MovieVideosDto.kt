package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * TMDb「电影视频」接口（/movie/{id}/videos）的返回结构。
 * TMDb 本身不托管视频文件，只返回 YouTube 等平台的视频标识（key）。
 */
data class MovieVideosDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("results")
    val results: List<VideoDto> = emptyList()
)

/**
 * 单条视频信息。
 * key 是 YouTube 视频 id，拼成 https://www.youtube.com/watch?v=key 即可打开。
 */
data class VideoDto(
    @SerializedName("key")
    val key: String = "",

    @SerializedName("site")
    val site: String = "",

    @SerializedName("type")
    val type: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("official")
    val official: Boolean = false
)
