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
class BluetoothDataManager@Inject constructor(
    private val dataStore: DataStore<Preferences>
){
    private object BluetoothKeys {
        val BLUETOOTH_ADDRESS = stringPreferencesKey("bluetooth_address")
        val BLUETOOTH_NAME = stringPreferencesKey("bluetooth_name")
    }

    val savedBluetoothAddress: Flow<String?> = dataStore.data.map { preferences ->
        preferences[BluetoothKeys.BLUETOOTH_ADDRESS]
    }

    val savedBluetoothName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[BluetoothKeys.BLUETOOTH_NAME]
    }


    suspend fun getBluetoothAddress(): String? {
        return savedBluetoothAddress.firstOrNull()
    }

    suspend fun getBluetoothName(): String? {
        return savedBluetoothName.firstOrNull()
    }


    /**
     * 保存蓝牙设备的地址和名称。
     */
    suspend fun saveBluetoothDevice(address: String, name: String) {
        dataStore.edit { preferences ->
            preferences[BluetoothKeys.BLUETOOTH_ADDRESS] = address
            preferences[BluetoothKeys.BLUETOOTH_NAME] = name
        }
    }

    /**
     * 清除存储的蓝牙设备信息。
     */
    suspend fun clearBluetoothDevice() {
        dataStore.edit { preferences ->
            preferences.remove(BluetoothKeys.BLUETOOTH_ADDRESS)
            preferences.remove(BluetoothKeys.BLUETOOTH_NAME)
        }
    }



}