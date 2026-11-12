package com.example.myapplication.core

/**
 * 网络请求安全封装：
 * 把可能抛出异常的调用包成 Result，界面据此区分成功与失败，避免直接崩溃。
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        // 网络异常、解析异常、超时等都会走到这里
        Result.failure(e)
    }
}
