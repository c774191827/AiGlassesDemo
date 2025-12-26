package com.lw.ai.glasses.ui.translate

import com.lw.top.lib_core.data.local.entity.TranslationEntity
import kotlinx.serialization.Serializable

/**
 * 翻译模式枚举
 */
enum class TranslationMode {
    REAL_TIME, // 实时翻译 (通常用于听讲座、看电影)
    DIALOGUE   // 对话翻译 (通常用于面对面交谈)
}

data class TranslatorUiState(
    val messages: List<TranslationEntity> = emptyList(),
    val isRecording: Boolean = false,
    val recordingLanguage: String = "",
    val currentAmplitude: Float = 0f,
    val allLanguages: List<Language> = emptyList(),
    val srcLang: Language? = null,
    val targetLang: Language? = null,
    val currentMode: TranslationMode = TranslationMode.REAL_TIME, // 默认实时翻译
    val error: String? = null
)

@Serializable
data class Language(
    val name: String,
    val nameEn: String,
    val langType: Int,
    val code: String
)
