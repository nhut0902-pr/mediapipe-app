package com.example.utils

import android.content.Context
import java.security.MessageDigest

object SecurityUtils {
    private const val PREFS_NAME = "applock_secure_prefs"
    private const val KEY_PASSWORD_HASH = "passcode_sha256"
    private const val KEY_BIOMETRIC_STATUS = "biometric_enabled"
    private const val KEY_LOCK_STATE_ACTIVE = "applock_overall_active"
    private const val KEY_SCHEDULE_ENABLED = "schedule_enabled"
    private const val KEY_SCHEDULE_START_HOUR = "schedule_start_hour"
    private const val KEY_SCHEDULE_START_MINUTE = "schedule_start_minute"
    private const val KEY_SCHEDULE_END_HOUR = "schedule_end_hour"
    private const val KEY_SCHEDULE_END_MINUTE = "schedule_end_minute"

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

    fun getLockDelaySeconds(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("lock_delay_seconds", 0)
    }

    fun setLockDelaySeconds(context: Context, seconds: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("lock_delay_seconds", seconds).apply()
    }

    fun isScheduleEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)
    }

    fun setScheduleEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, enabled).apply()
    }

    fun getScheduleStartHour(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SCHEDULE_START_HOUR, 22)
    }

    fun setScheduleStartHour(context: Context, hour: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SCHEDULE_START_HOUR, hour).apply()
    }

    fun getScheduleStartMinute(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SCHEDULE_START_MINUTE, 0)
    }

    fun setScheduleStartMinute(context: Context, minute: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SCHEDULE_START_MINUTE, minute).apply()
    }

    fun getScheduleEndHour(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SCHEDULE_END_HOUR, 6)
    }

    fun setScheduleEndHour(context: Context, hour: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SCHEDULE_END_HOUR, hour).apply()
    }

    fun getScheduleEndMinute(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SCHEDULE_END_MINUTE, 0)
    }

    fun setScheduleEndMinute(context: Context, minute: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SCHEDULE_END_MINUTE, minute).apply()
    }

    fun isAppLockCurrentlyActive(context: Context): Boolean {
        // If overall active is false, it's completely off
        if (!isAppLockOverallActive(context)) {
            return false
        }
        
        // If schedule is enabled, check current time
        if (isScheduleEnabled(context)) {
            val calendar = java.util.Calendar.getInstance()
            val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val currentMin = calendar.get(java.util.Calendar.MINUTE)
            
            val startHour = getScheduleStartHour(context)
            val startMin = getScheduleStartMinute(context)
            val endHour = getScheduleEndHour(context)
            val endMin = getScheduleEndMinute(context)
            
            return isTimeWithinSchedule(startHour, startMin, endHour, endMin, currentHour, currentMin)
        }
        
        return true
    }

    fun isTimeWithinSchedule(startHour: Int, startMin: Int, endHour: Int, endMin: Int, currentHour: Int, currentMin: Int): Boolean {
        val start = startHour * 60 + startMin
        val end = endHour * 60 + endMin
        val current = currentHour * 60 + currentMin

        return if (start < end) {
            current in start..end
        } else if (start > end) {
            current >= start || current <= end
        } else {
            true
        }
    }
}
