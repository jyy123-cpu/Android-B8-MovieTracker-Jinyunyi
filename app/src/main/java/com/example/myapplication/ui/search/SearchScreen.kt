package com.example.myapplication.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 搜索页：按关键词调用 TMDb 搜索接口（后续接入）。
 */
@Composable
fun SearchScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "搜索页：关键词搜索电影（待开发）",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
