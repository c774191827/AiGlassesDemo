package com.lw.ai.glasses.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import com.polidea.rxandroidble3.scan.ScanResult

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

data class HomeUiState(
    val scannedDevices: List<ScanResult> = emptyList(),
    val isScanning: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val batteryLevel: Int = -1,
    val connectedDeviceName: String? = null,
    val pendingSyncPhotosCount: Int = 0,
    val features: List<Feature> = emptyList()
){
    companion object {
        fun initialFeatures(pendingSyncPhotosCount: Int): List<Feature> {
            return listOf(
                Feature(
                    id = "sync_photos", // 使用路由作为唯一ID
                    name = "同步图片",
                    icon = Icons.Default.Sync,
                    route = "sync_photos",
                    badgeCount = pendingSyncPhotosCount
                ),
                Feature(id = "ai_chat", name = "AI对话", icon = Icons.Default.QuestionAnswer, route = "assistant"),
                Feature(id = "ai_vision", name = "AI识图", icon = Icons.Default.ImageSearch, route = "assistant"),
                Feature(id = "ai_translate", name = "AI翻译", icon = Icons.Default.Translate, route = "ai_translate"),
                Feature(id = "glasses_settings", name = "眼镜设置", icon = Icons.Default.Settings, route = "glasses_settings")
            )
        }
    }
}

data class Feature(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val route: String,
    val badgeCount: Int? = null
)