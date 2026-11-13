package com.example.myapplication.ui.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.request.ImageRequest
import com.example.myapplication.MovieTrackerApplication
import com.example.myapplication.core.Constants
import com.example.myapplication.data.local.entity.WatchStatus
import com.example.myapplication.ui.components.ErrorView
import com.example.myapplication.ui.components.LoadingView

/**
 * 详情页路由入口：获取仓库、创建 ViewModel。
 */
@Composable
fun DetailRoute(
    movieId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as MovieTrackerApplication)
        .container.movieRepository
    val viewModel: DetailViewModel = viewModel(
        factory = DetailViewModel.provideFactory(movieId, repository)
    )
    DetailScreen(viewModel = viewModel, onBack = onBack)
}

/**
 * 详情页主体：根据状态显示加载、错误或完整内容。
 */
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val current = state) {
            is DetailUiState.Loading -> LoadingView()

            is DetailUiState.Error -> ErrorView(
                message = current.message,
                onRetry = viewModel::loadDetail
            )

            is DetailUiState.Success -> DetailContent(
                state = current,
                onBack = onBack,
                onAddToWatchlist = { viewModel.addToWatchlist(current.detail) },
                onChangeStatus = viewModel::changeStatus,
                onRate = viewModel::rate
            )
        }
    }
}

/**
 * 详情内容：顶部背景大图 + 可滚动的信息区和操作区。
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun DetailContent(
    state: DetailUiState.Success,
    onBack: () -> Unit,
    onAddToWatchlist: () -> Unit,
    onChangeStatus: (WatchStatus) -> Unit,
    onRate: (Float) -> Unit
) {
    val detail = state.detail
    val savedMovie = state.savedMovie

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 内容可上下滚动
    ) {
        // 顶部背景大图
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Constants.backdropUrl(detail.backdropPath))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            // 返回按钮：半透明圆形底，浮在大图左上角
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0x80000000),
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // 海报 + 基础信息横向排列
            Row {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Constants.posterUrl(detail.posterPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = detail.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(110.dp)
                        .height(165.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "评分：${String.format("%.1f", detail.voteAverage)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "上映：${detail.releaseDate?.ifBlank { "未知" } ?: "未知"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "片长：${detail.runtime?.let { "$it 分钟" } ?: "未知"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 类型标签，自动换行
            Text(
                text = "类型",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.genres.forEach { genre ->
                    AssistChip(
                        onClick = {},
                        label = { Text(genre.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 剧情简介
            Text(
                text = "剧情简介",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detail.overview?.ifBlank { "暂无简介" } ?: "暂无简介",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 操作区：未加入看单时显示加入按钮
            if (savedMovie == null) {
                Button(
                    onClick = onAddToWatchlist,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "加入看单")
                }
            } else {
                // 已加入：可切换看单状态
                Text(
                    text = "看单状态切换",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WatchStatus.entries.forEach { status ->
                        FilterChip(
                            selected = savedMovie.status == status,
                            onClick = { onChangeStatus(status) },
                            label = { Text(status.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 我的评分：0-10 分，步长 0.5
                Text(
                    text = "我的评分：${String.format("%.1f", savedMovie.userRating ?: 0f)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // 用本地状态记住滑块位置，拖动结束再写库，避免频繁操作
                var sliderValue by remember(savedMovie.id) {
                    mutableFloatStateOf(savedMovie.userRating ?: 0f)
                }
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onRate(sliderValue) },
                    valueRange = 0f..10f,
                    steps = 19 // 20 个间隔，每格 0.5 分
                )
            }
        }
    }
}
