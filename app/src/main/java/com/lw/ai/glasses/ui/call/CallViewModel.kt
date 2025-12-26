package com.lw.ai.glasses.ui.call

import BaseViewModel
import android.view.TextureView
import androidx.lifecycle.viewModelScope
import com.fission.wear.glasses.sdk.GlassesManage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 模拟通话模式
 */
enum class CallMode {
    VIDEO, AUDIO
}

/**
 * 翻译记录数据类
 */
data class TranslationMessage(
    val sender: String,
    val originalText: String,
    val translatedText: String,
    val isFromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class CallViewModel : BaseViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    // 固定参数 (仅供开发测试)
    private val APP_ID = 954308550L
    private val APP_SIGN = "7ad72cbc05d3a4bf46ccfeb00652c538c82df4e6e73d47c4b02a9752212a0486"
    private val ROOM_ID = "Roomfaf06fe385d9"
    private val USER_ID = "user-1766112240110"

    fun setCallMode(mode: CallMode) {
        _uiState.update { it.copy(callMode = mode) }
    }

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(selectedLanguage = lang) }
    }

    /**
     * 开始通话
     */
    fun startCall(localView: TextureView? = null, remoteView: TextureView? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 调用 SDK 层的通用实现
            val isVideo = _uiState.value.callMode == CallMode.VIDEO
            
            GlassesManage.startCall(
                appID = APP_ID,
                appSign = APP_SIGN,
                roomID = ROOM_ID,
                userID = USER_ID,
                isVideo = isVideo,
                localView = localView,
                remoteView = remoteView
            )

            // 模拟延迟后进入通话状态 (实际结果应由 Zego 回调触发，此处先模拟)
            delay(1000) 
            _uiState.update { 
                it.copy(
                    isInCall = true,
                    isLoading = false
                )
            }
            
            simulateChat()
            // 假设远端流在 2 秒后到达
            delay(2000)
            _uiState.update { it.copy(isRemoteVideoReady = true) }
        }
    }

    /**
     * 结束通话
     */
    fun endCall() {
        GlassesManage.endCall()
        _uiState.update { 
            it.copy(
                isInCall = false,
                isRemoteVideoReady = false,
                translationLogs = emptyList()
            )
        }
    }

    private fun simulateChat() {
        viewModelScope.launch {
            var toggle = true
            while (_uiState.value.isInCall) {
                delay(4000)
                val newMsg = TranslationMessage(
                    sender = if (toggle) "我" else "对方",
                    originalText = "Wait a moment...",
                    translatedText = if (toggle) "等我一下，我在看数据。" else "好的，没问题。",
                    isFromMe = toggle
                )
                _uiState.update { it.copy(translationLogs = it.translationLogs + newMsg) }
                toggle = !toggle
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        endCall()
    }
}
