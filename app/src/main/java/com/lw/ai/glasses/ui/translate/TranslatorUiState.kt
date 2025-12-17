package com.lw.ai.glasses.ui.translate

import com.lw.top.lib_core.data.local.entity.TranslationEntity
import kotlinx.serialization.Serializable

data class TranslatorUiState(
    val messages: List<TranslationEntity> = emptyList(),
    val isRecording: Boolean = false,
    val recordingLanguage: String = "",
    val currentAmplitude: Float = 0f,
    val allLanguages: List<Language> = emptyList(),
    val srcLang: Language? = null,
    val targetLang: Language? = null,
    val error: String? = null
)

@Serializable
data class Language(
    val name: String,
    val nameEn: String,
    val langType: Int,
    val code: String
)
