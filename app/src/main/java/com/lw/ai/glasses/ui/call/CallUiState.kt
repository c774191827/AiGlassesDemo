package com.lw.ai.glasses.ui.call

data class CallUiState(
    val isInCall: Boolean = false,
    val callMode: CallMode = CallMode.VIDEO,
    val selectedLanguage: String = "英语",
    val translationLogs: List<TranslationMessage> = emptyList(),
    val isRemoteVideoReady: Boolean = false,
    val isLoading: Boolean = false
)
