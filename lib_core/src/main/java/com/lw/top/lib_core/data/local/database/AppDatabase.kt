package com.lw.top.lib_core.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lw.top.lib_core.data.local.dao.MediaFilesDao
import com.lw.top.lib_core.data.local.dao.UsersDao
import com.lw.top.lib_core.data.local.entity.MediaFilesEntity
import com.lw.top.lib_core.data.local.entity.UserEntity

@Database(entities = [UserEntity::class, MediaFilesEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UsersDao
    abstract fun mediaFilesDao(): MediaFilesDao
    companion object {
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // User表新增age字段
//                database.execSQL("ALTER TABLE users ADD COLUMN age INTEGER NOT NULL DEFAULT 0")
//            }
//        }
//
//        val MIGRATION_3_4 = object : Migration(3, 4) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // User表新增age字段
//                database.execSQL("ALTER TABLE users ADD COLUMN avatar TEXT NOT NULL DEFAULT ''")
//            }
//        }

        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
//                .addMigrations(MIGRATION_1_2,MIGRATION_3_4)
                .build()
        }
    }


}