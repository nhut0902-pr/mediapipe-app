package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.UpdateInfo
import com.example.data.UpdateRepository
import com.example.utils.DownloadHelper
import com.example.utils.DownloadProgressState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface UpdateUiState {
    object Idle : UpdateUiState
    object Checking : UpdateUiState
    data class HasUpdate(val updateInfo: UpdateInfo) : UpdateUiState
    object NoUpdate : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UpdateRepository()

    private val _updateUiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateUiState: StateFlow<UpdateUiState> = _updateUiState.asStateFlow()

    private val _downloadProgress = MutableStateFlow<DownloadProgressState>(DownloadProgressState.Idle)
    val downloadProgress: StateFlow<DownloadProgressState> = _downloadProgress.asStateFlow()

    private var activeDownloadId: Long? = null

    fun checkForUpdates(customPath: String? = null) {
        _updateUiState.value = UpdateUiState.Checking
        viewModelScope.launch {
            repository.checkUpdate(customPath)
                .onSuccess { updateInfo ->
                    val currentVersionCode = BuildConfig.VERSION_CODE
                    if (updateInfo.versionCode > currentVersionCode) {
                        _updateUiState.value = UpdateUiState.HasUpdate(updateInfo)
                    } else {
                        _updateUiState.value = UpdateUiState.NoUpdate
                    }
                }
                .onFailure { error ->
                    _updateUiState.value = UpdateUiState.Error(error.localizedMessage ?: "Lỗi kết nối máy chủ")
                }
        }
    }

    fun startDownloading(apkUrl: String) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            try {
                val downloadId = DownloadHelper.startDownload(context, apkUrl)
                activeDownloadId = downloadId
                _downloadProgress.value = DownloadProgressState.Downloading(0f)
                
                DownloadHelper.queryProgress(context, downloadId).collect { state ->
                    _downloadProgress.value = state
                }
            } catch (e: Exception) {
                _downloadProgress.value = DownloadProgressState.Failed(e.localizedMessage ?: "Không thể bắt đầu tải xuống")
            }
        }
    }

    fun installDownloadedApk(file: File) {
        DownloadHelper.installApk(getApplication(), file)
    }

    fun dismissUpdate() {
        _updateUiState.value = UpdateUiState.Idle
    }

    fun resetDownloadState() {
        _downloadProgress.value = DownloadProgressState.Idle
    }
}
