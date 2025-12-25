package com.lw.ai.glasses.ui.live

import BaseViewModel
import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.fission.wear.glasses.sdk.GlassesManage
import com.fission.wear.glasses.sdk.events.LiveEvent
import com.lw.top.lib_core.data.repository.DouYinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import javax.inject.Inject

@HiltViewModel
class LiveViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: DouYinRepository
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()
    var vlcPlayer: MediaPlayer? = null
    private var libVlc: LibVLC? = null


    init {

        val options = ArrayList<String>().apply {
            add("--rtsp-tcp") // 强制 TCP (防花屏)
            add("--network-caching=300") // 降低网络缓存 (低延迟)
            add("-vvv") // 详细日志
        }
        libVlc = LibVLC(context, options)
        vlcPlayer = MediaPlayer(libVlc)

        viewModelScope.launch {
            GlassesManage.eventFlow().collect { events ->
                when (events) {
                    is LiveEvent.LiveSuccess -> {
                        _uiState.update { it.copy(rtspUrl = events.rtsp) }
                        startLocalPlay()
                    }

                    is LiveEvent.Failed ->
                        ToastUtils.showLong(events.reason)

                    else -> {

                    }
                }

            }
        }
    }

    fun updateFps(fps: Int) {
        _uiState.update { it.copy(targetFps = fps) }
    }

    fun updateResolution(res: String) {
        _uiState.update { it.copy(targetResolution = res) }
    }

    fun updateBitrateChange(bitrate: Int) {
        _uiState.update {
            it.copy(bitrate = bitrate)
        }
    }

    fun startLiveStreaming() {
        GlassesManage.startLiveStreaming(
            _uiState.value.targetFps, _uiState.value.targetResolution, _uiState.value.bitrate
        )
    }

    fun stopLiveStreaming() {
        GlassesManage.stopLiveStreaming()
    }


    fun switchFunction(mode: AppFunctionMode) {
        _uiState.update { it.copy(currentFunction = mode) }
        stopLocalPlay()
    }

    fun toggleLocalPlay() {
        if (_uiState.value.isPlayingLocal) {
            stopLocalPlay()
        } else {
            startLocalPlay()
        }
    }

    private fun startLocalPlay() {
        val url = _uiState.value.rtspUrl
        try {
            val media = Media(libVlc, Uri.parse(url))
            // 针对低延迟的关键配置
            media.addOption(":network-caching=300")
            media.addOption(":clock-jitter=0")
            media.addOption(":clock-synchro=0")

            vlcPlayer?.media = media
            vlcPlayer?.play()
            media.release()

            _uiState.update { it.copy(isPlayingLocal = true) }
        } catch (e: Exception) {
        }

    }

    fun toggleDouyinPush() {
        if (_uiState.value.isPushingToDouyin) {
            stopDouyinPush()
        } else {
            startDouyinPush()
        }
    }

    private fun stopLocalPlay() {
        vlcPlayer?.stop()
        _uiState.update { it.copy(isPlayingLocal = false) }
    }

    fun initDySdk(activity: Activity) {
//        repository.initDouyinSdk(activity)
    }


    private fun startDouyinPush() {

    }

    private fun stopDouyinPush() {

    }

//    fun clearError() {
//        _uiState.update { it.copy(errorMessage = null) }
//    }

    override fun onCleared() {
        super.onCleared()
    }

}