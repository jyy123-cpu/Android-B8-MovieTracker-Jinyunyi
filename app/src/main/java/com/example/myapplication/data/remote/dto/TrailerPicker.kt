package com.example.myapplication.data.remote.dto

/**
 * 从 TMDb 返回的视频列表中挑一个最适合当「预告片」的 key。
 *
 * 挑选优先级：
 * 1. 官方预告片（type=Trailer 且 official=true）
 * 2. 普通预告片（type=Trailer）
 * 3. 先行片段（type=Teaser）
 * 4. 其余任意 YouTube 视频
 *
 * 只有 site=YouTube 且 key 非空的结果才可用；
 * 写成纯函数，便于单元测试，不依赖网络和 Android 框架。
 */
object TrailerPicker {

    fun pick(videos: List<VideoDto>): String? {
        // 先过滤出真正能打开的 YouTube 视频
        val usable = videos.filter { it.site.equals(SITE_YOUTUBE, ignoreCase = true) && it.key.isNotBlank() }
        if (usable.isEmpty()) return null

        val best = usable.firstOrNull { it.type == TYPE_TRAILER && it.official }
            ?: usable.firstOrNull { it.type == TYPE_TRAILER }
            ?: usable.firstOrNull { it.type == TYPE_TEASER }
            ?: usable.first()
        return best.key
    }

    private const val SITE_YOUTUBE = "YouTube"
    private const val TYPE_TRAILER = "Trailer"
    private const val TYPE_TEASER = "Teaser"
}
