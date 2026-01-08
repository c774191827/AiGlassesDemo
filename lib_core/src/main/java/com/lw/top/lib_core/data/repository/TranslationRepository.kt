package com.lw.top.lib_core.data.repository

import com.lw.top.lib_core.data.local.dao.TranslationDao
import com.lw.top.lib_core.data.local.entity.TranslationMessageEntity
import com.lw.top.lib_core.data.local.entity.TranslationSessionEntity
import com.lw.top.lib_core.data.local.entity.TranslationWithMessages
import com.lw.top.lib_core.data.repository.base.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TranslationRepository @Inject constructor(
    private val translationDao: TranslationDao
) : BaseRepository() {

    fun getAllSessionsWithMessagesFlow(): Flow<List<TranslationWithMessages>> {
        return translationDao.getAllSessionsWithMessagesFlow()
    }

    suspend fun insertSession(session: TranslationSessionEntity) {
        withContext(Dispatchers.IO) {
            translationDao.insertSession(session)
        }
    }

    suspend fun insertMessage(message: TranslationMessageEntity) {
        withContext(Dispatchers.IO) {
            translationDao.insertMessage(message)
        }
    }

    suspend fun getMessageById(requestId: String, messageId: String): TranslationMessageEntity? {
        return withContext(Dispatchers.IO) {
            translationDao.getMessageById(requestId, messageId)
        }
    }

    suspend fun clearAllTranslations() {
        withContext(Dispatchers.IO) {
            translationDao.clearAll()
        }
    }

    suspend fun deleteMessageById(requestId: String, messageId: String) {
        withContext(Dispatchers.IO) {
            translationDao.deleteMessageById(requestId, messageId)
        }
    }
}
