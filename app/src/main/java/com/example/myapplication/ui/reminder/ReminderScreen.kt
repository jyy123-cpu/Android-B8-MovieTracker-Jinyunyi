package com.example.myapplication.ui.reminder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 提醒中心（伴生模块 P3 入口）：
 * 展示待提醒影片、取消提醒、手动测试通知，通知内可直接操作看单数据。
 */
@Composable
fun ReminderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "提醒中心：上映提醒管理（待开发）",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
