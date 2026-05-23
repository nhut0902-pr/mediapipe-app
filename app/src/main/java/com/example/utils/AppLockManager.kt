package com.example.utils

import kotlinx.coroutines.flow.MutableStateFlow

object AppLockManager {
    // Keeps track of the currently running service status
    val isServiceRunning = MutableStateFlow(false)
    
    // Quick cache of locked packages list (synchronized)
    val lockedPackagesCache = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    
    // Set of packages unlocked during the current session
    val unlockedInSession = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    
    // The last package we checked 
    var lastActivePackage: String? = null
}
