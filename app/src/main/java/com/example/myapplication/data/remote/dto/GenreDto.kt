package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 电影类型，详情接口中返回完整对象（id + 名称）。
 */
data class GenreDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = ""
)
