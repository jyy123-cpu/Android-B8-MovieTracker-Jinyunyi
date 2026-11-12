package com.example.myapplication.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * TMDb 电影详情接口的数据结构。
 */
data class MovieDetailDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("poster_path")
    val posterPath: String? = null,

    @SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @SerializedName("overview")
    val overview: String? = null,

    @SerializedName("release_date")
    val releaseDate: String? = null,

    @SerializedName("vote_average")
    val voteAverage: Double = 0.0,

    @SerializedName("popularity")
    val popularity: Double = 0.0,

    @SerializedName("genres")
    val genres: List<GenreDto> = emptyList(),

    @SerializedName("runtime")
    val runtime: Int? = null
)
