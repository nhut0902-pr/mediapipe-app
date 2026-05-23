package com.example.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.utils.BiometricHelper
import com.example.viewmodel.AppLockViewModel

@Composable
fun SplashSetupScreen(
    viewModel: AppLockViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val isNewUser = remember { !viewModel.isPasscodeSet() }
    var step by remember { mutableStateOf(if (isNewUser) "SETUP_PIN" else "LOGIN_PIN") }
    
    var pinValue by remember { mutableStateOf("") }
    var confirmPinValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Auto prompt biometric when logging in if biometric is active
    LaunchedEffect(step) {
        if (step == "LOGIN_PIN" && viewModel.isBiometricEnabled.value && BiometricHelper.isBiometricAvailable(context)) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onNavigateToDashboard()
                    }
                })
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Mở khóa AppLock")
                .setSubtitle("Xác thực vân tay/khuôn mặt để mở trang quản trị")
                .setNegativeButtonText("Hủy")
                .build()
            biometricPrompt.authenticate(promptInfo)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App header logo branding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "APPLOCK SECURITY",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Khóa ứng dụng riêng tư bằng sinh trắc học",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        // Action Screen Body
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            when (step) {
                "SETUP_PIN" -> {
                    Text(
                        text = "Thiết lập mã PIN quản trị",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Nhập mã số ít nhất 4 chữ số giúp bạn quản lý cấu hình khóa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = pinValue,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) pinValue = it },
                        label = { Text("Nhập mã PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (pinValue.length < 4) {
                                errorMessage = "Mã PIN phải có từ 4 đến 6 chữ số!"
                            } else {
                                errorMessage = ""
                                step = "CONFIRM_PIN"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tiếp tục")
                    }
                }
                "CONFIRM_PIN" -> {
                    Text(
                        text = "Xác nhận mã PIN quản trị",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = confirmPinValue,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) confirmPinValue = it },
                        label = { Text("Nhập lại mã PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (pinValue == confirmPinValue) {
                                val success = viewModel.setPasscode(pinValue)
                                if (success) {
                                    Toast.makeText(context, "Thiết lập thành công!", Toast.LENGTH_SHORT).show()
                                    onNavigateToDashboard()
                                } else {
                                    errorMessage = "Lỗi lưu trữ passcode"
                                }
                            } else {
                                errorMessage = "Mã xác nhận không khớp!"
                                confirmPinValue = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hoàn tất thiết lập")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(onClick = {
                        step = "SETUP_PIN"
                        pinValue = ""
                        confirmPinValue = ""
                        errorMessage = ""
                    }) {
                        Text("Quay lại")
                    }
                }
                "LOGIN_PIN" -> {
                    Text(
                        text = "Mở khóa chế độ Admin",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = pinValue,
                        onValueChange = { 
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                pinValue = it
                                errorMessage = ""
                                if (it.length >= 4 && viewModel.verifyPasscode(it)) {
                                    onNavigateToDashboard()
                                }
                            }
                        },
                        label = { Text("Nhập mã PIN đăng nhập") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (viewModel.isBiometricEnabled.value && BiometricHelper.isBiometricAvailable(context)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .clickable {
                                    val activity = context as? FragmentActivity ?: return@clickable
                                    val executor = ContextCompat.getMainExecutor(activity)
                                    val biometricPrompt = BiometricPrompt(activity, executor,
                                        object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                super.onAuthenticationSucceeded(result)
                                                onNavigateToDashboard()
                                            }
                                        })
                                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Mở khóa AppLock")
                                        .setSubtitle("Xác thực vân tay/khuôn mặt để mở trang quản trị")
                                        .setNegativeButtonText("Hủy")
                                        .build()
                                    biometricPrompt.authenticate(promptInfo)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Dùng vân tay",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Footer brand tagline
        Text(
            text = "Phiên bản bảo mật v1.0 • offline-first",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}
