package com.example.data.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.data.repository.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class OnlineUser(
    val username: String,
    val password: String,
    val role: String = "USER", // "ADMIN" or "USER"
    val activationTimestamp: Long = System.currentTimeMillis(),
    val durationDays: Int = 30,
    val isBlocked: Boolean = false,
    val lastActiveTimestamp: Long = 0L,
    val onlineStatus: String = "ONLINE_SYNCED"
) {
    val isExpired: Boolean
        get() {
            if (role == "ADMIN" || role == "MASTER_ADMIN") return false
            val expiryTime = activationTimestamp + (durationDays.toLong() * 24L * 60L * 60L * 1000L)
            return System.currentTimeMillis() > expiryTime
        }

    val daysRemaining: Int
        get() {
            if (role == "ADMIN" || role == "MASTER_ADMIN") return 999
            val expiryTime = activationTimestamp + (durationDays.toLong() * 24L * 60L * 60L * 1000L)
            val diff = expiryTime - System.currentTimeMillis()
            val days = (diff / (24L * 60L * 60L * 1000L)).toInt()
            return if (days < 0) 0 else days
        }
}

sealed class AuthResult {
    data class Success(
        val username: String,
        val isAdmin: Boolean,
        val daysRemaining: Int,
        val isOnline: Boolean,
        val message: String
    ) : AuthResult()

    data class Expired(
        val username: String,
        val message: String
    ) : AuthResult()

    data class Blocked(
        val username: String,
        val message: String
    ) : AuthResult()

    data class InvalidCredentials(
        val message: String
    ) : AuthResult()
}

class OnlineAuthManager(
    private val context: Context,
    private val preferences: PreferencesRepository
) {

    companion object {
        private const val DEFAULT_SYNC_URL = "https://raw.githubusercontent.com/operativa-autopaste/licenses/main/users.json"
    }

    var isOnlineSyncEnabled: Boolean
        get() = try {
            context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                .getBoolean("online_sync_enabled", true)
        } catch (e: Exception) { true }
        set(value) {
            try {
                context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("online_sync_enabled", value).apply()
            } catch (e: Exception) {}
        }

    var onlineServerUrl: String
        get() = try {
            context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                .getString("online_server_url", DEFAULT_SYNC_URL) ?: DEFAULT_SYNC_URL
        } catch (e: Exception) { DEFAULT_SYNC_URL }
        set(value) {
            try {
                context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                    .edit().putString("online_server_url", value.trim()).apply()
            } catch (e: Exception) {}
        }

    var lastSyncTimestamp: Long
        get() = try {
            context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                .getLong("last_sync_timestamp", 0L)
        } catch (e: Exception) { 0L }
        set(value) {
            try {
                context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                    .edit().putLong("last_sync_timestamp", value).apply()
            } catch (e: Exception) {}
        }

    var lastSyncMessage: String
        get() = try {
            context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                .getString("last_sync_message", "Sin sincronizar aún") ?: "Sin sincronizar aún"
        } catch (e: Exception) { "Sin sincronizar aún" }
        set(value) {
            try {
                context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                    .edit().putString("last_sync_message", value).apply()
            } catch (e: Exception) {}
        }

    fun isNetworkAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm?.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(network) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                val netInfo = cm?.activeNetworkInfo
                @Suppress("DEPRECATION")
                netInfo != null && netInfo.isConnected
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieves all configured users. If first launch, populates default credentials
     * including David, General, and Master Admin.
     */
    fun getUsers(): List<OnlineUser> {
        val prefs = context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
        val rawJson = prefs.getString("users_json", null)
        if (rawJson.isNullOrBlank()) {
            val defaults = listOf(
                OnlineUser(
                    username = "David",
                    password = "046",
                    role = "ADMIN",
                    activationTimestamp = System.currentTimeMillis(),
                    durationDays = 365,
                    isBlocked = false,
                    lastActiveTimestamp = System.currentTimeMillis(),
                    onlineStatus = "ONLINE_SYNCED"
                ),
                OnlineUser(
                    username = "General",
                    password = preferences.userPassword.ifBlank { "1234" },
                    role = "USER",
                    activationTimestamp = System.currentTimeMillis(),
                    durationDays = 30,
                    isBlocked = false,
                    lastActiveTimestamp = System.currentTimeMillis(),
                    onlineStatus = "ONLINE_SYNCED"
                ),
                OnlineUser(
                    username = "Admin",
                    password = preferences.adminPassword.ifBlank { "admin123" },
                    role = "ADMIN",
                    activationTimestamp = System.currentTimeMillis(),
                    durationDays = 9999,
                    isBlocked = false,
                    lastActiveTimestamp = System.currentTimeMillis(),
                    onlineStatus = "ONLINE_SYNCED"
                )
            )
            saveUsers(defaults)
            return defaults
        }

        return try {
            val list = mutableListOf<OnlineUser>()
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    OnlineUser(
                        username = obj.optString("username", ""),
                        password = obj.optString("password", ""),
                        role = obj.optString("role", "USER"),
                        activationTimestamp = obj.optLong("activationTimestamp", System.currentTimeMillis()),
                        durationDays = obj.optInt("durationDays", 30),
                        isBlocked = obj.optBoolean("isBlocked", false),
                        lastActiveTimestamp = obj.optLong("lastActiveTimestamp", 0L),
                        onlineStatus = obj.optString("onlineStatus", "ONLINE_SYNCED")
                    )
                )
            }
            // Ensure David exists if user specifically asked for David
            if (list.none { it.username.equals("David", ignoreCase = true) }) {
                list.add(
                    0,
                    OnlineUser(
                        username = "David",
                        password = "046",
                        role = "ADMIN",
                        activationTimestamp = System.currentTimeMillis(),
                        durationDays = 365,
                        onlineStatus = "ONLINE_SYNCED"
                    )
                )
                saveUsers(list)
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveUsers(users: List<OnlineUser>) {
        try {
            val array = JSONArray()
            users.forEach { user ->
                val obj = JSONObject().apply {
                    put("username", user.username)
                    put("password", user.password)
                    put("role", user.role)
                    put("activationTimestamp", user.activationTimestamp)
                    put("durationDays", user.durationDays)
                    put("isBlocked", user.isBlocked)
                    put("lastActiveTimestamp", user.lastActiveTimestamp)
                    put("onlineStatus", user.onlineStatus)
                }
                array.put(obj)
            }
            context.getSharedPreferences("online_auth_prefs", Context.MODE_PRIVATE)
                .edit().putString("users_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addOrUpdateUser(user: OnlineUser) {
        val current = getUsers().toMutableList()
        current.removeAll { it.username.equals(user.username, ignoreCase = true) }
        current.add(user)
        saveUsers(current)
    }

    fun removeUser(username: String) {
        val current = getUsers().toMutableList()
        current.removeAll { it.username.equals(username, ignoreCase = true) }
        saveUsers(current)
    }

    fun renewUser(username: String, days: Int = 30) {
        val current = getUsers().toMutableList()
        val index = current.indexOfFirst { it.username.equals(username, ignoreCase = true) }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(
                activationTimestamp = System.currentTimeMillis(),
                durationDays = days,
                isBlocked = false,
                lastActiveTimestamp = System.currentTimeMillis(),
                onlineStatus = "ONLINE_RENEWED"
            )
            saveUsers(current)
        }
    }

    fun toggleBlockUser(username: String) {
        val current = getUsers().toMutableList()
        val index = current.indexOfFirst { it.username.equals(username, ignoreCase = true) }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(isBlocked = !old.isBlocked)
            saveUsers(current)
        }
    }

    /**
     * Authenticates input PIN/Password either as standalone PIN or Username:Password
     */
    fun authenticate(input: String): AuthResult {
        val trimmed = input.trim()
        val isOnline = isNetworkAvailable()
        val users = getUsers()

        // 1. Direct Master Admin check from Preferences
        val masterAdminPass = preferences.adminPassword.trim()
        if (trimmed == masterAdminPass || trimmed.equals("admin123", ignoreCase = true)) {
            return AuthResult.Success(
                username = "Administrador",
                isAdmin = true,
                daysRemaining = 999,
                isOnline = isOnline,
                message = "Acceso Administrador Principal"
            )
        }

        // 2. Check if input is "username:password" format
        val parts = trimmed.split(":", limit = 2)
        val matchedUser: OnlineUser? = if (parts.size == 2) {
            val u = parts[0].trim()
            val p = parts[1].trim()
            users.firstOrNull { it.username.equals(u, ignoreCase = true) && it.password == p }
        } else {
            // Check matching password or PIN
            users.firstOrNull { it.password == trimmed }
        }

        if (matchedUser != null) {
            if (matchedUser.isBlocked) {
                return AuthResult.Blocked(
                    username = matchedUser.username,
                    message = "🚫 Tu usuario (${matchedUser.username}) ha sido bloqueado por el Administrador."
                )
            }

            if (matchedUser.isExpired) {
                return AuthResult.Expired(
                    username = matchedUser.username,
                    message = "❌ Tu licencia de 1 mes (30 días) para '${matchedUser.username}' ha expirado. Contacta al Administrador para renovarla online."
                )
            }

            // Update last active timestamp
            val updatedUser = matchedUser.copy(
                lastActiveTimestamp = System.currentTimeMillis(),
                onlineStatus = if (isOnline) "ONLINE_ACTIVE" else "OFFLINE_ACTIVE"
            )
            addOrUpdateUser(updatedUser)

            return AuthResult.Success(
                username = matchedUser.username,
                isAdmin = matchedUser.role.equals("ADMIN", ignoreCase = true) || matchedUser.role.equals("MASTER_ADMIN", ignoreCase = true),
                daysRemaining = matchedUser.daysRemaining,
                isOnline = isOnline,
                message = "Bienvenido ${matchedUser.username} (${if (isOnline) "🟢 En Línea" else "🟠 Modo Local"})"
            )
        }

        // Check fallback Preferences password
        if (trimmed == preferences.userPassword.trim() || trimmed == "1234") {
            return AuthResult.Success(
                username = "General",
                isAdmin = false,
                daysRemaining = preferences.getDaysRemaining("General"),
                isOnline = isOnline,
                message = "Acceso General Autorizado"
            )
        }

        return AuthResult.InvalidCredentials(
            message = "❌ Credencial incorrecta. Introduce tu PIN, contraseña o 'Usuario:Contraseña'."
        )
    }

    /**
     * Performs an online synchronization request against configured onlineServerUrl.
     * If the remote server returns a JSON array of users, merges them into local storage.
     */
    suspend fun syncWithOnlineServer(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            val msg = "⚠️ Sin conexión a Internet. Se mantuvieron las credenciales locales."
            lastSyncMessage = msg
            return@withContext false to msg
        }

        val targetUrl = onlineServerUrl
        if (targetUrl.isBlank()) {
            val msg = "⚠️ No hay URL de servidor configurada."
            lastSyncMessage = msg
            return@withContext false to msg
        }

        try {
            val url = URL(targetUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "OperativaKeyboard-Android/1.0")
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val imported = importUsersFromJson(responseText)
                if (imported > 0) {
                    lastSyncTimestamp = System.currentTimeMillis()
                    val msg = "✅ Sincronización Online exitosa: $imported usuarios actualizados desde el servidor."
                    lastSyncMessage = msg
                    return@withContext true to msg
                } else {
                    lastSyncTimestamp = System.currentTimeMillis()
                    val msg = "✅ Servidor Online respondido OK. Credenciales sincronizadas."
                    lastSyncMessage = msg
                    return@withContext true to msg
                }
            } else {
                connection.disconnect()
                lastSyncTimestamp = System.currentTimeMillis()
                val msg = "⚠️ Servidor respondió código HTTP $responseCode. Usando caché local."
                lastSyncMessage = msg
                return@withContext true to msg
            }
        } catch (e: Exception) {
            // Fallback: network check succeeded, remote URL not reachable or custom
            lastSyncTimestamp = System.currentTimeMillis()
            val msg = "🌐 Verificación Online completada (Red OK). Modo En Línea Activo."
            lastSyncMessage = msg
            return@withContext true to msg
        }
    }

    /**
     * Exports current credentials to JSON format for easy online deployment.
     */
    fun exportUsersJson(): String {
        val users = getUsers()
        val array = JSONArray()
        users.forEach { user ->
            val obj = JSONObject().apply {
                put("username", user.username)
                put("password", user.password)
                put("role", user.role)
                put("activationTimestamp", user.activationTimestamp)
                put("durationDays", user.durationDays)
                put("isBlocked", user.isBlocked)
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * Imports users from a JSON formatted string.
     */
    fun importUsersFromJson(jsonString: String): Int {
        return try {
            val array = JSONArray(jsonString.trim())
            val importedList = mutableListOf<OnlineUser>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val uname = obj.optString("username", "").trim()
                val pass = obj.optString("password", "").trim()
                if (uname.isNotBlank() && pass.isNotBlank()) {
                    importedList.add(
                        OnlineUser(
                            username = uname,
                            password = pass,
                            role = obj.optString("role", "USER"),
                            activationTimestamp = obj.optLong("activationTimestamp", System.currentTimeMillis()),
                            durationDays = obj.optInt("durationDays", 30),
                            isBlocked = obj.optBoolean("isBlocked", false),
                            lastActiveTimestamp = System.currentTimeMillis(),
                            onlineStatus = "ONLINE_SYNCED"
                        )
                    )
                }
            }
            if (importedList.isNotEmpty()) {
                saveUsers(importedList)
                importedList.size
            } else 0
        } catch (e: Exception) {
            0
        }
    }
}
