package com.example.myapplication.ui.watchlist

import com.example.myapplication.data.local.entity.MovieEntity
import kotlin.math.ln

/**
 * 看单综合评分算法。
 *
 * 把三类信息融合成一个 0-10 的「综合推荐指数」：
 * 1. 我的评分（userRating）：看过的电影更相信自己的判断，权重更高；
 * 2. TMDb 官方评分（voteAverage）：作为口碑基础；
 * 3. 热度（popularity）：用对数压缩后做小幅加分，避免高热度电影把分数拉得太夸张。
 *
 * 写成纯函数（不依赖 Android 框架），便于直接做单元测试。
 */
object Scoring {

    // 融合权重：我的评分占 0.6，官方评分占 0.4
    private const val USER_WEIGHT = 0.6
    private const val TMDB_WEIGHT = 0.4

    // 热度归一化参考值，TMDb popularity 常见在 0~2000 之间
    private const val POPULARITY_REFERENCE = 2000.0

    // 热度最多贡献 0.5 分
    private const val MAX_POPULARITY_BONUS = 0.5

    /**
     * 计算单部电影的综合推荐指数，结果范围 0-10。
     */
    fun compositeScore(movie: MovieEntity): Double {
        // 1. 口碑分：有我的评分就加权融合，没有就直接用官方评分
        val wordOfMouth = if (movie.userRating != null) {
            USER_WEIGHT * movie.userRating + TMDB_WEIGHT * movie.voteAverage
        } else {
            movie.voteAverage
        }

        // 2. 热度加成：对数压缩后归一化到 0~1，再乘以最大加分
        val normalizedPopularity =
            ln(movie.popularity + 1.0) / ln(POPULARITY_REFERENCE + 1.0)
        val popularityBonus =
            MAX_POPULARITY_BONUS * normalizedPopularity.coerceIn(0.0, 1.0)

        // 3. 相加并限制在 0-10
        return (wordOfMouth + popularityBonus).coerceIn(0.0, 10.0)
    }
}
