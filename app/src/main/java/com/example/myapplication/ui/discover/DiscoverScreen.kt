package com.example.myapplication.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.MovieTrackerApplication
import com.example.myapplication.ui.components.EmptyView
import com.example.myapplication.ui.components.ErrorView
import com.example.myapplication.ui.components.LoadingView
import com.example.myapplication.ui.components.MovieCard

/**
 * 发现页路由入口：获取仓库并创建 ViewModel。
 */
@Composable
fun DiscoverRoute(onMovieClick: (Int) -> Unit) {
    val context = LocalContext.current
    val repository = (context.applicationContext as MovieTrackerApplication)
        .container.movieRepository
    val viewModel: DiscoverViewModel = viewModel(
        factory = DiscoverViewModel.provideFactory(repository)
    )
    DiscoverScreen(viewModel = viewModel, onMovieClick = onMovieClick)
}

/**
 * 发现页展示：热门电影海报网格 + 三态 + 滚动分页。
 */
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onMovieClick: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val movies by viewModel.movies.collectAsState()

    val gridState = rememberLazyGridState()

    // 当最后一个可见项接近列表末尾时加载更多
    val shouldLoadMore by remember {
        derivedStateOf {
            val total = gridState.layoutInfo.totalItemsCount
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisibleIndex >= total - 4
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val current = state) {
            is DiscoverUiState.Loading -> LoadingView()

            is DiscoverUiState.Empty -> EmptyView(message = "暂无热门电影数据")

            is DiscoverUiState.Error -> ErrorView(
                message = current.message,
                onRetry = viewModel::loadFirstPage
            )

            is DiscoverUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movies, key = { it.id }) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) }
                        )
                    }
                }
            }
        }
    }
}
