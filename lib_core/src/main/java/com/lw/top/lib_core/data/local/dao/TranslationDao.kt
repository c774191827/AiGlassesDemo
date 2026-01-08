package com.lw.top.lib_core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lw.top.lib_core.data.local.entity.TranslationMessageEntity
import com.lw.top.lib_core.data.local.entity.TranslationSessionEntity
import com.lw.top.lib_core.data.local.entity.TranslationWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: TranslationSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: TranslationMessageEntity)

    @Transaction
    @Query("SELECT * FROM translation_sessions ORDER BY timestamp DESC")
    fun getAllSessionsWithMessagesFlow(): Flow<List<TranslationWithMessages>>

    @Transaction
    @Query("SELECT * FROM translation_sessions WHERE requestId = :requestId LIMIT 1")
    suspend fun getSessionWithMessages(requestId: String): TranslationWithMessages?

    @Query("SELECT * FROM translation_messages WHERE requestId = :requestId AND messageId = :messageId LIMIT 1")
    suspend fun getMessageById(requestId: String, messageId: String): TranslationMessageEntity?

    @Query("DELETE FROM translation_sessions")
    suspend fun clearAll()

    @Query("DELETE FROM translation_messages WHERE requestId = :requestId AND messageId = :messageId")
    suspend fun deleteMessageById(requestId: String, messageId: String)
}
