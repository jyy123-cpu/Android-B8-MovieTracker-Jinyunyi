package com.example.myapplication.data.local

import com.example.myapplication.data.local.entity.WatchStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Room 类型转换器 [Converters] 的本地单元测试。
 * 转换逻辑只用到枚举的 name/valueOf，是纯 JVM 操作。
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `枚举转字符串_返回枚举名`() {
        assertEquals("PLAN", converters.fromWatchStatus(WatchStatus.PLAN))
        assertEquals("WATCHING", converters.fromWatchStatus(WatchStatus.WATCHING))
        assertEquals("WATCHED", converters.fromWatchStatus(WatchStatus.WATCHED))
    }

    @Test
    fun `字符串还原枚举_得到对应状态`() {
        assertEquals(WatchStatus.PLAN, converters.toWatchStatus("PLAN"))
        assertEquals(WatchStatus.WATCHING, converters.toWatchStatus("WATCHING"))
        assertEquals(WatchStatus.WATCHED, converters.toWatchStatus("WATCHED"))
    }

    @Test
    fun `三个状态往返转换_保持一致`() {
        WatchStatus.values().forEach { status ->
            val stored = converters.fromWatchStatus(status)
            val restored = converters.toWatchStatus(stored)
            assertEquals(status, restored)
        }
    }
}
