package com.lw.ai.glasses.ui.assistant

import com.lw.top.lib_core.data.local.entity.AiAssistantEntity

data class AiAssistantUiState(
    val messages: List<AiAssistantEntity> = emptyList(),
    val streamingMessageId: Long? = null
)

data class StreamState(
    val displayedQuestionLength: Int = 0,
    val displayedAnswerLength: Int = 0
)
