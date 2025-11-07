package com.lw.ai.glasses.ui.setting

import BaseViewModel
import com.fission.wear.glasses.sdk.GlassesManage
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class SettingViewModel @Inject constructor(
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val items = listOf(
            SettingItem.InfoItem(title = "固件版本", value = "1.0.0"),
            SettingItem.Divider,
            SettingItem.InfoItem(title = "App版本", value = "1.0.0")
        )

        _uiState.update {
            it.copy(
                settingItems = items,
                disconnectAction = DisconnectActionState(isEnabled = true) // 假设设备已连接
            )
        }
    }

    fun handleAction(itemId: String) {
        when (itemId) {

        }
    }

    fun onDisconnect() {
        GlassesManage.disConnect()
        _uiState.update {
            it.copy(disconnectAction = it.disconnectAction.copy(isEnabled = false))
        }
    }

}