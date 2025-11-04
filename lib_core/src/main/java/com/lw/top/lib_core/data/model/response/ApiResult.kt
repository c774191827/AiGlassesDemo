package com.lw.top.lib_core.data.model.response

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Throwable, val msg: String? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()

    object Empty : ApiResult<Nothing>()
}