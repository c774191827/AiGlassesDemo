package com.lw.top.lib_core.data.repository

import com.lw.top.lib_core.data.local.dao.AiAssistantDao
import com.lw.top.lib_core.data.local.entity.AiAssistantEntity
import com.lw.top.lib_core.data.repository.base.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AiAssistantRepository @Inject constructor(
    private val aiAssistantDao: AiAssistantDao
) : BaseRepository() {

    fun getAllMessagesFlow(): Flow<List<AiAssistantEntity>> {
        return aiAssistantDao.getAllMessagesFlow()
    }

    suspend fun getAllMessages(): List<AiAssistantEntity> {
        return aiAssistantDao.getAllMessages()
    }

    suspend fun insertMessageAndGetId(entity: AiAssistantEntity): Long {
        return withContext(Dispatchers.IO) {
            aiAssistantDao.insert(entity)
        }
    }

    suspend fun insertMessage(entity: AiAssistantEntity) {
        withContext(Dispatchers.IO) {
            aiAssistantDao.insert(entity)
        }
    }

    suspend fun getMessageById(id: Long): AiAssistantEntity? {
        return withContext(Dispatchers.IO) {
            aiAssistantDao.getById(id)
        }
    }

    suspend fun deleteMessage(entity: AiAssistantEntity) {
        withContext(Dispatchers.IO) {
            aiAssistantDao.delete(entity)
        }
    }

    suspend fun clearAllMessages() {
        withContext(Dispatchers.IO) {
            aiAssistantDao.clearAll()
        }
    }

}