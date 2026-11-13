package com.example.myapplication.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

/**
 * 通知权限工具。
 * Android 13（API 33）起，发送通知需要运行时申请 POST_NOTIFICATIONS；
 * 低版本在安装时就已授予，这里直接视为已授权。
 */

/** 当前是否已经获得通知权限 */
@Composable
fun hasNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    val context = LocalContext.current
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * 进入界面时自动弹出一次通知权限申请。
 * 用户拒绝也不阻塞界面，只是通知可能发不出来，可稍后再引导。
 */
@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // 结果无需特殊处理：授权后通知自然能发，拒绝则保持静默
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
