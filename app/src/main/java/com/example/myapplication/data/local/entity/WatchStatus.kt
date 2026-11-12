package com.example.myapplication.data.local.entity

/**
 * 看单状态枚举。
 * Room 通过 Converters 把它按枚举名（name）存入数据库。
 */
enum class WatchStatus(val label: String) {
    PLAN("想看"),
    WATCHING("在看"),
    WATCHED("看过")
}
