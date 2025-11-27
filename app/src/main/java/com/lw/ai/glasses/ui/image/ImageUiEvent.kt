package com.lw.ai.glasses.ui.image

import com.lw.top.lib_core.data.local.entity.MediaFilesEntity

sealed class ImageUiEvent {
    data class SelectImage(val image: MediaFilesEntity) : ImageUiEvent()
    object DismissImage : ImageUiEvent()
    object None : ImageUiEvent()
}