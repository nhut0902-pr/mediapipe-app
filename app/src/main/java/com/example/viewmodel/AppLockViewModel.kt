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
    private val repository = AppLockRepository(database.lockedAppDao, database.securityLogDao)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _isOverallActive = MutableStateFlow(SecurityUtils.isAppLockOverallActive(application))
    val isOverallActive: StateFlow<Boolean> = _isOverallActive.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(SecurityUtils.isBiometricEnabled(application))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _lockDelaySeconds = MutableStateFlow(SecurityUtils.getLockDelaySeconds(application))
    val lockDelaySeconds: StateFlow<Int> = _lockDelaySeconds.asStateFlow()

    fun setLockDelaySeconds(seconds: Int) {
        SecurityUtils.setLockDelaySeconds(getApplication(), seconds)
        _lockDelaySeconds.value = seconds
    }

    private val _appIconDisguise = MutableStateFlow(SecurityUtils.getAppIconDisguise(application))
    val appIconDisguise: StateFlow<String> = _appIconDisguise.asStateFlow()

    fun setAppIconDisguise(style: String) {
        SecurityUtils.setAppIconDisguise(getApplication(), style)
        _appIconDisguise.value = style
    }

    private val _isScheduleEnabled = MutableStateFlow(SecurityUtils.isScheduleEnabled(application))
    val isScheduleEnabled: StateFlow<Boolean> = _isScheduleEnabled.asStateFlow()

    private val _scheduleStartHour = MutableStateFlow(SecurityUtils.getScheduleStartHour(application))
    val scheduleStartHour: StateFlow<Int> = _scheduleStartHour.asStateFlow()

    private val _scheduleStartMinute = MutableStateFlow(SecurityUtils.getScheduleStartMinute(application))
    val scheduleStartMinute: StateFlow<Int> = _scheduleStartMinute.asStateFlow()

    private val _scheduleEndHour = MutableStateFlow(SecurityUtils.getScheduleEndHour(application))
    val scheduleEndHour: StateFlow<Int> = _scheduleEndHour.asStateFlow()

    private val _scheduleEndMinute = MutableStateFlow(SecurityUtils.getScheduleEndMinute(application))
    val scheduleEndMinute: StateFlow<Int> = _scheduleEndMinute.asStateFlow()

    fun setScheduleEnabled(enabled: Boolean) {
        SecurityUtils.setScheduleEnabled(getApplication(), enabled)
        _isScheduleEnabled.value = enabled
    }

    fun setScheduleStartTime(hour: Int, minute: Int) {
        SecurityUtils.setScheduleStartHour(getApplication(), hour)
        SecurityUtils.setScheduleStartMinute(getApplication(), minute)
        _scheduleStartHour.value = hour
        _scheduleStartMinute.value = minute
    }

    fun setScheduleEndTime(hour: Int, minute: Int) {
        SecurityUtils.setScheduleEndHour(getApplication(), hour)
        SecurityUtils.setScheduleEndMinute(getApplication(), minute)
        _scheduleEndHour.value = hour
        _scheduleEndMinute.value = minute
    }

    enum class AppFilter { ALL, LOCKED, UNLOCKED }

    private val _currentFilter = MutableStateFlow(AppFilter.ALL)
    val currentFilter: StateFlow<AppFilter> = _currentFilter.asStateFlow()

    fun setFilter(filter: AppFilter) {
        _currentFilter.value = filter
    }

    private val _isIntruderEnabled = MutableStateFlow(SecurityUtils.isIntruderEnabled(application))
    val isIntruderEnabled: StateFlow<Boolean> = _isIntruderEnabled.asStateFlow()

    fun setIntruderEnabled(enabled: Boolean) {
        SecurityUtils.setIntruderEnabled(getApplication(), enabled)
        _isIntruderEnabled.value = enabled
    }

    private val _isRandomKeypadEnabled = MutableStateFlow(SecurityUtils.isRandomKeypadEnabled(application))
    val isRandomKeypadEnabled: StateFlow<Boolean> = _isRandomKeypadEnabled.asStateFlow()

    fun setRandomKeypadEnabled(enabled: Boolean) {
        SecurityUtils.setRandomKeypadEnabled(getApplication(), enabled)
        _isRandomKeypadEnabled.value = enabled
    }

    private val _isFakeCrashEnabled = MutableStateFlow(SecurityUtils.isFakeCrashEnabled(application))
    val isFakeCrashEnabled: StateFlow<Boolean> = _isFakeCrashEnabled.asStateFlow()

    fun setFakeCrashEnabled(enabled: Boolean) {
        SecurityUtils.setFakeCrashEnabled(getApplication(), enabled)
        _isFakeCrashEnabled.value = enabled
    }

    private val _securityQuestion = MutableStateFlow(SecurityUtils.getSecurityQuestion(application))
    val securityQuestion: StateFlow<String> = _securityQuestion.asStateFlow()

    private val _securityAnswer = MutableStateFlow(SecurityUtils.getSecurityAnswer(application))
    val securityAnswer: StateFlow<String> = _securityAnswer.asStateFlow()

    fun setSecurityQuestionAndAnswer(question: String, answer: String) {
        SecurityUtils.setSecurityQuestionAndAnswer(getApplication(), question, answer)
        _securityQuestion.value = question
        _securityAnswer.value = answer.trim().lowercase()
    }

    // Expose security logs as a state flow auto-updated
    val securityLogs: StateFlow<List<com.example.model.SecurityLog>> = repository.allSecurityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined state for filtered apps list based on search and selected filter tab
    val filteredApps: StateFlow<List<AppInfo>> = combine(
        _installedApps,
        _searchQuery,
        _currentFilter
    ) { apps, query, filter ->
        val searched = if (query.isBlank()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
        when (filter) {
            AppFilter.ALL -> searched
            AppFilter.LOCKED -> searched.filter { it.isLocked }
            AppFilter.UNLOCKED -> searched.filter { !it.isLocked }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Locked apps count
    val lockedAppsCount: StateFlow<Int> = repository.allLockedApps
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _downloadedUtilities = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val downloadedUtilities: StateFlow<Map<String, Boolean>> = _downloadedUtilities.asStateFlow()

    private val utilityKeys = listOf(
        "calendar", "calculator", "notes", "stopwatch", "flashlight",
        "password", "qrcode", "heartrate", "multiplatform", "chatbot"
    )

    fun loadDownloadedUtilities() {
        val context = getApplication<Application>()
        val map = utilityKeys.associateWith { key ->
            SecurityUtils.isUtilityDownloaded(context, key)
        }
        _downloadedUtilities.value = map
    }

    fun setUtilityDownloaded(utilityId: String, status: Boolean) {
        val context = getApplication<Application>()
        SecurityUtils.setUtilityDownloaded(context, utilityId, status)
        loadDownloadedUtilities()
    }

    init {
        loadDownloadedUtilities()
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

    fun clearAllSecurityLogs() {
        viewModelScope.launch {
            repository.clearSecurityLogs()
        }
    }

    fun deleteSecurityLog(id: Int) {
        viewModelScope.launch {
            repository.deleteSecurityLog(id)
        }
    }
}
