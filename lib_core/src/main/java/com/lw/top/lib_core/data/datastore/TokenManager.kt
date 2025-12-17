package com.lw.top.lib_core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private var currentAccessToken: String? = null
    private var currentRefreshToken: String? = null
    private val tokenLock = Any()

    init {
        runBlocking {
            val prefs = dataStore.data.firstOrNull()
            currentAccessToken = prefs?.get(PreferencesKeys.ACCESS_TOKEN)
            currentRefreshToken = prefs?.get(PreferencesKeys.REFRESH_TOKEN)
        }
    }

    /**
     * 获取当前用户令牌 (Access Token)。优先从内存缓存读取。
     * 这个方法应该尽量快，主要供 HeaderInterceptor 调用。
     */
    fun getAccessToken(): String? {
        synchronized(tokenLock) {
            return currentAccessToken
        }
    }

    /**
     * 获取用于刷新操作的 Refresh Token。
     * 这个方法应该只在准备调用刷新Token API时使用。
     */
    suspend fun getRefreshToken(): String? {
        synchronized(tokenLock) {
            if (currentRefreshToken != null) return currentRefreshToken
        }
        // 如果内存中没有（例如，应用刚启动且init中的runBlocking未完全执行完，或被清除了）
        // 则从DataStore读取最新的
        val refreshTokenFromDs = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN]
        }.firstOrNull()
        synchronized(tokenLock) {
            currentRefreshToken = refreshTokenFromDs // 更新内存缓存
        }
        return refreshTokenFromDs
    }

    /**
     * 保存新的访问令牌和可选的新的刷新令牌。
     * @param newAccessToken 新的访问令牌。
     * @param newRefreshToken 可选的新的刷新令牌 (如果API在刷新时轮换了Refresh Token)。
     */
    suspend fun saveTokens(newAccessToken: String? = null, newRefreshToken: String? = null) {
        synchronized(tokenLock) {
            currentAccessToken = newAccessToken
            if (newRefreshToken != null) {
                currentRefreshToken = newRefreshToken
            }
        }
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN] = newAccessToken!!
            if (newRefreshToken != null) {
                preferences[PreferencesKeys.REFRESH_TOKEN] = newRefreshToken
            }
        }
    }

    /**
     * 退出登陆时调用，清除当前用户令牌 (Access Token)
     */
    suspend fun clearUserToken() {
        synchronized(tokenLock) {
            currentAccessToken = null
        }
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACCESS_TOKEN)
            preferences.remove(PreferencesKeys.REFRESH_TOKEN)
        }
    }


}