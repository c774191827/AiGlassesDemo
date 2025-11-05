package com.lw.top.lib_core.data.repository

import com.lw.top.lib_core.data.local.dao.MediaFilesDao
import com.lw.top.lib_core.data.local.entity.MediaFilesEntity
import com.lw.top.lib_core.data.repository.base.BaseRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext


class PhotoRepository @Inject constructor(
    private val mediaFilesDao: MediaFilesDao
): BaseRepository() {

    fun getSyncedPhotosFlow(): Flow<List<MediaFilesEntity>> {
        return mediaFilesDao.getAllFilesFlow()
    }

    suspend fun insertPhoto(entity: MediaFilesEntity) {
        withContext(Dispatchers.IO) {
            mediaFilesDao.insert(entity)
        }
    }

    suspend fun insertAllPhotos(entities: List<MediaFilesEntity>) {
        withContext(Dispatchers.IO) {
            mediaFilesDao.insertAll(entities)
        }
    }

    suspend fun deletePhoto(entity: MediaFilesEntity) {
        withContext(Dispatchers.IO) {
            mediaFilesDao.delete(entity)
        }
    }

    suspend fun clearAllPhotos() {
        withContext(Dispatchers.IO) {
            mediaFilesDao.clearAll()
        }
    }

}