package com.lw.ai.glasses.ui.image

import BaseViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.fission.wear.glasses.sdk.GlassesManage
import com.fission.wear.glasses.sdk.events.FileSyncEvent
import com.lw.top.lib_core.data.local.entity.MediaFilesEntity
import com.lw.top.lib_core.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : BaseViewModel() {

    private val _syncState = MutableStateFlow(SyncState())

    val uiState: StateFlow<ImageUiState> = combine(
        photoRepository.getSyncedPhotosFlow(),
        _syncState
    ) { photos, syncState ->
        ImageUiState(
            images = photos,
            syncState = syncState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ImageUiState()
    )

    init {
//        viewModelScope.launch {
//            photoRepository.clearAllPhotos()
//        }
        observeGlassesEvents()
    }


    fun syncAllMediaFile(){
        if (_syncState.value.isSyncing) {
            ToastUtils.showLong("文件正在同步中")
            return
        }
        _syncState.value = SyncState(isSyncing = true)
        GlassesManage.syncAllMediaFile()
    }

    fun observeGlassesEvents() {
        viewModelScope.launch {
            GlassesManage.eventFlow().collect { events ->
                when (events) {

                    is FileSyncEvent.ConnectSuccess -> {

                    }

                    is FileSyncEvent.DownloadProgress -> {
                        _syncState.update {
                            it.copy(
                                syncProgress = events.progress / 100f,
                                currentFileIndex = events.curFileIndex,
                                totalFilesToSync = events.totalFileCount
                            )
                        }
                    }

                    is FileSyncEvent.DownloadSuccess -> {
                        val newFileEntity = MediaFilesEntity(
                            filePath = events.filePath,
                            type = "IMAGE",
                            createdAt = System.currentTimeMillis(),
                            size = 10000,
                        )
                        photoRepository.insertPhoto(newFileEntity)
                        val isLastFile = (events.curFileIndex + 1) == events.totalFileCount
                        if (isLastFile) {
                            _syncState.value = SyncState()
                        }
                    }

                    is FileSyncEvent.Failed -> {
                        _syncState.value = SyncState()
                    }

                    else -> {

                    }
                }
            }
        }
    }

}