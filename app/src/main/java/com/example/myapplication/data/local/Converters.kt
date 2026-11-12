package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.data.local.entity.WatchStatus

/**
 * Room 类型转换器：
 * 数据库本身只认识基本类型，枚举需要在 String 和枚举之间转换。
 */
class Converters {

    @TypeConverter
    fun fromWatchStatus(status: WatchStatus): String = status.name

    @TypeConverter
    fun toWatchStatus(value: String): WatchStatus = WatchStatus.valueOf(value)
}
