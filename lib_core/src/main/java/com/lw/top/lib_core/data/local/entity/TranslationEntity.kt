package com.lw.top.lib_core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "translation_history")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = System.currentTimeMillis(),
    val originalText: String,
    val translatedText: String,
    val audioPath: String?,
    val isUser: Boolean,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)