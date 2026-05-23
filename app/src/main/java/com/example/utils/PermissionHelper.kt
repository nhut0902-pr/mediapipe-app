package com.example.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.os.Build

object PermissionHelper {
    // Check if Overlay (Draw Over Other Apps) permission is granted
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    // Check if our specific Accessibility service is enabled
    fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponentName = "${context.packageName}/${serviceClass.name}"
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServicesSetting)
        while (splitter.hasNext()) {
            val enabledService = splitter.next()
            if (enabledService.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
