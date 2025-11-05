package com.lw.top.lib_core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lin_wear_preferences")

object PreferencesKeys {

    val IS_FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("is_first_launch_completed")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val SET_PASSWORD_STATE = intPreferencesKey("set_password_state")
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val USER_ID = stringPreferencesKey("user_id")
    val MAC_ADDRESS=stringPreferencesKey("mac_address")

}