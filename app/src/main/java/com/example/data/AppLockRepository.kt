package com.example.data

import com.example.database.LockedAppDao
import com.example.database.SecurityLogDao
import com.example.model.LockedApp
import com.example.model.SecurityLog
import kotlinx.coroutines.flow.Flow

class AppLockRepository(
    private val lockedAppDao: LockedAppDao,
    private val securityLogDao: SecurityLogDao
) {
    val allLockedApps: Flow<List<LockedApp>> = lockedAppDao.getAllLockedAppsFlow()
    val allSecurityLogs: Flow<List<SecurityLog>> = securityLogDao.getAllLogsFlow()

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

    suspend fun insertSecurityLog(packageName: String, appName: String, logType: String, attemptedPin: String = "") {
        securityLogDao.insertLog(
            SecurityLog(
                packageName = packageName,
                appName = appName,
                logType = logType,
                attemptedPin = attemptedPin
            )
        )
    }

    suspend fun clearSecurityLogs() {
        securityLogDao.clearAllLogs()
    }

    suspend fun deleteSecurityLog(id: Int) {
        securityLogDao.deleteLog(id)
    }
}
