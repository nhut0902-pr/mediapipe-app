package com.example.model

data class AppInfo(
    val appName: String,
    val packageName: String,
    val isLocked: Boolean,
    val isSystemApp: Boolean
)
