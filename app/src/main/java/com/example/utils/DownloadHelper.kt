package com.example.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

sealed interface DownloadProgressState {
    object Idle : DownloadProgressState
    data class Downloading(val progress: Float) : DownloadProgressState
    data class Success(val file: File) : DownloadProgressState
    data class Failed(val message: String) : DownloadProgressState
}

object DownloadHelper {

    private const val FILE_NAME = "AppLock-Update.apk"

    fun getApkFile(): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            FILE_NAME
        )
    }

    fun startDownload(context: Context, apkUrl: String): Long {
        val file = getApkFile()
        if (file.exists()) {
            file.delete()
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Đang tải bản cập nhật")
            .setDescription("Vui lòng chờ giây lát...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FILE_NAME)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        return downloadManager.enqueue(request)
    }

    fun queryProgress(context: Context, downloadId: Long): Flow<DownloadProgressState> = flow {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var isDownloading = true
        
        while (isDownloading) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            
            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                
                if (statusIndex != -1 && bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                    val status = cursor.getInt(statusIndex)
                    val downloaded = cursor.getLong(bytesDownloadedIndex)
                    val total = cursor.getLong(bytesTotalIndex)
                    
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val apkFile = getApkFile()
                            emit(DownloadProgressState.Success(apkFile))
                            isDownloading = false
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            val reason = if (reasonIndex != -1) cursor.getInt(reasonIndex) else -1
                            emit(DownloadProgressState.Failed("Tải thất bại (Mã lỗi: $reason)"))
                            isDownloading = false
                        }
                        DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING -> {
                            val progress = if (total > 0) {
                                (downloaded.toFloat() / total.toFloat()) * 100f
                            } else {
                                0f
                            }
                            emit(DownloadProgressState.Downloading(progress))
                        }
                    }
                }
            } else {
                emit(DownloadProgressState.Failed("Không thể truy vấn trạng thái tải."))
                isDownloading = false
            }
            cursor?.close()
            
            if (isDownloading) {
                delay(300)
            }
        }
    }

    fun installApk(context: Context, file: File) {
        if (!file.exists()) {
            Toast.makeText(context, "File cài đặt không tồn tại!", Toast.LENGTH_LONG).show()
            return
        }

        // Install compatibility security check for Oreo+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Toast.makeText(context, "Vui lòng cho phép cài ứng dụng không xác định rồi mở lại!", Toast.LENGTH_LONG).show()
                return
            }
        }

        try {
            val authority = "${context.packageName}.provider"
            val apkUri = FileProvider.getUriForFile(context, authority, file)
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Lỗi khi gọi trình cài đặt: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
