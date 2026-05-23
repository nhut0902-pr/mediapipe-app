package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.utils.DownloadProgressState
import com.example.viewmodel.UpdateUiState
import com.example.viewmodel.UpdateViewModel

@Composable
fun UpdateDialog(
    viewModel: UpdateViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.updateUiState.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    val currentUpdate = (uiState as? UpdateUiState.HasUpdate)?.updateInfo ?: return

    val isForced = currentUpdate.forceUpdate
    val isDownloading = downloadProgress is DownloadProgressState.Downloading
    val isSuccess = downloadProgress is DownloadProgressState.Success

    Dialog(
        onDismissRequest = {
            if (!isForced && !isDownloading) {
                viewModel.dismissUpdate()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isForced && !isDownloading,
            dismissOnClickOutside = !isForced && !isDownloading
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Cập nhật ứng dụng",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Bản Cập Nhật Mới Có Sẵn!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Version Info
                Text(
                    text = "Phiên bản: v${currentUpdate.versionName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Changelog Box
                Text(
                    text = "Nhật ký thay đổi:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = currentUpdate.changelog,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Download Progress or Status UI
                when (val progress = downloadProgress) {
                    is DownloadProgressState.Downloading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { progress.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Đang tải: ${progress.progress.toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    is DownloadProgressState.Success -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tải thành công! Vui lòng cài đặt.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.installDownloadedApk(progress.file) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cài đặt ngay")
                            }
                        }
                    }
                    is DownloadProgressState.Failed -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Lỗi tải xuống: ${progress.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { viewModel.resetDownloadState() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Thử lại")
                                }
                                if (!isForced) {
                                    Button(
                                        onClick = {
                                            viewModel.resetDownloadState()
                                            viewModel.dismissUpdate()
                                        },
                                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Bỏ qua")
                                    }
                                }
                            }
                        }
                    }
                    DownloadProgressState.Idle -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (!isForced) {
                                OutlinedButton(
                                    onClick = { viewModel.dismissUpdate() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Để sau")
                                }
                            }
                            Button(
                                onClick = { viewModel.startDownloading(currentUpdate.apkUrl) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cập nhật")
                            }
                        }
                    }
                }
            }
        }
    }
}
