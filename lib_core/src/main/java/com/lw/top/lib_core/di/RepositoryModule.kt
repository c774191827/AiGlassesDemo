package com.lw.top.lib_core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.lw.top.lib_core.data.datastore.dataStore
import com.lw.top.lib_core.data.local.dao.MediaFilesDao
import com.lw.top.lib_core.data.repository.PhotoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun providePhotoRepository(mediaFilesDao: MediaFilesDao): PhotoRepository {
        return PhotoRepository(mediaFilesDao)
    }

}