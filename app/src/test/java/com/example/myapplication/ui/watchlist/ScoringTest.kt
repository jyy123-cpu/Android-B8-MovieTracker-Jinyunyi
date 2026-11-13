package com.example.myapplication.ui.watchlist

import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.data.local.entity.WatchStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ln

/**
 * 综合评分算法 [Scoring] 的本地单元测试。
 * 全部是纯 JVM 计算，不依赖 Android 框架和设备。
 */
class ScoringTest {

    /**
     * 构造测试用电影，只暴露和评分有关的三个参数，其余字段给固定默认值。
     */
    private fun buildMovie(
        voteAverage: Double = 0.0,
        popularity: Double = 0.0,
        userRating: Float? = null
    ): MovieEntity = MovieEntity(
        id = 1,
        title = "测试电影",
        posterPath = null,
        backdropPath = null,
        overview = null,
        releaseDate = null,
        voteAverage = voteAverage,
        popularity = popularity,
        status = WatchStatus.PLAN,
        userRating = userRating,
        genres = null,
        addedAt = 0L,
        reminderEnabled = false,
        reminderTime = null
    )

    // 浮点比较容忍误差，避免小数精度导致断言失败
    private val delta = 1e-6

    @Test
    fun `无用户评分且热度为零时_综合分等于官方分`() {
        val movie = buildMovie(voteAverage = 7.5, popularity = 0.0, userRating = null)
        // 热度为 0 时热度加成为 0，没有用户评分则直接用官方分
        assertEquals(7.5, Scoring.compositeScore(movie), delta)
    }

    @Test
    fun `有用户评分时_按六四权重融合`() {
        val movie = buildMovie(voteAverage = 6.0, popularity = 0.0, userRating = 8.0f)
        // 0.6 * 8 + 0.4 * 6 = 4.8 + 2.4 = 7.2
        assertEquals(7.2, Scoring.compositeScore(movie), delta)
    }

    @Test
    fun `热度为零时_热度加成为零`() {
        val movie = buildMovie(voteAverage = 8.0, popularity = 0.0)
        // 不管官方分多少，popularity=0 时 ln(1)=0，加分项为 0
        assertEquals(8.0, Scoring.compositeScore(movie), delta)
    }

    @Test
    fun `热度等于参考值时_拿到满额加分`() {
        // popularity=2000 时归一化结果为 1，热度加分达到上限 0.5
        val movie = buildMovie(voteAverage = 0.0, popularity = 2000.0)
        assertEquals(0.5, Scoring.compositeScore(movie), delta)
    }

    @Test
    fun `超高热度被压缩_加分不超过零点五`() {
        // 远超参考值的热度，归一化后被 coerceIn 限制到 1，加分仍为 0.5
        val movie = buildMovie(voteAverage = 0.0, popularity = 100_000.0)
        assertEquals(0.5, Scoring.compositeScore(movie), delta)
    }

    @Test
    fun `中等热度_对数压缩后加分符合预期`() {
        val movie = buildMovie(voteAverage = 0.0, popularity = 100.0)
        // 手算归一化热度：ln(101) / ln(2001)，再乘 0.5
        val expected = 0.5 * (ln(101.0) / ln(2001.0))
        assertEquals(expected, Scoring.compositeScore(movie), delta)
    }

    @Test
    fun `满分电影加高热_综合分不超过十`() {
        val movie = buildMovie(voteAverage = 10.0, popularity = 2000.0, userRating = 10.0f)
        // 口碑分已经是 10，再加 0.5 会被 coerceIn 截断到 10
        assertEquals(10.0, Scoring.compositeScore(movie), delta)
    }

    @Test
    fun `零分电影_综合分不低于零`() {
        val movie = buildMovie(voteAverage = 0.0, popularity = 0.0)
        assertEquals(0.0, Scoring.compositeScore(movie), delta)
    }

    @Test
    fun `任意输入_综合分都落在零到十之间`() {
        // 用一组极端值组合，验证结果始终在合法区间
        val cases = listOf(
            buildMovie(voteAverage = 10.0, popularity = 5000.0, userRating = 10.0f),
            buildMovie(voteAverage = 0.0, popularity = 0.0, userRating = 0.0f),
            buildMovie(voteAverage = 3.3, popularity = 350.7, userRating = 6.5f)
        )
        cases.forEach {
            val score = Scoring.compositeScore(it)
            assertTrue("分数应在 0..10，实际=$score", score in 0.0..10.0)
        }
    }
}
