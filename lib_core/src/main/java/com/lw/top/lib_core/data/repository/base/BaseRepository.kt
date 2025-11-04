package com.lw.top.lib_core.data.repository.base

import com.lw.top.lib_core.data.model.response.ApiResponse
import com.lw.top.lib_core.data.model.response.ApiResult
import java.io.IOException

open class BaseRepository {
    suspend fun <T> safeApiCall(
        call: suspend () -> ApiResponse<T?>
    ): ApiResult<T?> {
        return try {
            val response = call()
            if (response.code == 0) {
                ApiResult.Success(response.data)
            } else {
                ApiResult.Error(
                    exception = Exception("API error ${response.code}"),
                    msg = response.msg
                )
            }
        } catch (e: IOException) { // 网络连接错误等
            ApiResult.Error(exception = e, msg = "Network error: ${e.localizedMessage}")
        } catch (e: Exception) { // 其他未知错误，例如 GSON 解析错误等
            ApiResult.Error(
                exception = e,
                msg = "An unexpected error occurred: ${e.localizedMessage}"
            )
        }
    }
}