package com.lw.top.lib_core.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lw.top.lib_core.data.local.dao.AiAssistantDao
import com.lw.top.lib_core.data.local.dao.MediaFilesDao
import com.lw.top.lib_core.data.local.dao.UsersDao
import com.lw.top.lib_core.data.local.entity.AiAssistantEntity
import com.lw.top.lib_core.data.local.entity.MediaFilesEntity
import com.lw.top.lib_core.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, MediaFilesEntity::class, AiAssistantEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UsersDao
    abstract fun mediaFilesDao(): MediaFilesDao
    abstract fun aiAssistantDao(): AiAssistantDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ai_assistant` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `question` TEXT NOT NULL, `answer` TEXT NOT NULL, `questionType` TEXT NOT NULL, `answerType` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }
//        val MIGRATION_3_4 = object : Migration(3, 4) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // User表新增age字段
//                database.execSQL("ALTER TABLE users ADD COLUMN avatar TEXT NOT NULL DEFAULT ''")
//            }
//        }

        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }


}