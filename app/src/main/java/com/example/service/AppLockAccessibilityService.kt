package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.LockActivity
import com.example.database.AppDatabase
import com.example.utils.AppLockManager
import com.example.utils.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppLockAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastPackageName: String? = null

    override fun onCreate() {
        super.onCreate()
        AppLockManager.isServiceRunning.value = true
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Prefetch locked app list packages straight to memory cache in AppLockManager
        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val list = db.lockedAppDao.getAllLockedApps()
            AppLockManager.lockedPackagesCache.clear()
            AppLockManager.lockedPackagesCache.addAll(list.map { it.packageName })
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        
        // Skip launchers, systemUI, settings overlay UI, and our own app
        if (packageName == this.packageName) {
            return
        }

        if (isSystemLauncherOrSystemUI(packageName)) {
            return
        }

        // Detect multitasking switch: if they leave a locked app, remove it from session
        // This ensures "Chống bypass khi chuyển đa nhiệm" - if they switch back, they MUST unlock again!
        val lastPkg = lastPackageName
        if (lastPkg != null && lastPkg != packageName) {
            if (AppLockManager.lockedPackagesCache.contains(lastPkg)) {
                AppLockManager.unlockedInSession.remove(lastPkg)
            }
        }
        
        lastPackageName = packageName
        AppLockManager.lastActivePackage = packageName

        // Check overall applock state
        if (!SecurityUtils.isAppLockOverallActive(applicationContext)) {
            return
        }

        // Check cache if app package clicked is locked
        if (AppLockManager.lockedPackagesCache.contains(packageName)) {
            // Check session to optimize prompt frequency
            if (AppLockManager.unlockedInSession.contains(packageName)) {
                return // Safe, already unlocked in this session
            }

            // Lock application! Launch full screen LockActivity overlay
            val intent = Intent(this, LockActivity::class.java).apply {
                putExtra("LOCKED_PACKAGE", packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
    }

    private fun isSystemLauncherOrSystemUI(packageName: String): Boolean {
        return packageName == "com.android.systemui" ||
                packageName == "com.google.android.apps.nexuslauncher" ||
                packageName == "com.android.launcher3" ||
                packageName.contains("launcher")
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        AppLockManager.isServiceRunning.value = false
    }
}
