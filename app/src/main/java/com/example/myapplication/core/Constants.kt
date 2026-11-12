package com.example.myapplication.core

/**
 * 全局常量：TMDb 图片地址与尺寸。
 * TMDb 返回的 poster_path 只是相对路径，需要拼接图片域名才能加载。
 */
object Constants {
    private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    const val POSTER_SIZE = "w500"
    const val BACKDROP_SIZE = "w780"

    /** 拼接完整海报地址，path 为 null 时返回 null（界面可显示占位图） */
    fun posterUrl(path: String?): String? =
        if (path.isNullOrBlank()) null else "$IMAGE_BASE_URL$POSTER_SIZE$path"

    /** 拼接完整背景大图地址 */
    fun backdropUrl(path: String?): String? =
        if (path.isNullOrBlank()) null else "$IMAGE_BASE_URL$BACKDROP_SIZE$path"
}
