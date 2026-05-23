package com.example.database

import androidx.room.*
import com.example.model.LockedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps ORDER BY appName ASC")
    fun getAllLockedAppsFlow(): Flow<List<LockedApp>>

    @Query("SELECT * FROM locked_apps")
    suspend fun getAllLockedApps(): List<LockedApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockedApp(app: LockedApp)

    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteLockedApp(packageName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM locked_apps WHERE packageName = :packageName LIMIT 1)")
    suspend fun isAppLocked(packageName: String): Boolean
}
