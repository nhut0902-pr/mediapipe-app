package com.example.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators

object BiometricHelper {
    fun canAuthenticate(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL)
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val result = canAuthenticate(context)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    fun getBiometricErrorMessage(context: Context): String {
        return when (canAuthenticate(context)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Thiết bị hỗ trợ sinh trắc học"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Thiết bị không có phần cứng sinh trắc học"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Phần cứng sinh trắc học hiện không khả dụng"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Chưa đăng ký vân tay/khuôn mặt trên thiết bị"
            else -> "Sinh trắc học không khả dụng"
        }
    }
}
