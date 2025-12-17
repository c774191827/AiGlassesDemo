package com.lw.top.lib_core.data.local.database

import android.content.Context
import com.lw.top.lib_core.data.local.dao.AiAssistantDao
import com.lw.top.lib_core.data.local.dao.MediaFilesDao
import com.lw.top.lib_core.data.local.dao.TranslationDao
import com.lw.top.lib_core.data.local.dao.UsersDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUsersDao(appDatabase: AppDatabase): UsersDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideMediaFilesDao(appDatabase: AppDatabase): MediaFilesDao {
        return appDatabase.mediaFilesDao()
    }

    @Provides
    fun provideAiAssistantDao(appDatabase: AppDatabase): AiAssistantDao {
        return appDatabase.aiAssistantDao()
    }

    @Provides
    fun provideTranslationDao(database: AppDatabase): TranslationDao {
        return database.translationDao()
    }
}