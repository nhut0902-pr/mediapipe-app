package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.LockedApp
import com.example.model.SecurityLog

@Database(entities = [LockedApp::class, SecurityLog::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val lockedAppDao: LockedAppDao
    abstract val securityLogDao: SecurityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "applock_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
