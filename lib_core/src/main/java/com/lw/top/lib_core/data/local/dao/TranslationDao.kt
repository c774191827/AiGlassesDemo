package com.lw.top.lib_core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.lw.top.lib_core.data.local.entity.TranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao : BaseDao<TranslationEntity> {

    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllTranslationsFlow(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    suspend fun getAllTranslations(): List<TranslationEntity>

    @Query("SELECT * FROM translation_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TranslationEntity?

    @Query("DELETE FROM translation_history")
    suspend fun clearAll()
}