package com.example.myapplication.data.remote.dto

import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.data.local.entity.WatchStatus

/**
 * 数据映射扩展：把网络 DTO 转成本地 Room 实体。
 * 加入看单时调用，新增记录默认无评分、无提醒。
 */

/** 详情 DTO 转实体，能拿到完整类型名称 */
fun MovieDetailDto.toEntity(
    status: WatchStatus,
    addedAt: Long
): MovieEntity = MovieEntity(
    id = id,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    popularity = popularity,
    status = status,
    userRating = null,
    genres = genres.joinToString(separator = ",") { it.name },
    addedAt = addedAt,
    reminderEnabled = false,
    reminderTime = null
)

/** 列表 DTO 转实体（列表接口只返回类型 id，没有类型名称，genres 留空） */
fun MovieDto.toEntity(
    status: WatchStatus,
    addedAt: Long
): MovieEntity = MovieEntity(
    id = id,
    title = title,
    posterPath = posterPath,
    backdropPath = backdropPath,
    overview = overview,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    popularity = popularity,
    status = status,
    userRating = null,
    genres = null,
    addedAt = addedAt,
    reminderEnabled = false,
    reminderTime = null
)
