package com.example

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.AppLockManager
import com.example.utils.BiometricHelper
import com.example.utils.SecurityUtils
import java.util.concurrent.Executor
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.lifecycle.coroutineScope

class LockActivity : FragmentActivity() {

    private var lockedPackage: String = ""
    private var appLabel: String = ""
    private var appIcon: Drawable? = null

    fun logSecurityEvent(logType: String, attemptedPin: String = "") {
        if (SecurityUtils.isIntruderEnabled(this)) {
            lifecycle.coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val db = com.example.database.AppDatabase.getInstance(applicationContext)
                    db.securityLogDao.insertLog(
                        com.example.model.SecurityLog(
                            packageName = lockedPackage,
                            appName = appLabel,
                            logType = logType,
                            attemptedPin = attemptedPin
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lockedPackage = intent.getStringExtra("LOCKED_PACKAGE") ?: ""
        if (lockedPackage.isEmpty()) {
            finish()
            return
        }

        // Load targeted application name and icon info
        val pm = packageManager
        try {
            val appInfo = pm.getApplicationInfo(lockedPackage, 0)
            appLabel = pm.getApplicationLabel(appInfo).toString()
            appIcon = pm.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            appLabel = "Ứng dụng bảo mật"
        }

        // Intercept back clicks -> bypass defense redirects directly to Android home screen
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                redirectHome()
            }
        })

        // Check if passcode is set on the app at all. If not, bypass security
        if (!SecurityUtils.isPasscodeSet(this)) {
            AppLockManager.unlockedInSession.add(lockedPackage)
            finish()
            return
        }

        setContent {
            MyApplicationTheme {
                LockScreenContent(
                    appName = appLabel,
                    packageName = lockedPackage,
                    appIcon = appIcon,
                    onUnlockSuccess = {
                        AppLockManager.unlockedInSession.add(lockedPackage)
                        finish()
                    },
                    onCancel = {
                        redirectHome()
                    }
                )
            }
        }

        // Auto trigger biometric scan if enabled, available, and fake crash is NOT active
        if (!SecurityUtils.isFakeCrashEnabled(this) && SecurityUtils.isBiometricEnabled(this) && BiometricHelper.isBiometricAvailable(this)) {
            triggerBiometricPrompt()
        }
    }

    fun triggerBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    logSecurityEvent("SUCCESS", "Sinh trắc học")
                    AppLockManager.unlockedInSession.add(lockedPackage)
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    logSecurityEvent("FAILED", "Sinh trắc học (Sai vân tay)")
                    Toast.makeText(this@LockActivity, "Xác thực thất bại", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Xác thực mở khóa $appLabel")
            .setSubtitle("Vui lòng quét vân tay hoặc khuôn mặt để tiếp tục")
            .setNegativeButtonText("Dùng mã PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun redirectHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
fun LockScreenContent(
    appName: String,
    packageName: String,
    appIcon: Drawable?,
    onUnlockSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { com.example.database.AppDatabase.getInstance(context.applicationContext) }
    val repo = remember { com.example.data.AppLockRepository(db.lockedAppDao, db.securityLogDao) }

    var pinValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var failedAttemptsCount by remember { mutableStateOf(0) }

    var showFakeCrash by remember { mutableStateOf(SecurityUtils.isFakeCrashEnabled(context)) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryAnswer by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf("") }

    LaunchedEffect(showFakeCrash) {
        if (!showFakeCrash) {
            // Auto trigger biometric scan if enabled and available
            if (SecurityUtils.isBiometricEnabled(context) && BiometricHelper.isBiometricAvailable(context)) {
                (context as? LockActivity)?.triggerBiometricPrompt()
            }
        }
    }

    if (showFakeCrash) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "Lỗi hệ thống",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Rất tiếc, ứng dụng $appName đã dừng đột ngột. Vui lòng thử lại sau.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancel()
                    }
                ) {
                    Text("Đóng")
                }
            },
            dismissButton = {
                var clickCount by remember { mutableIntStateOf(0) }
                TextButton(
                    onClick = {
                        clickCount++
                        if (clickCount >= 3) {
                            showFakeCrash = false
                            Toast.makeText(context, "Đã bỏ qua màn hình ngụy trang lỗi!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Gửi báo cáo sự cố thành công (${clickCount}/3)", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Báo cáo", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp),
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        )
    }

    if (showRecoveryDialog) {
        val question = remember { SecurityUtils.getSecurityQuestion(context) }
        val savedAnswer = remember { SecurityUtils.getSecurityAnswer(context) }
        
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = {
                Text(
                    text = "Khôi phục mật mã",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Câu hỏi bảo mật của bạn:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = question,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    OutlinedTextField(
                        value = recoveryAnswer,
                        onValueChange = {
                            recoveryAnswer = it
                            recoveryError = ""
                        },
                        placeholder = { Text("Nhập câu trả lời...") },
                        singleLine = true,
                        isError = recoveryError.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (recoveryError.isNotEmpty()) {
                        Text(
                            text = recoveryError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (savedAnswer.isEmpty()) {
                            recoveryError = "Bạn chưa thiết lập câu hỏi bảo mật trong Cài đặt!"
                        } else if (recoveryAnswer.trim().lowercase() == savedAnswer) {
                            showRecoveryDialog = false
                            Toast.makeText(context, "Xác thực thành công. Đang mở khóa!", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                repo.insertSecurityLog(packageName, appName, "SUCCESS_RECOVERY")
                            }
                            onUnlockSuccess()
                        } else {
                            recoveryError = "Câu trả lời nhập vào không chính xác!"
                        }
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    val isRandomKeypad = remember { SecurityUtils.isRandomKeypadEnabled(context) }
    val scrambledKeys = remember {
        val list = (0..9).map { it.toString() }.toMutableList()
        if (isRandomKeypad) {
            list.shuffle()
        }
        list
    }

    val handlePinInput: (String) -> Unit = { digit ->
        if (pinValue.length < 4) {
            errorMessage = ""
            pinValue += digit
            if (pinValue.length == 4) {
                // Verify Pin entered securely
                if (SecurityUtils.verifyPasscode(context, pinValue)) {
                    coroutineScope.launch {
                        repo.insertSecurityLog(packageName, appName, "SUCCESS")
                    }
                    onUnlockSuccess()
                } else {
                    failedAttemptsCount++
                    val attempted = pinValue
                    errorMessage = "Mã PIN không đúng, vui lòng thử lại!"
                    pinValue = ""
                    coroutineScope.launch {
                        repo.insertSecurityLog(packageName, appName, "FAILED", attempted)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App header containing metadata info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            if (appIcon != null) {
                val bitmap = remember(appIcon) {
                    appIcon.toBitmap(80, 80).asImageBitmap()
                }
                Image(
                    bitmap = bitmap,
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$appName đã bị khóa",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Bảo mật bằng AppLock sinh trắc học",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            if (SecurityUtils.isIntruderEnabled(context) && failedAttemptsCount >= 3) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CẢNH BÁO ĐỘT NHẬP: Phát hiện $failedAttemptsCount lần nhập mã PIN sai liên tục!",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Numeric Indicator Dots
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..4) {
                    val filled = pinValue.length >= i
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Numeric Keypad Board Layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = if (isRandomKeypad) {
                listOf(
                    scrambledKeys.subList(0, 3),
                    scrambledKeys.subList(3, 6),
                    scrambledKeys.subList(6, 9)
                )
            } else {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )
            }

            for (row in keys) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (digit in row) {
                        KeypadButton(text = digit, onClick = { handlePinInput(digit) })
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Biometrics Shortcut triggers biometric scanner instantly
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .clickable(enabled = BiometricHelper.isBiometricAvailable(context)) {
                            (context as? LockActivity)?.let {
                                val executor = ContextCompat.getMainExecutor(it)
                                val prompt = BiometricPrompt(it, executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            super.onAuthenticationSucceeded(result)
                                            coroutineScope.launch {
                                                repo.insertSecurityLog(packageName, appName, "SUCCESS")
                                            }
                                            onUnlockSuccess()
                                        }
                                        override fun onAuthenticationFailed() {
                                            super.onAuthenticationFailed()
                                        }
                                    })
                                val info = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Quét sinh trắc học")
                                    .setSubtitle("Xác thực mở khóa $appName")
                                    .setNegativeButtonText("Mã PIN")
                                    .build()
                                prompt.authenticate(info)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (BiometricHelper.isBiometricAvailable(context)) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Scan",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                val lastKey = if (isRandomKeypad) scrambledKeys[9] else "0"
                KeypadButton(text = lastKey, onClick = { handlePinInput(lastKey) })

                // Backspace button to delete typed elements
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (pinValue.isNotEmpty()) {
                                pinValue = pinValue.dropLast(1)
                                errorMessage = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Xóa chữ số",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Recovery & Cancel Actions Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { showRecoveryDialog = true }) {
                Text(
                    text = "Quên mật khẩu?",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            TextButton(onClick = onCancel) {
                Text(
                    text = "Thoát",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
