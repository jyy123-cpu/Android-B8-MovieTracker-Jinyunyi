package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 电影看单实体，一条记录对应一部被加入看单的电影。
 * 主键直接使用 TMDb 的电影 id，保证同一部电影不会重复入库。
 */
@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: Int,                    // TMDb 电影 id
    val title: String,              // 电影标题
    val posterPath: String?,        // 海报相对路径，需拼接 TMDb 图片域名
    val backdropPath: String?,      // 详情页背景大图相对路径
    val overview: String?,          // 剧情简介
    val releaseDate: String?,       // 上映日期，格式 yyyy-MM-dd
    val voteAverage: Double,        // TMDb 官方评分，0-10
    val popularity: Double,         // TMDb 热度值，用于期待值排序
    val status: WatchStatus,        // 看单状态：想看 / 在看 / 看过
    val userRating: Float?,         // 我的评分 0-10，未评分为 null
    val genres: String?,            // 类型名，逗号分隔，用于类型分布统计
    val addedAt: Long,              // 加入看单的时间戳
    val reminderEnabled: Boolean,   // 是否设置了上映提醒
    val reminderTime: Long?         // 提醒触发时间戳，未设置为 null
)
