package com.lw.top.lib_core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.lw.top.lib_core.data.local.entity.MediaFilesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFilesDao : BaseDao<MediaFilesEntity> {

    @Query("SELECT * FROM media_files ORDER BY createdAt DESC")
    fun getAllFilesFlow(): Flow<List<MediaFilesEntity>>

    @Query("DELETE FROM media_files") // 直接写出表名
    suspend fun clearAll()

}