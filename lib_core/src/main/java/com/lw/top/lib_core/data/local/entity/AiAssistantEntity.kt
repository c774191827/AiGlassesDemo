package com.lw.top.lib_core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ai_assistant")
data class AiAssistantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val answer: String,
    val questionType: String,
    val answerType: String,
    val timestamp: Long = System.currentTimeMillis()
)
