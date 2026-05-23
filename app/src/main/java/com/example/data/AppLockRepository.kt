package com.example.data

import com.example.database.LockedAppDao
import com.example.model.LockedApp
import kotlinx.coroutines.flow.Flow

class AppLockRepository(private val lockedAppDao: LockedAppDao) {
    val allLockedApps: Flow<List<LockedApp>> = lockedAppDao.getAllLockedAppsFlow()

    suspend fun getAllLockedAppsList(): List<LockedApp> {
        return lockedAppDao.getAllLockedApps()
    }

    suspend fun lockApp(packageName: String, appName: String) {
        lockedAppDao.insertLockedApp(LockedApp(packageName, appName, isLocked = true))
    }

    suspend fun unlockApp(packageName: String) {
        lockedAppDao.deleteLockedApp(packageName)
    }

    suspend fun isAppLocked(packageName: String): Boolean {
        return lockedAppDao.isAppLocked(packageName)
    }
}
