package com.quietlogic.allisok.data.local.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "allisok_database"
            ).addMigrations(AppDatabase.MIGRATION_1_2)
                .build().also { database ->
                instance = database
            }
        }
    }
}