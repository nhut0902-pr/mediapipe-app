package com.example.utils

import android.content.Context
import java.security.MessageDigest

object SecurityUtils {
    private const val PREFS_NAME = "applock_secure_prefs"
    private const val KEY_PASSWORD_HASH = "passcode_sha256"
    private const val KEY_BIOMETRIC_STATUS = "biometric_enabled"
    private const val KEY_LOCK_STATE_ACTIVE = "applock_overall_active"

    fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { String.format("%02x", it) }
        } catch (e: Exception) {
            ""
        }
    }

    fun isPasscodeSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_PASSWORD_HASH)
    }

    fun setPasscode(context: Context, pin: String): Boolean {
        if (pin.length < 4) return false
        val hashed = sha256(pin)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.edit().putString(KEY_PASSWORD_HASH, hashed).commit()
    }

    fun verifyPasscode(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        return sha256(pin) == savedHash
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_STATUS, true)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_STATUS, enabled).apply()
    }

    fun isAppLockOverallActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOCK_STATE_ACTIVE, true)
    }

    fun setAppLockOverallActive(context: Context, active: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_LOCK_STATE_ACTIVE, active).apply()
    }
}
