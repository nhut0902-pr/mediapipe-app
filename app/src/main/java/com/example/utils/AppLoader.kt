package com.example.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.Intent
import com.example.model.AppInfo

object AppLoader {
    fun getInstalledApps(context: Context, lockedPackages: Set<String>): List<AppInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        // Query list of launcher apps
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        val appList = mutableListOf<AppInfo>()
        val packageNameSet = mutableSetOf<String>() // Avoid duplicates
        
        for (resolveInfo in resolveInfos) {
            val packageName = resolveInfo.activityInfo.packageName
            
            // Skip our own AppLock app to avoid self-lockout
            if (packageName == context.packageName) continue
            
            if (packageNameSet.contains(packageName)) continue
            packageNameSet.add(packageName)

            val appName = resolveInfo.loadLabel(packageManager).toString()
            val appInfo = resolveInfo.activityInfo.applicationInfo
            
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            
            val isLocked = lockedPackages.contains(packageName)
            appList.add(
                AppInfo(
                    appName = appName,
                    packageName = packageName,
                    isLocked = isLocked,
                    isSystemApp = isSystemApp
                )
            )
        }
        
        return appList.sortedWith(compareBy({ !it.isLocked }, { it.appName.lowercase() }))
    }
}
