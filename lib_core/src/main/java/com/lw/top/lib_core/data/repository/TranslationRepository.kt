package com.lw.top.lib_core.data.repository

import com.lw.top.lib_core.data.local.dao.TranslationDao
import com.lw.top.lib_core.data.local.entity.TranslationEntity
import com.lw.top.lib_core.data.repository.base.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TranslationRepository @Inject constructor(
    private val translationDao: TranslationDao
) : BaseRepository() {

    fun getAllTranslationsFlow(): Flow<List<TranslationEntity>> {
        return translationDao.getAllTranslationsFlow()
    }

    suspend fun getAllTranslations(): List<TranslationEntity> {
        return translationDao.getAllTranslations()
    }

    suspend fun insertTranslationAndGetId(entity: TranslationEntity): Long {
        return withContext(Dispatchers.IO) {
            translationDao.insert(entity)
        }
    }

    suspend fun insertTranslation(entity: TranslationEntity) {
        withContext(Dispatchers.IO) {
            translationDao.insert(entity)
        }
    }

    suspend fun getTranslationById(id: Long): TranslationEntity? {
        return withContext(Dispatchers.IO) {
            translationDao.getById(id)
        }
    }

    suspend fun deleteTranslation(entity: TranslationEntity) {
        withContext(Dispatchers.IO) {
            translationDao.delete(entity)
        }
    }

    suspend fun clearAllTranslations() {
        withContext(Dispatchers.IO) {
            translationDao.clearAll()
        }
    }

}