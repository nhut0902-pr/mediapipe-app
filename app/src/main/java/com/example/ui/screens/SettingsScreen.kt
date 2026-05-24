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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var showIntruderLogDialog by remember { mutableStateOf(false) }

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

    val isFakeCrashEnabled by viewModel.isFakeCrashEnabled.collectAsStateWithLifecycle()
    val securityQuestion by viewModel.securityQuestion.collectAsStateWithLifecycle()
    val securityAnswer by viewModel.securityAnswer.collectAsStateWithLifecycle()
    val securityLogs by viewModel.securityLogs.collectAsStateWithLifecycle()

    var showQuestionDialog by remember { mutableStateOf(false) }
    var tempQuestion by remember { mutableStateOf("") }
    var tempAnswer by remember { mutableStateOf("") }
    var questionError by remember { mutableStateOf("") }

    var showLogsDialog by remember { mutableStateOf(false) }

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
                Column {
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
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsToggleRow(
                        title = "Khóa ngụy trang Lỗi giả",
                        subtitle = "Hiện lỗi 'Ứng dụng đã dừng' giả khi mở app, nhấp 'Báo cáo' 3 lần để mở",
                        icon = Icons.Default.Warning,
                        checked = isFakeCrashEnabled,
                        onCheckedChange = { viewModel.setFakeCrashEnabled(it) }
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
                Column {
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
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableRow(
                        title = "Nhật ký truy cập bảo mật",
                        subtitle = "Xem lịch sử mở khóa và các lần đột nhập sai",
                        icon = Icons.Default.History,
                        onClick = {
                            showLogsDialog = true
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableRow(
                        title = "Câu hỏi bảo mật khôi phục",
                        subtitle = "Thiết lập câu hỏi cá nhân khôi phục mã PIN khi quên",
                        icon = Icons.Default.QuestionAnswer,
                        onClick = {
                            tempQuestion = if (securityQuestion.isNotEmpty()) securityQuestion else "Bạn sinh ra ở thành phố nào?"
                            tempAnswer = securityAnswer
                            questionError = ""
                            showQuestionDialog = true
                        }
                    )
                }
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

            // Section 3.5: Tính năng nâng cao
            Text(
                "TÍNH NĂNG NÂNG CAO",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    val isRandomKeypadEnabled by viewModel.isRandomKeypadEnabled.collectAsStateWithLifecycle()
                    SettingsToggleRow(
                        title = "Xáo trộn bàn phím PIN",
                        subtitle = "Xáo trộn vị trí quay số mỗi lần mở khóa để chống nhìn lén",
                        icon = Icons.Default.Shuffle,
                        checked = isRandomKeypadEnabled,
                        onCheckedChange = { viewModel.setRandomKeypadEnabled(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    val isIntruderEnabled by viewModel.isIntruderEnabled.collectAsStateWithLifecycle()
                    SettingsToggleRow(
                        title = "Cảnh báo kẻ đột nhập",
                        subtitle = "Hiển thị cảnh báo đột nhập trực quan khi nhập sai mã PIN quá 3 lần",
                        icon = Icons.Default.Announcement,
                        checked = isIntruderEnabled,
                        onCheckedChange = { viewModel.setIntruderEnabled(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    val securityLogs by viewModel.securityLogs.collectAsStateWithLifecycle()
                    val failedAttemptsLogs = remember(securityLogs) { securityLogs.filter { it.logType == "FAILED" } }
                    SettingsClickableRow(
                        title = "Nhật ký đột nhập",
                        subtitle = "Hiện có ${failedAttemptsLogs.size} lần nhập sai mã khóa",
                        icon = Icons.Default.Warning,
                        onClick = { showIntruderLogDialog = true }
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
                            "Phiên bản: 1.5.5",
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
                        UpdateBulletItem("Bộ lọc Phân loại Ứng dụng (v1.5.5)", "Thêm dải Tabs bộ lọc nhanh trạng thái Đã khóa / Chưa khóa trực quan.")
                        UpdateBulletItem("Xáo trộn Bàn phím PIN (v1.5.5)", "Nháo vị trí các phím số ngẫu nhiên ngăn rò rỉ mã số khi gõ khóa.")
                        UpdateBulletItem("Cảnh báo Đột nhập (v1.5.5)", "Báo cáo màu đỏ trực quan khi nhập sai PIN > 3 lần kèm ghi nhớ nhật ký.")
                        UpdateBulletItem("Ngụy trang Biểu tượng ứng dụng", "Thay đổi tên và biểu tượng hiển thị ngoài màn hình chính tránh bị dòm ngó.")
                        UpdateBulletItem("Hẹn giờ khóa ứng dụng", "Thiết lập khung giờ khóa/mở bảo vệ tự động linh hoạt.")
                        UpdateBulletItem("Vá lỗi & Tăng độ tin cậy", "Khắc phục cơ chế đổi icon hoạt động ổn định trên mọi hệ điều hành.")
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

    // Configure Security Question Dialog
    if (showQuestionDialog) {
        val questionsList = listOf(
            "Bạn sinh ra ở thành phố nào?",
            "Tên con vật cưng đầu tiên của bạn là gì?",
            "Món ăn yêu thích nhất của bạn là gì?",
            "Tên trường học tiểu học của bạn là gì?",
            "Màu sắc may mắn yêu thích của bạn là gì?"
        )
        var expandedQuestionDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showQuestionDialog = false },
            title = { Text("Thiết lập Câu hỏi bảo mật", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Dùng câu hỏi bảo mật để lấy lại mã PIN trong trường hợp bạn quên.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Spinner-like selector for Question
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedQuestionDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tempQuestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Chọn câu hỏi",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandedQuestionDropdown,
                            onDismissRequest = { expandedQuestionDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            questionsList.forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(q) },
                                    onClick = {
                                        tempQuestion = q
                                        expandedQuestionDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tempAnswer,
                        onValueChange = {
                            tempAnswer = it
                            questionError = ""
                        },
                        label = { Text("Câu trả lời bảo mật") },
                        placeholder = { Text("Nhập câu trả lời...") },
                        singleLine = true,
                        isError = questionError.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (questionError.isNotEmpty()) {
                        Text(
                            text = questionError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempAnswer.trim().isEmpty()) {
                            questionError = "Câu trả lời không được để trống!"
                        } else {
                            viewModel.setSecurityQuestionAndAnswer(tempQuestion, tempAnswer)
                            Toast.makeText(context, "Lưu câu hỏi bảo mật thành công!", Toast.LENGTH_SHORT).show()
                            showQuestionDialog = false
                        }
                    }
                ) {
                    Text("Lưu cấu hình")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuestionDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Security Access Logs Viewer Dialog
    if (showLogsDialog) {
        val coroutineScope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { showLogsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nhật ký bảo mật", fontWeight = FontWeight.Bold)
                    if (securityLogs.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val dbInstance = com.example.database.AppDatabase.getInstance(context.applicationContext)
                                    withContext(Dispatchers.IO) {
                                        dbInstance.securityLogDao.clearAllLogs()
                                    }
                                    Toast.makeText(context, "Đã xóa toàn bộ nhật ký!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Xóa hết", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    if (securityLogs.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryToggleOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Chưa có bất kỳ nhật ký bảo mật nào.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(securityLogs.size) { index ->
                                val log = securityLogs[index]
                                val isSuccess = log.logType.startsWith("SUCCESS")
                                val statusText = when {
                                    log.logType == "SUCCESS_RECOVERY" -> "Khôi phục qua câu hỏi thành công"
                                    isSuccess -> "Mở khóa thành công"
                                    log.logType.contains("Sai vân tay") -> "Sai vân tay đột nhập"
                                    else -> "Mở khóa lỗi PIN"
                                }
                                val statusColor = if (isSuccess) {
                                    Color(0xFF2E7D32)
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                                
                                val formattedTime = remember(log.timestamp) {
                                    val sdf = java.text.SimpleDateFormat("dd/MM HH:mm:ss", java.util.Locale.getDefault())
                                    sdf.format(java.util.Date(log.timestamp))
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                                    contentDescription = null,
                                                    tint = statusColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = log.appName,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = statusText,
                                                color = statusColor,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (!isSuccess && !log.attemptedPin.isNullOrBlank()) {
                                                Text(
                                                    text = "PIN đã nhập: ${log.attemptedPin}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = formattedTime,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLogsDialog = false }) {
                    Text("Đóng")
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

    if (showIntruderLogDialog) {
        val securityLogs by viewModel.securityLogs.collectAsStateWithLifecycle()
        val failedLogs = remember(securityLogs) { securityLogs.filter { it.logType == "FAILED" } }

        AlertDialog(
            onDismissRequest = { showIntruderLogDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lịch sử Đột nhập", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (failedLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Chưa phát hiện xâm nhập nào!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        failedLogs.reversed().forEach { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = log.appName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        
                                        val date = java.util.Date(log.timestamp)
                                        val format = java.text.SimpleDateFormat("dd/MM HH:mm:ss", java.util.Locale.getDefault())
                                        Text(
                                            text = format.format(date),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Mã số đã gõ: [${log.attemptedPin}]",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showIntruderLogDialog = false }
                ) {
                    Text("Đóng")
                }
            },
            dismissButton = {
                if (failedLogs.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            viewModel.clearAllSecurityLogs()
                            showIntruderLogDialog = false
                            Toast.makeText(context, "Đã xóa sạch nhật ký đột nhập!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Xóa nhật ký", color = MaterialTheme.colorScheme.error)
                    }
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
