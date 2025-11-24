package com.lw.top.lib_core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val RECENT_FIRMWARE_FILES_KEY =
        stringSetPreferencesKey("recent_firmware_files")

    suspend fun getRecentFilesJson(): Set<String> {
        return dataStore.data
            .map { preferences ->
                preferences[RECENT_FIRMWARE_FILES_KEY] ?: emptySet()
            }
            .first()
    }

    suspend fun saveRecentFilesJson(filesJson: Set<String>) {
        dataStore.edit { preferences ->
            preferences[RECENT_FIRMWARE_FILES_KEY] = filesJson
        }
    }

}