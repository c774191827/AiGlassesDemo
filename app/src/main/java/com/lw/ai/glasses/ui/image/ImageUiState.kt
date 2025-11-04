package com.lw.ai.glasses.ui.image

import com.lw.top.lib_core.data.local.entity.MediaFilesEntity



data class ImageUiState(
    val images: List<MediaFilesEntity> = emptyList(),
    val syncState: SyncState = SyncState()
)

data class SyncState(
    val isSyncing: Boolean = false,
    val syncProgress: Float = 0.0f,
    val currentFileIndex: Int = 0,
    val totalFilesToSync: Int = 0
)