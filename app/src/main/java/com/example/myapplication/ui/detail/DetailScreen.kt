package com.example.myapplication.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 详情页：海报、简介、评分、加入看单、打分、设置上映提醒。
 * @param movieId 从列表跳转携带的 TMDb 电影 id
 */
@Composable
fun DetailScreen(movieId: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "详情页：电影 ID = $movieId（待开发）",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
