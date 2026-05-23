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

class LockActivity : FragmentActivity() {

    private var lockedPackage: String = ""
    private var appLabel: String = ""
    private var appIcon: Drawable? = null

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

        // Auto trigger biometric scan if enabled and available
        if (SecurityUtils.isBiometricEnabled(this) && BiometricHelper.isBiometricAvailable(this)) {
            triggerBiometricPrompt()
        }
    }

    private fun triggerBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    AppLockManager.unlockedInSession.add(lockedPackage)
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user canceled or clicked PIN fallback, just display the fallback keypad
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
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
    var pinValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val handlePinInput: (String) -> Unit = { digit ->
        if (pinValue.length < 4) {
            errorMessage = ""
            pinValue += digit
            if (pinValue.length == 4) {
                // Verify Pin entered securely
                if (SecurityUtils.verifyPasscode(context, pinValue)) {
                    onUnlockSuccess()
                } else {
                    errorMessage = "Mã PIN không đúng, vui lòng thử lại!"
                    pinValue = ""
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
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9")
            )

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
                            // Since we have triggerBiometricPrompt inside LockActivity, we let users tap this button
                            // to launch biometric popup scanner.
                            (context as? LockActivity)?.let {
                                val executor = ContextCompat.getMainExecutor(it)
                                val prompt = BiometricPrompt(it, executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            super.onAuthenticationSucceeded(result)
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

                KeypadButton(text = "0", onClick = { handlePinInput("0") })

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

        // Bottom escape button cancels and returns the smartphone to Home Screen
        TextButton(onClick = onCancel) {
            Text(
                text = "Thoát ra màn hình chính",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
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
