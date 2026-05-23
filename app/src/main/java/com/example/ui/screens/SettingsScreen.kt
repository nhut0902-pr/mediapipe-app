package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.AppLockAccessibilityService
import com.example.utils.BiometricHelper
import com.example.utils.PermissionHelper
import com.example.utils.SecurityUtils
import com.example.viewmodel.AppLockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AppLockViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isOverallActive by viewModel.isOverallActive.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    
    var showChangePinDialog by remember { mutableStateOf(false) }
    var tempNewPin by remember { mutableStateOf("") }
    var tempConfirmPin by remember { mutableStateOf("") }
    var pinDialogError by remember { mutableStateOf("") }

    // Read permissions state
    var isAccessibilityEnabled by remember {
        mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context, AppLockAccessibilityService::class.java))
    }
    var isOverlayEnabled by remember {
        mutableStateOf(PermissionHelper.isOverlayPermissionGranted(context))
    }

    SideEffect {
        isAccessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context, AppLockAccessibilityService::class.java)
        isOverlayEnabled = PermissionHelper.isOverlayPermissionGranted(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt cấu hình", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Thao tác chính
            Text(
                "CHẾ ĐỘ HOẠT ĐỘNG",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsToggleRow(
                        title = "Kích hoạt AppLock",
                        subtitle = "Bật/Tắt chế độ bảo vệ chung toàn hệ thống",
                        icon = Icons.Default.Security,
                        checked = isOverallActive,
                        onCheckedChange = { viewModel.setOverallActive(it) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsToggleRow(
                        title = "Xác thực sinh trắc học",
                        subtitle = "Ưu tiên quét vân tay hoặc nhận diện khuôn mặt",
                        icon = Icons.Default.Fingerprint,
                        checked = isBiometricEnabled,
                        onCheckedChange = { viewModel.setBiometricEnabled(it) },
                        enabled = BiometricHelper.isBiometricAvailable(context)
                    )
                }
            }

            // Section 2: Tài khoản & Passcode
            Text(
                "BẢO MẬT & MẬT MÃ",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                SettingsClickableRow(
                    title = "Thay đổi mã PIN",
                    subtitle = "Cập nhật mã số khóa fallback truy cập",
                    icon = Icons.Default.VpnKey,
                    onClick = {
                        tempNewPin = ""
                        tempConfirmPin = ""
                        pinDialogError = ""
                        showChangePinDialog = true
                    }
                )
            }

            // Section 3: Quyền hệ thống kiểm tra
            Text(
                "QUYỀN TRUY CẬP VÀ THIẾT BỊ",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsStatusRow(
                        title = "Dịch vụ Hỗ trợ tiếp cận (Accessibility)",
                        subtitle = "Dùng theo dõi trạng thái đóng mở ứng dụng",
                        icon = Icons.Default.SettingsAccessibility,
                        statusText = if (isAccessibilityEnabled) "Đã cấp quyền" else "Chưa được cấp",
                        statusColor = if (isAccessibilityEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsStatusRow(
                        title = "Vẽ đè màn hình (Overlay permission)",
                        subtitle = "Hiển thị màn hình khóa che khuất app",
                        icon = Icons.Default.Tv,
                        statusText = if (isOverlayEnabled) "Đã cấp quyền" else "Chưa được cấp",
                        statusColor = if (isOverlayEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsStatusRow(
                        title = "Khả năng sinh trắc học thiết bị",
                        subtitle = BiometricHelper.getBiometricErrorMessage(context),
                        icon = Icons.Default.Info,
                        statusText = if (BiometricHelper.isBiometricAvailable(context)) "Sẵn sàng" else "Không hỗ trợ",
                        statusColor = if (BiometricHelper.isBiometricAvailable(context)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        onClick = {
                            // Biometric compatibility info dialog can trigger
                        }
                    )
                }
            }

            // Section 4: Chế độ giao diện & Tối ưu hóa đa nhiệm
            Text(
                "THÔNG TIN PHIÊN BẢN",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Chế độ Tối ưu (Dark Mode): Hoạt động tự động theo cấu hình hệ thống.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cơ chế Chống Bypass: Tự động khóa lại ứng dụng ngay lập tức khi người sử dụng chuyển qua đa nhiệm sang app khác, ngăn chặn hoàn toàn việc rò rỉ nội dung bảo mật.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    // Passcode Editing Dialog
    if (showChangePinDialog) {
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Đổi mã PIN bảo mật") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempNewPin,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) tempNewPin = it },
                        label = { Text("Nhập mã PIN mới (4-6 chữ số)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tempConfirmPin,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) tempConfirmPin = it },
                        label = { Text("Xác nhận mã PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    if (pinDialogError.isNotEmpty()) {
                        Text(
                            text = pinDialogError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempNewPin.length < 4) {
                            pinDialogError = "Mã PIN mới phải từ 4 đến 6 chữ số!"
                        } else if (tempNewPin != tempConfirmPin) {
                            pinDialogError = "Mã xác nhận PIN không khớp!"
                        } else {
                            val saved = viewModel.setPasscode(tempNewPin)
                            if (saved) {
                                Toast.makeText(context, "Thay đổi PIN thành công!", Toast.LENGTH_SHORT).show()
                                showChangePinDialog = false
                            } else {
                                pinDialogError = "Lỗi lưu PIN mới."
                            }
                        }
                    }
                ) {
                    Text("Lưu thay đổi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Hủy bỏ")
                }
            }
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun SettingsStatusRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusText: String,
    statusColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Text(
            text = statusText,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}
