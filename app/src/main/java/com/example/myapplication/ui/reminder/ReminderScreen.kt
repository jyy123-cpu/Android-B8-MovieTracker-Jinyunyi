package com.example.myapplication.ui.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.MovieTrackerApplication
import com.example.myapplication.core.Constants
import com.example.myapplication.data.local.entity.MovieEntity
import com.example.myapplication.reminder.RequestNotificationPermission
import com.example.myapplication.ui.components.EmptyView
import com.example.myapplication.ui.components.LoadingView

/**
 * 提醒中心路由入口（伴生模块 P3）：获取容器、创建 ViewModel，并申请通知权限。
 */
@Composable
fun ReminderRoute() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val repository = (appContext as MovieTrackerApplication).container.movieRepository

    val viewModel: ReminderViewModel = viewModel(
        factory = ReminderViewModel.provideFactory(repository, appContext)
    )

    // 进入页面时申请通知权限（Android 13+）
    RequestNotificationPermission()

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReminderScreen(
        state = state,
        onToggleReminder = viewModel::toggleReminder,
        onTestNotification = viewModel::sendTestNotification
    )
}

/**
 * 提醒中心纯展示界面。
 */
@Composable
fun ReminderScreen(
    state: ReminderUiState,
    onToggleReminder: (Int) -> Unit,
    onTestNotification: (Int) -> Unit
) {
    when {
        state.loading -> LoadingView()

        state.movies.isEmpty() -> EmptyView(
            message = "看单里还没有电影，先去发现页或搜索页添加吧"
        )

        else -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            item {
                Text(
                    text = "上映提醒",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(state.movies, key = { it.id }) { movie ->
                ReminderItem(
                    movie = movie,
                    onToggle = { onToggleReminder(movie.id) },
                    onTest = { onTestNotification(movie.id) }
                )
            }
        }
    }
}

/**
 * 单部电影的提醒卡片：海报、信息、提醒开关和测试按钮。
 */
@Composable
private fun ReminderItem(
    movie: MovieEntity,
    onToggle: () -> Unit,
    onTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 海报小图
            AsyncImage(
                model = Constants.posterUrl(movie.posterPath),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 56.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 中间信息区，占满剩余空间
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "上映：${movie.releaseDate ?: "未知"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(onClick = onTest) {
                    Text(text = "测试通知", style = MaterialTheme.typography.labelSmall)
                }
            }

            // 右侧提醒开关
            Switch(
                checked = movie.reminderEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
