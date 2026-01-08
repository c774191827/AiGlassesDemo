package com.lw.top.lib_core.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object AppKeys {
        val SERVER_ENVIRONMENT = stringPreferencesKey("server_environment")
    }

    val savedEnvironment: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AppKeys.SERVER_ENVIRONMENT]
    }

    suspend fun getEnvironment(): String? {
        return savedEnvironment.firstOrNull()
    }

    /**
     * 保存当前选择的环境名称 (枚举的 name)
     */
    suspend fun saveEnvironment(envName: String) {
        dataStore.edit { preferences ->
            preferences[AppKeys.SERVER_ENVIRONMENT] = envName
        }
    }
}
