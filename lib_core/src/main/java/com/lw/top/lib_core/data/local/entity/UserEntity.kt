package com.lw.top.lib_core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val age: Int = 25,
    val avatar: String?,
    val createTime: Long,
    val email: String,
    val inviteCode: String,
    val isSetPassword: Int,
    val nickname: String,
    val status: Int,
    val updateTime: Long,
    var loginTime: Long = 0,
    var isCurrentUser: Boolean = false,
    var accessToken: String = "",
    var refreshToken: String = "",
)
