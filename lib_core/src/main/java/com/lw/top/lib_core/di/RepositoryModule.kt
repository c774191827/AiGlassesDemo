package com.lw.top.lib_core.di

import com.lw.top.lib_core.data.local.dao.AiAssistantDao
import com.lw.top.lib_core.data.local.dao.MediaFilesDao
import com.lw.top.lib_core.data.local.dao.TranslationDao
import com.lw.top.lib_core.data.repository.AiAssistantRepository
import com.lw.top.lib_core.data.repository.PhotoRepository
import com.lw.top.lib_core.data.repository.TranslationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun providePhotoRepository(mediaFilesDao: MediaFilesDao): PhotoRepository {
        return PhotoRepository(mediaFilesDao)
    }

    @Provides
    fun provideAiAssistantRepository(aiAssistantDao: AiAssistantDao): AiAssistantRepository {
        return AiAssistantRepository(aiAssistantDao)
    }

    @Provides
    fun provideTranslationRepository(translationDao: TranslationDao): TranslationRepository {
        return TranslationRepository(translationDao)
    }
}