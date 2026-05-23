package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppLockRepository
import com.example.database.AppDatabase
import com.example.model.AppInfo
import com.example.utils.AppLoader
import com.example.utils.AppLockManager
import com.example.utils.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppLockViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = AppLockRepository(database.lockedAppDao)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _isOverallActive = MutableStateFlow(SecurityUtils.isAppLockOverallActive(application))
    val isOverallActive: StateFlow<Boolean> = _isOverallActive.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(SecurityUtils.isBiometricEnabled(application))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    // Combined state for filtered apps list
    val filteredApps: StateFlow<List<AppInfo>> = combine(
        _installedApps,
        _searchQuery
    ) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Locked apps count
    val lockedAppsCount: StateFlow<Int> = repository.allLockedApps
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Sync cache and load lists
        viewModelScope.launch {
            repository.allLockedApps.collect { lockedList ->
                val set = lockedList.map { it.packageName }.toSet()
                AppLockManager.lockedPackagesCache.clear()
                AppLockManager.lockedPackagesCache.addAll(set)
                loadInstalledApps(set)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            val lockedList = repository.getAllLockedAppsList()
            val set = lockedList.map { it.packageName }.toSet()
            loadInstalledApps(set)
        }
    }

    private suspend fun loadInstalledApps(lockedSet: Set<String>) {
        _isLoadingApps.value = true
        withContext(Dispatchers.IO) {
            val apps = AppLoader.getInstalledApps(getApplication(), lockedSet)
            _installedApps.value = apps
            _isLoadingApps.value = false
        }
    }

    fun toggleAppLock(packageName: String, appName: String, shouldLock: Boolean) {
        viewModelScope.launch {
            if (shouldLock) {
                repository.lockApp(packageName, appName)
                AppLockManager.lockedPackagesCache.add(packageName)
            } else {
                repository.unlockApp(packageName)
                AppLockManager.lockedPackagesCache.remove(packageName)
                AppLockManager.unlockedInSession.remove(packageName)
            }
        }
    }

    fun setOverallActive(active: Boolean) {
        SecurityUtils.setAppLockOverallActive(getApplication(), active)
        _isOverallActive.value = active
    }

    fun setBiometricEnabled(enabled: Boolean) {
        SecurityUtils.setBiometricEnabled(getApplication(), enabled)
        _isBiometricEnabled.value = enabled
    }

    fun isPasscodeSet(): Boolean {
        return SecurityUtils.isPasscodeSet(getApplication())
    }

    fun setPasscode(pin: String): Boolean {
        return SecurityUtils.setPasscode(getApplication(), pin)
    }

    fun verifyPasscode(pin: String): Boolean {
        return SecurityUtils.verifyPasscode(getApplication(), pin)
    }
}
