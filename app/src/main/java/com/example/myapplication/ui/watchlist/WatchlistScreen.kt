package com.example.myapplication.ui.watchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.MovieTrackerApplication
import com.example.myapplication.core.Constants
import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.ui.components.EmptyView
import com.example.myapplication.ui.components.LoadingView

/**
 * 看单页路由入口：获取仓库、创建 ViewModel。
 * @param onMovieClick 点击某部电影，带着 id 进入详情页
 */
@Composable
fun WatchlistRoute(onMovieClick: (Int) -> Unit) {
    val context = LocalContext.current
    val repository = (context.applicationContext as MovieTrackerApplication)
        .container.movieRepository
    val viewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModel.provideFactory(repository)
    )
    WatchlistScreen(
        viewModel = viewModel,
        onMovieClick = onMovieClick
    )
}

/**
 * 看单页主体：加载态 / 空态 / 统计 + 筛选 + 列表。
 */
@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onMovieClick: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    when (val current = state) {
        is WatchlistUiState.Loading -> LoadingView()

        is WatchlistUiState.Success -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部统计卡片
                StatsCard(state = current)

                // 状态筛选
                ChipRow(
                    options = StatusFilter.entries,
                    selected = current.filter,
                    labelOf = { it.label },
                    onSelect = { viewModel.setFilter(it) }
                )

                // 排序方式
                ChipRow(
                    options = SortMode.entries,
                    selected = current.sortMode,
                    labelOf = { it.label },
                    onSelect = { viewModel.setSortMode(it) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 列表区：当前筛选结果为空时显示空态
                if (current.filteredMovies.isEmpty()) {
                    EmptyView(message = "这个分类下还没有电影，去发现页加入看单吧")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(current.filteredMovies, key = { it.id }) { movie ->
                            WatchlistItem(
                                movie = movie,
                                onClick = { onMovieClick(movie.id) },
                                onRemove = { viewModel.remove(movie.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 顶部统计卡片：总数、各状态数量、平均综合分、平均我的评分。
 */
@Composable
private fun StatsCard(state: WatchlistUiState.Success) {
    val movies = state.allMovies
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 第一行：总数、平均综合分、平均我的评分
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(value = movies.size.toString(), label = "看单总数")
                StatItem(
                    value = String.format("%.1f", state.averageComposite),
                    label = "平均综合分"
                )
                StatItem(
                    value = state.averageUserRating?.let { String.format("%.1f", it) } ?: "—",
                    label = "平均我的评分"
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // 第二行：三个状态各自的数量
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    value = movies.count { it.status.name == "PLAN" }.toString(),
                    label = "想看"
                )
                StatItem(
                    value = movies.count { it.status.name == "WATCHING" }.toString(),
                    label = "在看"
                )
                StatItem(
                    value = movies.count { it.status.name == "WATCHED" }.toString(),
                    label = "看过"
                )
            }
        }
    }
}

/** 统计项：上方数字、下方标签 */
@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 通用一行筛选 Chip，用于状态筛选和排序。
 */
@Composable
private fun <T> ChipRow(
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(labelOf(option)) }
            )
        }
    }
}

/**
 * 单条看单卡片：海报 + 信息 + 综合分 + 移除按钮。
 */
@Composable
private fun WatchlistItem(
    movie: MovieEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            // 海报
            AsyncImage(
                model = Constants.posterUrl(movie.posterPath),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            // 信息列
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "状态：${movie.status.label}",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.height(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "官方 ${String.format("%.1f", movie.voteAverage)}" +
                            "　我的 ${movie.userRating?.let { String.format("%.1f", it) } ?: "未评"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // 综合推荐指数，突出显示
                Text(
                    text = "综合推荐 ${String.format("%.1f", Scoring.compositeScore(movie))}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // 移除按钮
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "从看单移除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
