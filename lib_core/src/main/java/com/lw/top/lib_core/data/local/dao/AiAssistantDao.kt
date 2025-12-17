package com.lw.top.lib_core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.lw.top.lib_core.data.local.entity.AiAssistantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiAssistantDao: BaseDao<AiAssistantEntity> {

    @Query("SELECT * FROM ai_assistant ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<AiAssistantEntity>>

    @Query("SELECT * FROM ai_assistant ORDER BY timestamp DESC")
    suspend fun getAllMessages(): List<AiAssistantEntity>

    @Query("SELECT * FROM ai_assistant WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AiAssistantEntity?

    @Query("DELETE FROM ai_assistant")
    suspend fun clearAll()
}