package com.example.myapplication.ui.discover

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 发现页：展示 TMDb 热门 / 正在上映电影列表（后续接入网络数据）。
 */
@Composable
fun DiscoverScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "发现页：热门电影列表（待开发）",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
