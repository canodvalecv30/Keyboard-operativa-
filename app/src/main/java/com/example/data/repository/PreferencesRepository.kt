package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val userManager = context.getSystemService(Context.USER_SERVICE) as? android.os.UserManager
        val isUnlocked = Build.VERSION.SDK_INT < Build.VERSION_CODES.N || (userManager?.isUserUnlocked == true)
        val safeContext = if (!isUnlocked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createDeviceProtectedStorageContext()
        } else {
            context
        }
        safeContext.getSharedPreferences("autopaste_prefs", Context.MODE_PRIVATE)
    }

    var autoSpaceAfterPaste: Boolean
        get() = try { prefs.getBoolean("auto_space", true) } catch (e: Exception) { true }
        set(value) { try { prefs.edit().putBoolean("auto_space", value).apply() } catch (e: Exception) {} }

    var autoEnterAfterPaste: Boolean
        get() = try { prefs.getBoolean("auto_enter", false) } catch (e: Exception) { false }
        set(value) { try { prefs.edit().putBoolean("auto_enter", value).apply() } catch (e: Exception) {} }

    var hapticFeedbackEnabled: Boolean
        get() = try { prefs.getBoolean("haptic_feedback", true) } catch (e: Exception) { true }
        set(value) { try { prefs.edit().putBoolean("haptic_feedback", value).apply() } catch (e: Exception) {} }

    var defaultRepeatMultiplier: Int
        get() = try { prefs.getInt("repeat_multiplier", 1) } catch (e: Exception) { 1 }
        set(value) { try { prefs.edit().putInt("repeat_multiplier", value).apply() } catch (e: Exception) {} }

    var keyHeightScale: Float
        get() = try { prefs.getFloat("key_height_scale", 1.0f) } catch (e: Exception) { 1.0f }
        set(value) { try { prefs.edit().putFloat("key_height_scale", value).apply() } catch (e: Exception) {} }

    var autoKeyboardControl: Boolean
        get() = try { prefs.getBoolean("auto_keyboard_control", true) } catch (e: Exception) { true }
        set(value) { try { prefs.edit().putBoolean("auto_keyboard_control", value).apply() } catch (e: Exception) {} }

    var isDarkTheme: Boolean
        get() = try { prefs.getBoolean("dark_theme", true) } catch (e: Exception) { true }
        set(value) { try { prefs.edit().putBoolean("dark_theme", value).apply() } catch (e: Exception) {} }

    var isGridMode: Boolean
        get() = try { prefs.getBoolean("is_grid_mode", true) } catch (e: Exception) { true }
        set(value) { try { prefs.edit().putBoolean("is_grid_mode", value).apply() } catch (e: Exception) {} }

    var gridColumnCount: Int
        get() = try { prefs.getInt("grid_column_count", 2) } catch (e: Exception) { 2 }
        set(value) { try { prefs.edit().putInt("grid_column_count", value).apply() } catch (e: Exception) {} }

    var isAutoSendActive: Boolean
        get() = try { prefs.getBoolean("is_auto_send_active", true) } catch (e: Exception) { true }
        set(value) { try { prefs.edit().putBoolean("is_auto_send_active", value).apply() } catch (e: Exception) {} }

    var isButtonsReversed: Boolean
        get() = try { prefs.getBoolean("is_buttons_reversed", false) } catch (e: Exception) { false }
        set(value) { try { prefs.edit().putBoolean("is_buttons_reversed", value).apply() } catch (e: Exception) {} }

    var burstDelayMs: Long
        get() = try { prefs.getLong("burst_delay_ms", 50L) } catch (e: Exception) { 50L }
        set(value) { try { prefs.edit().putLong("burst_delay_ms", value).apply() } catch (e: Exception) {} }

    var pasteSpeedDelayMs: Long
        get() = try { prefs.getLong("paste_speed_delay_ms", 10L) } catch (e: Exception) { 10L }
        set(value) { try { prefs.edit().putLong("paste_speed_delay_ms", value).apply() } catch (e: Exception) {} }

    var showToastOnPaste: Boolean
        get() = try { prefs.getBoolean("show_toast_on_paste", false) } catch (e: Exception) { false }
        set(value) { try { prefs.edit().putBoolean("show_toast_on_paste", value).apply() } catch (e: Exception) {} }

    var ultraFastMode: Boolean
        get() = try { prefs.getBoolean("ultra_fast_mode", true) } catch (e: Exception) { true }
        set(value) { try { prefs.edit().putBoolean("ultra_fast_mode", value).apply() } catch (e: Exception) {} }

    var tikTokLiveAutoDetect: Boolean
        get() = try { prefs.getBoolean("tiktok_live_autodetect", true) } catch (e: Exception) { true }
        set(value) { try { prefs.edit().putBoolean("tiktok_live_autodetect", value).apply() } catch (e: Exception) {} }

    var tikTokLiveOnlyMode: Boolean
        get() = try { prefs.getBoolean("tiktok_live_only_mode", false) } catch (e: Exception) { false }
        set(value) { try { prefs.edit().putBoolean("tiktok_live_only_mode", value).apply() } catch (e: Exception) {} }

    var keyboardLanguage: String
        get() = try { prefs.getString("keyboard_language", "ES") ?: "ES" } catch (e: Exception) { "ES" }
        set(value) { try { prefs.edit().putString("keyboard_language", value).apply() } catch (e: Exception) {} }

    var adminPassword: String
        get() = try { prefs.getString("admin_password", "admin123") ?: "admin123" } catch (e: Exception) { "admin123" }
        set(value) { try { prefs.edit().putString("admin_password", value).apply() } catch (e: Exception) {} }

    var userPassword: String
        get() = try { prefs.getString("user_password", "1234") ?: "1234" } catch (e: Exception) { "1234" }
        set(value) { try { prefs.edit().putString("user_password", value).apply() } catch (e: Exception) {} }

    var customUsersSet: Set<String>
        get() = try { prefs.getStringSet("custom_users", emptySet()) ?: emptySet() } catch (e: Exception) { emptySet() }
        set(value) { try { prefs.edit().putStringSet("custom_users", value).apply() } catch (e: Exception) {} }

    var customAdminsSet: Set<String>
        get() = try { prefs.getStringSet("custom_admins", emptySet()) ?: emptySet() } catch (e: Exception) { emptySet() }
        set(value) { try { prefs.edit().putStringSet("custom_admins", value).apply() } catch (e: Exception) {} }

    fun getCustomUsers(): List<Pair<String, String>> {
        return customUsersSet.mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }

    fun addCustomUser(username: String, pass: String) {
        val current = customUsersSet.toMutableSet()
        current.removeIf { it.startsWith("$username:") }
        current.add("$username:$pass")
        customUsersSet = current
    }

    fun removeCustomUser(username: String) {
        val current = customUsersSet.toMutableSet()
        current.removeIf { it.startsWith("$username:") }
        customUsersSet = current
    }

    fun getCustomAdmins(): List<Pair<String, String>> {
        return customAdminsSet.mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }

    fun addCustomAdmin(username: String, pass: String) {
        val current = customAdminsSet.toMutableSet()
        current.removeIf { it.startsWith("$username:") }
        current.add("$username:$pass")
        customAdminsSet = current
    }

    fun removeCustomAdmin(username: String) {
        val current = customAdminsSet.toMutableSet()
        current.removeIf { it.startsWith("$username:") }
        customAdminsSet = current
    }

    // --- User Usage Tracking & 1-Month Limit ---
    val ONE_MONTH_MILLIS = 30L * 24 * 60 * 60 * 1000L

    fun getUserActivationTimestamp(username: String): Long {
        return try { prefs.getLong("user_activation_$username", 0L) } catch (e: Exception) { 0L }
    }

    fun setUserActivationTimestamp(username: String, timestamp: Long) {
        try { prefs.edit().putLong("user_activation_$username", timestamp).apply() } catch (e: Exception) {}
    }

    fun getUserLastActiveTimestamp(username: String): Long {
        return try { prefs.getLong("user_last_active_$username", 0L) } catch (e: Exception) { 0L }
    }

    fun setUserLastActiveTimestamp(username: String, timestamp: Long) {
        try { prefs.edit().putLong("user_last_active_$username", timestamp).apply() } catch (e: Exception) {}
    }

    fun recordUserLogin(username: String) {
        val now = System.currentTimeMillis()
        if (getUserActivationTimestamp(username) == 0L) {
            setUserActivationTimestamp(username, now)
        }
        setUserLastActiveTimestamp(username, now)
    }

    fun isUserExpired(username: String): Boolean {
        val activation = getUserActivationTimestamp(username)
        if (activation == 0L) return false // Not activated yet
        val now = System.currentTimeMillis()
        return (now - activation) > ONE_MONTH_MILLIS
    }

    fun getDaysRemaining(username: String): Int {
        val activation = getUserActivationTimestamp(username)
        if (activation == 0L) return 30
        val now = System.currentTimeMillis()
        val elapsedMillis = now - activation
        val elapsedDays = (elapsedMillis / (1000L * 60 * 60 * 24)).toInt()
        val remaining = 30 - elapsedDays
        return if (remaining < 0) 0 else remaining
    }

    fun renewUserSubscription(username: String) {
        val now = System.currentTimeMillis()
        setUserActivationTimestamp(username, now)
        setUserLastActiveTimestamp(username, now)
    }

    // --- App Wallpaper / Background Customization ---
    var appBackground: String
        get() = try { prefs.getString("app_background_key", "PRESET_DEFAULT") ?: "PRESET_DEFAULT" } catch (e: Exception) { "PRESET_DEFAULT" }
        set(value) { try { prefs.edit().putString("app_background_key", value).apply() } catch (e: Exception) {} }
}

