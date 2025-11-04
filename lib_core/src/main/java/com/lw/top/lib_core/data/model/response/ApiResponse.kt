package com.lw.top.lib_core.data.model.response

import com.google.gson.annotations.SerializedName


data class ApiResponse<T>( // T 代表 data 字段的具体类型
    @SerializedName("reqId")
    val reqId: String?, // 根据JSON，它可能存在，设为可空更安全

    @SerializedName("code")
    val code: Int,

    @SerializedName("msg")
    val msg: String?, // 根据JSON，它可能存在，设为可空更安全

    @SerializedName("data")
    val data: T? // data 字段，其类型由泛型 T 决定，也可能为null
)