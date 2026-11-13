package com.example.myapplication.data.remote.dto

import com.example.myapplication.data.local.entity.WatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * DTO 到 Room 实体映射（[Mappers]）的本地单元测试。
 */
class MappersTest {

    private val addedAt = 1_700_000_000_000L

    @Test
    fun `列表DTO转实体_字段正确映射`() {
        val dto = MovieDto(
            id = 555,
            title = "列表电影",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            overview = "剧情简介",
            releaseDate = "2026-08-01",
            voteAverage = 7.8,
            popularity = 120.5,
            genreIds = listOf(28, 12)
        )

        val entity = dto.toEntity(WatchStatus.PLAN, addedAt)

        // 基本字段逐一对应
        assertEquals(555, entity.id)
        assertEquals("列表电影", entity.title)
        assertEquals("/poster.jpg", entity.posterPath)
        assertEquals("/backdrop.jpg", entity.backdropPath)
        assertEquals("剧情简介", entity.overview)
        assertEquals("2026-08-01", entity.releaseDate)
        assertEquals(7.8, entity.voteAverage, 1e-9)
        assertEquals(120.5, entity.popularity, 1e-9)
        assertEquals(WatchStatus.PLAN, entity.status)
        assertEquals(addedAt, entity.addedAt)
    }

    @Test
    fun `列表DTO转实体_新增记录默认无评分无提醒且类型为空`() {
        val entity = MovieDto(id = 1).toEntity(WatchStatus.WATCHING, addedAt)

        // 列表接口拿不到类型名，genres 留空；新记录不应该带评分和提醒
        assertNull(entity.userRating)
        assertNull(entity.genres)
        assertFalse(entity.reminderEnabled)
        assertNull(entity.reminderTime)
    }

    @Test
    fun `详情DTO转实体_类型名用逗号拼接`() {
        val dto = MovieDetailDto(
            id = 888,
            title = "详情电影",
            genres = listOf(
                GenreDto(id = 28, name = "动作"),
                GenreDto(id = 878, name = "科幻")
            )
        )

        val entity = dto.toEntity(WatchStatus.WATCHED, addedAt)

        assertEquals(888, entity.id)
        // 详情接口能拿到完整类型名，按逗号拼接成一个字符串
        assertEquals("动作,科幻", entity.genres)
        assertEquals(WatchStatus.WATCHED, entity.status)
    }

    @Test
    fun `详情DTO转实体_无类型时得到空字符串`() {
        val entity = MovieDetailDto(id = 2).toEntity(WatchStatus.PLAN, addedAt)
        // 空列表 joinToString 结果是空字符串，而不是 null
        assertEquals("", entity.genres)
        assertNull(entity.userRating)
        assertFalse(entity.reminderEnabled)
    }
}
