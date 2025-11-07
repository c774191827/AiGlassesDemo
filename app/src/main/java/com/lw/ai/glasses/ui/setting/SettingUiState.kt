package com.lw.ai.glasses.ui.setting

import androidx.compose.runtime.Stable

data class SettingUiState(
    val settingItems: List<SettingItem> = emptyList(),
    val disconnectAction: DisconnectActionState = DisconnectActionState()
)

@Stable
data class DisconnectActionState(
    val title: String = "断开连接",
    val isEnabled: Boolean = true
)

sealed interface SettingItem {
    data class ActionItem(
        val id: String,
        val title: String,
        val summary: String? = null,
        val isEnabled: Boolean = true
    ) : SettingItem

    data class SwitchItem(
        val id: String,
        val title: String,
        val summary: String? = null,
        val isChecked: Boolean,
        val isEnabled: Boolean = true
    ) : SettingItem

    data class InfoItem(
        val title: String,
        val value: String
    ) : SettingItem

    data object Divider : SettingItem
}