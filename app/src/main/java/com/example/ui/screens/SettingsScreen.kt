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
    
    val isScheduleEnabled by viewModel.isScheduleEnabled.collectAsStateWithLifecycle()
    val scheduleStartHour by viewModel.scheduleStartHour.collectAsStateWithLifecycle()
    val scheduleStartMinute by viewModel.scheduleStartMinute.collectAsStateWithLifecycle()
    val scheduleEndHour by viewModel.scheduleEndHour.collectAsStateWithLifecycle()
    val scheduleEndMinute by viewModel.scheduleEndMinute.collectAsStateWithLifecycle()

    val appIconDisguise by viewModel.appIconDisguise.collectAsStateWithLifecycle()
    var showDisguiseDialog by remember { mutableStateOf(false) }

    var showStartTimerDialog by remember { mutableStateOf(false) }
    var showEndTimerDialog by remember { mutableStateOf(false) }
    var tempStartHour by remember { mutableStateOf(22) }
    var tempStartMin by remember { mutableStateOf(0) }
    var tempEndHour by remember { mutableStateOf(6) }
    var tempEndMin by remember { mutableStateOf(0) }

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

            // Section 1.5: Hẹn giờ bảo mật
            Text(
                "HẸN GIỜ BẢO MẬT",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsToggleRow(
                        title = "Kích hoạt Hẹn giờ khóa",
                        subtitle = "Chỉ bật bảo vệ trong khoảng thời gian hẹn giờ cụ thể",
                        icon = Icons.Default.Schedule,
                        checked = isScheduleEnabled,
                        onCheckedChange = { viewModel.setScheduleEnabled(it) }
                    )
                    
                    if (isScheduleEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        
                        SettingsClickableRow(
                            title = "Thời gian Bắt đầu",
                            subtitle = String.format("%02d:%02d", scheduleStartHour, scheduleStartMinute),
                            icon = Icons.Default.PlayArrow,
                            onClick = {
                                tempStartHour = scheduleStartHour
                                tempStartMin = scheduleStartMinute
                                showStartTimerDialog = true
                            }
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        
                        SettingsClickableRow(
                            title = "Thời gian Kết thúc",
                            subtitle = String.format("%02d:%02d", scheduleEndHour, scheduleEndMinute),
                            icon = Icons.Default.Stop,
                            onClick = {
                                tempEndHour = scheduleEndHour
                                tempEndMin = scheduleEndMinute
                                showEndTimerDialog = true
                            }
                        )
                    }
                }
            }

            // Section 1.8: Ngụy trang ứng dụng
            Text(
                "NGỤY TRANG ỨNG DỤNG",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                val disguiseText = when (appIconDisguise) {
                    "CALCULATOR" -> "Máy tính"
                    "WEATHER" -> "Thời tiết"
                    "CALENDAR" -> "Lịch"
                    else -> "Mặc định (AppLock)"
                }
                SettingsClickableRow(
                    title = "Ngụy trang Biểu tượng",
                    subtitle = "Cách hiển thị ngoài launcher: $disguiseText",
                    icon = Icons.Default.VisibilityOff,
                    onClick = { showDisguiseDialog = true }
                )
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Phiên bản: 1.5.4",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "MỚI NHẤT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "THÔNG TIN CẬP NHẬT CÓ GÌ MỚI:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        UpdateBulletItem("Ngụy trang biểu tượng ứng dụng", "Thay đổi tên và biểu tượng hiển thị ngoài màn hình chính để đánh lạc hướng (Máy tính, Thời tiết, Lịch bản đồ...)")
                        UpdateBulletItem("Hẹn giờ khóa ứng dụng", "Thiết lập khung giờ đóng/mở bảo vệ tự động (Bật/tắt trong Cài đặt phía trên).")
                        UpdateBulletItem("Tích hợp Widget màn hình chính", "Tiện ích ngoài màn hình chính hiển thị trạng thái và mở khóa nhanh.")
                        UpdateBulletItem("Nhật ký truy cập bảo mật", "Ghi lại chi tiết lịch sử đóng/mở ứng dụng để kiểm soát an toàn.")
                        UpdateBulletItem("Tối ưu hóa đa nhiệm v2", "Cơ chế chống Bypass chặn đứng xâm nhập qua cử chỉ chuyển trang vuốt.")
                        UpdateBulletItem("Vá lỗi & Tăng độ tin cậy", "Sửa đổi phản hồi vân tay & gia tăng tính mượt mà của màn che.")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

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

    // Start Timer Picker Dialog
    if (showStartTimerDialog) {
        AlertDialog(
            onDismissRequest = { showStartTimerDialog = false },
            title = { Text("Chọn thời gian Bắt đầu", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Giờ", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledIconButton(
                                onClick = { tempStartHour = (tempStartHour + 1) % 24 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, null)
                            }
                            Text(
                                text = String.format("%02d", tempStartHour),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            FilledIconButton(
                                onClick = { tempStartHour = if (tempStartHour - 1 < 0) 23 else tempStartHour - 1 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, null)
                            }
                        }
                        
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Minute Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Phút", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledIconButton(
                                onClick = { tempStartMin = (tempStartMin + 5) % 60 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, null)
                            }
                            Text(
                                text = String.format("%02d", tempStartMin),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            FilledIconButton(
                                onClick = { tempStartMin = if (tempStartMin - 5 < 0) 55 else tempStartMin - 5 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, null)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setScheduleStartTime(tempStartHour, tempStartMin)
                        showStartTimerDialog = false
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimerDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // End Timer Picker Dialog
    if (showEndTimerDialog) {
        AlertDialog(
            onDismissRequest = { showEndTimerDialog = false },
            title = { Text("Chọn thời gian Kết thúc", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Giờ", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledIconButton(
                                onClick = { tempEndHour = (tempEndHour + 1) % 24 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, null)
                            }
                            Text(
                                text = String.format("%02d", tempEndHour),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            FilledIconButton(
                                onClick = { tempEndHour = if (tempEndHour - 1 < 0) 23 else tempEndHour - 1 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, null)
                            }
                        }
                        
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Minute Column
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Phút", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledIconButton(
                                onClick = { tempEndMin = (tempEndMin + 5) % 60 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, null)
                            }
                            Text(
                                text = String.format("%02d", tempEndMin),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            FilledIconButton(
                                onClick = { tempEndMin = if (tempEndMin - 5 < 0) 55 else tempEndMin - 5 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, null)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setScheduleEndTime(tempEndHour, tempEndMin)
                        showEndTimerDialog = false
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimerDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // App Icon Disguise Picker Dialog
    if (showDisguiseDialog) {
        var tempSelectedDisguise by remember { mutableStateOf(appIconDisguise) }
        AlertDialog(
            onDismissRequest = { showDisguiseDialog = false },
            title = { Text("Chọn biểu tượng ngụy trang", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Khi thay đổi biểu tượng ngụy trang, tên và icon ứng dụng ngoài màn hình chính sẽ thay đổi tương ứng.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    DisguiseOptionRow(
                        title = "Mặc định (AppLock)",
                        desc = "Biểu tượng bảo mật hình tấm khiên an toàn",
                        iconRes = com.example.R.drawable.img_app_icon,
                        selected = tempSelectedDisguise == "DEFAULT",
                        onSelect = { tempSelectedDisguise = "DEFAULT" }
                    )

                    DisguiseOptionRow(
                        title = "Máy tính (Calculator)",
                        desc = "Ngụy trang dưới dạng máy tính bỏ túi",
                        iconRes = com.example.R.drawable.ic_disguise_calculator_vector,
                        selected = tempSelectedDisguise == "CALCULATOR",
                        onSelect = { tempSelectedDisguise = "CALCULATOR" }
                    )

                    DisguiseOptionRow(
                        title = "Thời tiết (Weather)",
                        desc = "Ngụy trang dưới dạng ứng dụng thời tiết",
                        iconRes = com.example.R.drawable.ic_disguise_weather_vector,
                        selected = tempSelectedDisguise == "WEATHER",
                        onSelect = { tempSelectedDisguise = "WEATHER" }
                    )

                    DisguiseOptionRow(
                        title = "Lịch (Calendar)",
                        desc = "Ngụy trang dưới dạng lịch biểu tiện ích",
                        iconRes = com.example.R.drawable.ic_disguise_calendar_vector,
                        selected = tempSelectedDisguise == "CALENDAR",
                        onSelect = { tempSelectedDisguise = "CALENDAR" }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setAppIconDisguise(tempSelectedDisguise)
                        showDisguiseDialog = false
                        Toast.makeText(context, "Thay đổi Biểu tượng hoàn tất! Launcher sẽ cần khoảng vài giây để cập nhật lại giao diện hoàn toàn.", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisguiseDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun DisguiseOptionRow(
    title: String,
    desc: String,
    iconRes: Int,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        
        // Icon preview base
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
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

@Composable
fun UpdateBulletItem(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            "• ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}
