package com.example.myapplication.ui.watchlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 我的看单页：想看 / 在看 / 看过三个分类，读取 Room 数据并按评分算法排序。
 */
@Composable
fun WatchlistScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "看单页：想看 / 在看 / 看过（待开发）",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
