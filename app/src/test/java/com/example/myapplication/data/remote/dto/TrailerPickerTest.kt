package com.example.myapplication.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 预告片挑选逻辑 [TrailerPicker] 的本地单元测试。
 * 纯 JVM 计算，不依赖网络和 Android 框架。
 */
class TrailerPickerTest {

    /** 构造一条视频记录，默认是 YouTube 上的普通预告片 */
    private fun video(
        key: String = "abc",
        site: String = "YouTube",
        type: String = "Trailer",
        official: Boolean = false
    ): VideoDto = VideoDto(
        key = key,
        site = site,
        type = type,
        name = "测试视频",
        official = official
    )

    @Test
    fun `列表为空_返回null`() {
        assertNull(TrailerPicker.pick(emptyList()))
    }

    @Test
    fun `只有非YouTube站点_返回null`() {
        val videos = listOf(video(key = "v1", site = "Vimeo", type = "Trailer"))
        assertNull(TrailerPicker.pick(videos))
    }

    @Test
    fun `key为空或空白_该条被忽略`() {
        val videos = listOf(
            video(key = "", type = "Trailer", official = true),
            video(key = "   ", type = "Trailer", official = true),
            video(key = "real", type = "Trailer")
        )
        // 前两条 key 无效，只能选到第三条
        assertEquals("real", TrailerPicker.pick(videos))
    }

    @Test
    fun `官方预告片优先于普通预告片`() {
        val videos = listOf(
            video(key = "normal", type = "Trailer", official = false),
            video(key = "official", type = "Trailer", official = true)
        )
        assertEquals("official", TrailerPicker.pick(videos))
    }

    @Test
    fun `没有官方时_取第一条普通预告片`() {
        val videos = listOf(
            video(key = "first", type = "Trailer"),
            video(key = "second", type = "Trailer")
        )
        assertEquals("first", TrailerPicker.pick(videos))
    }

    @Test
    fun `没有预告片时_退而选Teaser`() {
        val videos = listOf(
            video(key = "teaser", type = "Teaser"),
            video(key = "clip", type = "Clip")
        )
        // Trailer 一个都没有，Teaser 优先级高于 Clip
        assertEquals("teaser", TrailerPicker.pick(videos))
    }

    @Test
    fun `只有花絮或片段时_仍兜底返回一条可用视频`() {
        val videos = listOf(video(key = "bts", type = "Behind the Scenes"))
        assertEquals("bts", TrailerPicker.pick(videos))
    }

    @Test
    fun `站点判断不区分大小写`() {
        val videos = listOf(video(key = "lower", site = "youtube", type = "Trailer"))
        assertEquals("lower", TrailerPicker.pick(videos))
    }
}
