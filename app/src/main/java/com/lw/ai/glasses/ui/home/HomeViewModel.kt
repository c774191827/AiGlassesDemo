package com.lw.ai.glasses.ui.home

import BaseViewModel
import android.Manifest
import android.content.Context
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.fission.wear.glasses.sdk.GlassesManage
import com.fission.wear.glasses.sdk.config.BleComConfig
import com.fission.wear.glasses.sdk.events.CmdResultEvent
import com.fission.wear.glasses.sdk.events.ConnectionStateEvent
import com.fission.wear.glasses.sdk.events.ScanStateEvent
import com.lw.top.lib_core.data.datastore.BluetoothDataManager
import com.polidea.rxandroidble3.exceptions.BleDisconnectedException
import com.polidea.rxandroidble3.exceptions.BleGattException
import com.polidea.rxandroidble3.scan.ScanFilter
import com.polidea.rxandroidble3.scan.ScanSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothDataManager: BluetoothDataManager
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _permissionEvent = MutableSharedFlow<List<String>>(replay = 1)
    val permissionEvent = _permissionEvent.asSharedFlow()


    init {
        viewModelScope.launch {
            if (!bluetoothDataManager.getBluetoothAddress().isNullOrEmpty()) {
                connectDevice(
                    bluetoothDataManager.getBluetoothAddress()!!,
                    bluetoothDataManager.getBluetoothName()!!
                )
            }
        }
        checkAndRequestPermissions()
        observeGlassesEvents()
        updateFeatures()
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = getPermissionsToRequest()
        if (permissionsToRequest.isNotEmpty()) {
            viewModelScope.launch {
                _permissionEvent.emit(permissionsToRequest)
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (!isGranted) {
            ToastUtils.showLong("部分权限被拒绝，功能可能受限")
        }
    }

    private fun getPermissionsToRequest(): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {//部分国产手机可能需要加上位置权限
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return permissions
    }

    fun observeGlassesEvents() {
        viewModelScope.launch {
            GlassesManage.eventFlow().collect { events ->
                when (events) {
                    is ScanStateEvent.DeviceFound -> {
                        _uiState.update { state ->
                            state.copy(
                                scannedDevices = state.scannedDevices
                                    .plus(events.data)
                                    .distinctBy { it.bleDevice.macAddress }
                                    .sortedByDescending { it.rssi }
                                    .filter {
                                        it.bleDevice.name?.contains(
                                            "Glass",
                                            ignoreCase = true
                                        ) == true
                                    }
                            )
                        }
                    }

                    is ScanStateEvent.ScanFinished -> {
                        _uiState.update {
                            it.copy(isScanning = false)
                        }
                    }

                    is ScanStateEvent.Error -> {
                        _uiState.update {
                            it.copy(isScanning = false)
                        }
                    }

                    is ConnectionStateEvent.Connecting -> {
                        _uiState.update {
                            it.copy(
                                connectionState = ConnectionState.CONNECTING,
                            )
                        }
                    }

                    is ConnectionStateEvent.Connected -> {
                        _uiState.update {
                            it.copy(
                                connectionState = ConnectionState.CONNECTED,
                            )
                        }
                        GlassesManage.getBatteryLevel()
                        GlassesManage.getMediaFileCount()
                        GlassesManage.connectAiAssistant()
                    }

                    is ConnectionStateEvent.Disconnected -> {
                        _uiState.update {
                            it.copy(
                                connectionState = ConnectionState.DISCONNECTED,
                                batteryLevel = -1
                            )
                        }

                        if (error is BleDisconnectedException || error is BleGattException) {
                            _uiState.update {
                                it.copy(connectionState = ConnectionState.DISCONNECTED)
                            }
                        } else {

                        }

                    }

                    is CmdResultEvent.DevicePower -> {
                        _uiState.update {
                            it.copy(
                                batteryLevel = events.value ?: 0,
                                isCharging = events.isCharging
                            )
                        }
                    }

                    is CmdResultEvent.MediaFileCount -> {
                        _uiState.update { it.copy(pendingSyncPhotosCount = events.count) }
                        updateFeatures()
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun startScanDevice() {
        GlassesManage.initialize(context, 2)
        if (_uiState.value.isScanning) return
        GlassesManage.startScanBleDevices(
            bleScanConfig = null,
            scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build(),
            scanFilters = arrayOf(ScanFilter.Builder().build())
        )
    }

    fun connectDevice(mac: String, name: String) {
        GlassesManage.initialize(context, 2)
        GlassesManage.stopScanBleDevices(context)
        GlassesManage.connect(BleComConfig(context, mac))
        viewModelScope.launch {
            bluetoothDataManager.saveBluetoothDevice(mac, name)
        }
        _uiState.update {
            it.copy(
                connectedDeviceName = name,
            )
        }
    }

    private fun updateFeatures() {
        _uiState.update { currentState ->
            currentState.copy(
                features = HomeUiState.initialFeatures(currentState.pendingSyncPhotosCount)
            )
        }
    }

}