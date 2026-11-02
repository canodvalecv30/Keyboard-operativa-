package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.service.FloatingCancelOverlayService
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.SnippetEntity
import com.example.ui.viewmodel.MainViewModel
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun KeyboardSettingsScreen(
    viewModel: MainViewModel,
    isAdmin: Boolean = false,
    onAdminStatusChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = viewModel.preferences

    var autoSpace by remember { mutableStateOf(prefs.autoSpaceAfterPaste) }
    var autoEnter by remember { mutableStateOf(prefs.autoEnterAfterPaste) }
    var haptics by remember { mutableStateOf(prefs.hapticFeedbackEnabled) }
    var isDarkTheme by remember { mutableStateOf(prefs.isDarkTheme) }
    var tikTokAutoDetect by remember { mutableStateOf(prefs.tikTokLiveAutoDetect) }
    var tikTokOnlyMode by remember { mutableStateOf(prefs.tikTokLiveOnlyMode) }
    var burstDelayMs by remember { mutableStateOf(prefs.burstDelayMs) }
    var pasteSpeedDelayMs by remember { mutableStateOf(prefs.pasteSpeedDelayMs) }
    var showToastOnPaste by remember { mutableStateOf(prefs.showToastOnPaste) }
    var ultraFastMode by remember { mutableStateOf(prefs.ultraFastMode) }
    var isGridMode by remember { mutableStateOf(prefs.isGridMode) }
    var gridColumnCount by remember { mutableIntStateOf(prefs.gridColumnCount) }
    var isAutoSendActive by remember { mutableStateOf(prefs.isAutoSendActive) }
    var showFloatingCancel by remember { mutableStateOf(prefs.showFloatingCancelButton) }
    var systemOverlayEnabled by remember { mutableStateOf(prefs.systemOverlayEnabled) }
    var isButtonsReversed by remember { mutableStateOf(prefs.isButtonsReversed) }

    var showImportJsonDialog by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }

    val snippets by viewModel.allSnippets.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Official Google Account & Unique ID Profile Card
        AccountProfileCard(viewModel = viewModel)

        if (isAdmin) {
            // Admin Panel: Management of Google Accounts, IDs, Activation Toggles & 6-digit Codes
            AdminIdManagerSection(viewModel = viewModel)
        }

        // Admin Security Management Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🌐 Panel de Usuarios y Contraseñas Online",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sincronización en la nube, licencias de 1 mes y gestión de David y usuarios",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val onlineAuth = viewModel.onlineAuth
                val isOnline by remember { mutableStateOf(onlineAuth.isNetworkAvailable()) }
                val isSyncing by viewModel.isSyncingOnline.collectAsState()
                var syncFeedback by remember { mutableStateOf(onlineAuth.lastSyncMessage) }

                // Live Online Status Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOnline) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOnline) "🟢 Modo En Línea (Online Conectado)" else "🟠 Modo Local (Offline)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                            Text(
                                text = syncFeedback,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.syncOnline { success, msg ->
                                    syncFeedback = msg
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text(if (isSyncing) "Sincronizando..." else "🔄 Sincronizar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (isAdmin) {
                    Text(
                        text = "👑 Modo Administrador Activo: Registra, renueva y administra las contraseñas de usuarios en línea.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    // Cloud Server URL & JSON Tools Dialog state
                    var showServerConfigDialog by remember { mutableStateOf(false) }
                    var showJsonImportDialog by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showServerConfigDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Servidor Cloud", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                val json = onlineAuth.exportUsersJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Usuarios Online JSON", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Configuración JSON copiada al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar JSON", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = { showJsonImportDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Importar JSON", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (showServerConfigDialog) {
                        var tempUrl by remember { mutableStateOf(onlineAuth.onlineServerUrl) }
                        AlertDialog(
                            onDismissRequest = { showServerConfigDialog = false },
                            title = { Text("Configurar Servidor Online") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Ingresa la URL del servidor o webhook para sincronizar contraseñas y renovaciones online:",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    OutlinedTextField(
                                        value = tempUrl,
                                        onValueChange = { tempUrl = it },
                                        label = { Text("URL Servidor / Endpoint") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    onlineAuth.onlineServerUrl = tempUrl.trim()
                                    showServerConfigDialog = false
                                    Toast.makeText(context, "URL de servidor online guardada", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Guardar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showServerConfigDialog = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }

                    if (showJsonImportDialog) {
                        var inputJson by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { showJsonImportDialog = false },
                            title = { Text("Importar Usuarios desde JSON") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Pega el JSON con la lista de usuarios y contraseñas autorizados:", style = MaterialTheme.typography.bodySmall)
                                    OutlinedTextField(
                                        value = inputJson,
                                        onValueChange = { inputJson = it },
                                        placeholder = { Text("[{\"username\":\"David\",\"password\":\"046\",\"role\":\"ADMIN\"}]") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    val count = onlineAuth.importUsersFromJson(inputJson)
                                    if (count > 0) {
                                        viewModel.refreshOnlineUsers()
                                        showJsonImportDialog = false
                                        Toast.makeText(context, "$count usuarios importados exitosamente", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Formato JSON inválido", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("Importar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showJsonImportDialog = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }

                    HorizontalDivider()

                    // Master Admin Password change
                    var tempAdminPass by remember { mutableStateOf(prefs.adminPassword) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempAdminPass,
                            onValueChange = { tempAdminPass = it },
                            label = { Text("Código de Administrador Maestro") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                prefs.adminPassword = tempAdminPass.trim()
                                Toast.makeText(context, "Código de Administrador actualizado", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text("Guardar", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    HorizontalDivider()

                    // --- PANEL DE USUARIOS REGISTRADOS ---
                    val onlineUsersList by viewModel.onlineUsers.collectAsState()

                    Text(
                        text = "👥 Cuentas y Contraseñas Registradas (${onlineUsersList.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6)
                    )

                    Text(
                        text = "Incluye a David (Admin), General y cuentas personalizadas con expiración de 1 mes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        onlineUsersList.forEach { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (user.role == "ADMIN" || user.role == "MASTER_ADMIN")
                                        Color(0xFF8B5CF6).copy(alpha = 0.08f)
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (user.role == "ADMIN" || user.role == "MASTER_ADMIN") "👑 ${user.username}" else "👤 ${user.username}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (user.role == "ADMIN") Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = if (user.role == "ADMIN") "ADMIN" else "1 MES",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (user.role == "ADMIN") Color(0xFF8B5CF6) else Color(0xFF10B981),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "PIN/Contraseña: ${user.password}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            val statusText = when {
                                                user.isBlocked -> "🚫 Bloqueado por Administrador"
                                                user.isExpired -> "🔴 Expirado (0 días restantes)"
                                                user.role == "ADMIN" -> "🟢 Acceso Permanente (Online)"
                                                else -> "🟢 Activo (${user.daysRemaining} días restantes)"
                                            }
                                            val statusColor = when {
                                                user.isBlocked || user.isExpired -> MaterialTheme.colorScheme.error
                                                else -> Color(0xFF10B981)
                                            }

                                            Text(
                                                text = statusText,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = statusColor
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (user.role != "ADMIN" && user.role != "MASTER_ADMIN") {
                                                Button(
                                                    onClick = {
                                                        onlineAuth.renewUser(user.username, 30)
                                                        viewModel.refreshOnlineUsers()
                                                        Toast.makeText(context, "Licencia renovada por 1 mes para ${user.username}", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                                ) {
                                                    Text("🔄 +30 Días", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    onlineAuth.toggleBlockUser(user.username)
                                                    viewModel.refreshOnlineUsers()
                                                    Toast.makeText(context, "Estado de ${user.username} modificado", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (user.isBlocked) Icons.Default.CheckCircle else Icons.Default.Info,
                                                    contentDescription = "Bloquear o Desbloquear",
                                                    tint = if (user.isBlocked) Color(0xFF10B981) else Color(0xFFF59E0B)
                                                )
                                            }

                                            if (!user.username.equals("Admin", ignoreCase = true)) {
                                                IconButton(
                                                    onClick = {
                                                        onlineAuth.removeUser(user.username)
                                                        viewModel.refreshOnlineUsers()
                                                        Toast.makeText(context, "Cuenta ${user.username} eliminada", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar cuenta",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // --- REGISTRAR NUEVA CUENTA ONLINE ---
                    Text(
                        text = "➕ Registrar Nueva Cuenta Online",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6)
                    )

                    var newAccountName by remember { mutableStateOf("") }
                    var newAccountPass by remember { mutableStateOf("") }
                    var newAccountIsAdmin by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = newAccountName,
                        onValueChange = { newAccountName = it },
                        label = { Text("Nombre de Usuario (ej: David, Operativa)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newAccountPass,
                        onValueChange = { newAccountPass = it },
                        label = { Text("Contraseña o PIN de Acceso") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (newAccountIsAdmin) "👑 Rol: Administrador (Sin límite)" else "👤 Rol: Usuario Normal (1 Mes)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (newAccountIsAdmin) Color(0xFF8B5CF6) else Color(0xFF10B981)
                        )
                        Switch(
                            checked = newAccountIsAdmin,
                            onCheckedChange = { newAccountIsAdmin = it }
                        )
                    }

                    Button(
                        onClick = {
                            if (newAccountName.isNotBlank() && newAccountPass.isNotBlank()) {
                                val newUser = com.example.data.auth.OnlineUser(
                                    username = newAccountName.trim(),
                                    password = newAccountPass.trim(),
                                    role = if (newAccountIsAdmin) "ADMIN" else "USER",
                                    activationTimestamp = System.currentTimeMillis(),
                                    durationDays = if (newAccountIsAdmin) 9999 else 30,
                                    isBlocked = false,
                                    lastActiveTimestamp = System.currentTimeMillis(),
                                    onlineStatus = "ONLINE_SYNCED"
                                )
                                onlineAuth.addOrUpdateUser(newUser)
                                viewModel.refreshOnlineUsers()
                                newAccountName = ""
                                newAccountPass = ""
                                Toast.makeText(context, "¡Cuenta registrada y sincronizada online!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Completa nombre y contraseña", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("➕ Registrar y Autorizar Online", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onAdminStatusChange(false) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔒 Cerrar Sesión de Administrador")
                    }
                } else {
                    Text(
                        text = "Estás en modo usuario normal. Para registrar usuarios, crear administradores (David) o renovar licencias online, inicia sesión como Administrador.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { showAdminLoginDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔑 Iniciar como Administrador", color = Color.White)
                    }
                }
            }
        }

        // 1. Activation Guide Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Activar Teclado en Android",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Para usar Spam de la Operativa Keyboard en WhatsApp, Instagram, Juegos o cualquier app, sigue estos 2 pasos:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Step 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Paso 1: Habilitar en Ajustes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Activa 'Spam de la Operativa Keyboard' en la lista de teclados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Abre Ajustes -> Idioma e Entrada", Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Text("1. Habilitar")
                    }
                }

                // Step 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Paso 2: Seleccionar Teclado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Cambia el teclado activo por Spam de la Operativa Keyboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        }
                    ) {
                        Text("2. Cambiar")
                    }
                }
            }
        }

        // 1.5. Second Space / Multi-user info card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Compatibilidad Universal de Teléfonos & OEMs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Spam de la Operativa Keyboard es compatible de forma nativa con todos los teléfonos Android (Samsung, Xiaomi, Motorola, Pixel, Oppo, Realme, Vivo, etc.).\n\n" +
                            "📱 Xiaomi / Redmi / POCO (HyperOS & MIUI):\n" +
                            "• Habilita 'Autoinicio' en Seguridad -> Permisos para que el teclado siempre esté listo.\n" +
                            "• Si usas 'Segundo Espacio', clona/instala esta app dentro del Segundo Espacio.\n\n" +
                            "📱 Samsung (One UI):\n" +
                            "• Activa la opción 'Botón Teclado en la barra de navegación' para cambiar entre teclados al instante.\n\n" +
                            "📱 Motorola / Pixel / Realme / Vivo / Oppo:\n" +
                            "• Toca '2. Cambiar' arriba en cualquier momento para alternar rápidamente con tu teclado de fábrica (Gboard/Samsung Keyboard).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Customization & On-Screen Keyboard Settings Card
        var currentHeightScale by remember { mutableStateOf(prefs.keyHeightScale) }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Teclado en Pantalla (On-Screen Keyboard)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Language Selector for On-Screen Keyboard
                var selectedLanguage by remember { mutableStateOf(prefs.keyboardLanguage) }
                Column {
                    Text(
                        text = "Idioma del Teclado en Pantalla",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Selecciona el idioma activo para el teclado en pantalla y las sugerencias.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ES" to "Español 🇪🇸",
                            "EN" to "English 🇺🇸",
                            "PT" to "Português 🇧🇷",
                            "FR" to "Français 🇫🇷"
                        ).forEach { (code, label) ->
                            val isSelected = selectedLanguage == code
                            if (isSelected) {
                                Button(
                                    onClick = {
                                        selectedLanguage = code
                                        prefs.keyboardLanguage = code
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        selectedLanguage = code
                                        prefs.keyboardLanguage = code
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configurar en Teclado en Pantalla de Android")
                    }
                }

                // Keyboard height scale selector for small/large phone screens
                Column {
                    Text(
                        text = "Escala de Altura de Teclas (Adaptable a Teléfonos)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ajusta la altura de las teclas para adaptarse a pantallas de cualquier tamaño.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0.8f to "Compacto (0.8x)",
                            1.0f to "Estándar (1.0x)",
                            1.2f to "Grande (1.2x)",
                            1.4f to "Max (1.4x)"
                        ).forEach { (scale, label) ->
                            val isSelected = currentHeightScale == scale
                            if (isSelected) {
                                Button(
                                    onClick = {
                                        currentHeightScale = scale
                                        prefs.keyHeightScale = scale
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        currentHeightScale = scale
                                        prefs.keyHeightScale = scale
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Keyboard layout view mode selector (Grid vs List)
                Column {
                    Text(
                        text = "Modo de Vista de Snippets en el Teclado",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Elige si prefieres ver los snippets organizados en cuadrícula o en lista continua.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isGridMode = true
                                prefs.isGridMode = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGridMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isGridMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📊 Cuadrícula (Grid)", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                isGridMode = false
                                prefs.isGridMode = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isGridMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isGridMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📋 Lista (Flow)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Grid Column count selector (if grid mode)
                if (isGridMode) {
                    Column {
                        Text(
                            text = "Número de Columnas en Cuadrícula",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                2 to "2 Columnas",
                                3 to "3 Columnas",
                                4 to "4 Columnas"
                            ).forEach { (cols, label) ->
                                val isSelected = gridColumnCount == cols
                                if (isSelected) {
                                    Button(
                                        onClick = {
                                            gridColumnCount = cols
                                            prefs.gridColumnCount = cols
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            gridColumnCount = cols
                                            prefs.gridColumnCount = cols
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // Auto Send Switch
                SettingSwitchRow(
                    title = "⚡ Auto-Envío Automático al Pegar",
                    description = "Envía la frase automáticamente tras seleccionarla sin necesidad de presionar enviar.",
                    checked = isAutoSendActive,
                    onCheckedChange = {
                        isAutoSendActive = it
                        prefs.isAutoSendActive = it
                    }
                )

                // Floating Cancel Button Switch (Full Screen In-App & Keyboard)
                SettingSwitchRow(
                    title = "🛑 Botón Flotante Móvil por Toda la Pantalla",
                    description = "Todo el botón se puede arrastrar libremente por cualquier parte de la pantalla (arriba, abajo, izquierda, derecha) y nunca se quita cuando el teclado se baje o se cierre.",
                    checked = showFloatingCancel,
                    onCheckedChange = {
                        showFloatingCancel = it
                        prefs.showFloatingCancelButton = it
                    }
                )

                // System Overlay Floating Button Switch (Over WhatsApp, TikTok, etc.)
                SettingSwitchRow(
                    title = "📱 Botón Flotante Permanente en Todo el Celular (Sobre WhatsApp, TikTok, etc.)",
                    description = "Mantiene el botón flotante fijo en la pantalla de tu celular sobre cualquier app externa, activo incluso cuando el teclado está cerrado.",
                    checked = systemOverlayEnabled,
                    onCheckedChange = { isEnabled ->
                        if (isEnabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                Toast.makeText(context, "Autoriza 'Mostrar sobre otras apps' para activar el botón en todo el celular", Toast.LENGTH_LONG).show()
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                                    } catch (e2: Exception) {
                                        Toast.makeText(context, "Por favor autoriza el permiso en Ajustes de tu teléfono", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                try {
                                    systemOverlayEnabled = true
                                    prefs.systemOverlayEnabled = true
                                    FloatingCancelOverlayService.start(context)
                                    Toast.makeText(context, "🟢 Botón flotante activado sobre todo el celular", Toast.LENGTH_SHORT).show()
                                } catch (t: Throwable) {
                                    Toast.makeText(context, "No se pudo iniciar el servicio de botón flotante", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            try {
                                systemOverlayEnabled = false
                                prefs.systemOverlayEnabled = false
                                FloatingCancelOverlayService.stop(context)
                                Toast.makeText(context, "🔴 Botón flotante sobre otras apps desactivado", Toast.LENGTH_SHORT).show()
                            } catch (t: Throwable) {
                                t.printStackTrace()
                            }
                        }
                    }
                )

                // Buttons Reversed Switch (Lado del teclado / Modo Zurdo)
                SettingSwitchRow(
                    title = "↔️ Acomodar Botones al Otro Lado",
                    description = "Invierte la posición de los botones (Enter, Borrar, Espacio y Macros) para usarlos fácilmente con la otra mano.",
                    checked = isButtonsReversed,
                    onCheckedChange = {
                        isButtonsReversed = it
                        prefs.isButtonsReversed = it
                    }
                )

                // Auto Space Switch
                SettingSwitchRow(
                    title = "Auto-Espacio después de pegar",
                    description = "Añade un espacio automáticamente tras insertar un snippet.",
                    checked = autoSpace,
                    onCheckedChange = {
                        autoSpace = it
                        prefs.autoSpaceAfterPaste = it
                    }
                )

                // Auto Enter Switch
                SettingSwitchRow(
                    title = "Auto-Enter (Nueva Línea)",
                    description = "Añade un salto de línea automáticamente tras pegar.",
                    checked = autoEnter,
                    onCheckedChange = {
                        autoEnter = it
                        prefs.autoEnterAfterPaste = it
                    }
                )

                // Haptics Switch
                SettingSwitchRow(
                    title = "Vibración Háptica",
                    description = "Vibración sutil al pulsar cualquier tecla del teclado.",
                    checked = haptics,
                    onCheckedChange = {
                        haptics = it
                        prefs.hapticFeedbackEnabled = it
                    }
                )

                // Dark Theme Switch
                SettingSwitchRow(
                    title = "Tema Oscuro Cyber Neon",
                    description = "Teclado optimizado de alto contraste para noche.",
                    checked = isDarkTheme,
                    onCheckedChange = {
                        isDarkTheme = it
                        prefs.isDarkTheme = it
                    }
                )
            }
        }

        // 2.3. Personalización de Fondo de Pantalla
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🎨 Fondo de Pantalla y Personalización",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Elige el diseño visual de fondo para el teclado y la aplicación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val currentBg by viewModel.appBackground.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "PRESET_DEFAULT" to "🖼️ Operativa",
                        "PRESET_NEON" to "🌌 Neon",
                        "PRESET_GOLD" to "👑 Oro"
                    )
                    presets.forEach { (presetKey, label) ->
                        val isSelected = currentBg == presetKey
                        Button(
                            onClick = {
                                viewModel.updateAppBackground(presetKey)
                                Toast.makeText(context, "Fondo aplicado: $label", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // 2.5. TikTok Live & Speed Optimization Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color(0xFFFE2C55).copy(alpha = 0.12f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = Color(0xFFFE2C55),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🎵 Modo TikTok Live & Velocidad de Envío",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Ajusta la velocidad de pegado, ráfaga continua y activa las optimizaciones para máxima fluidez y cero lag.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Paste Latency Selector
                Column {
                    Text(
                        text = "⚡ Velocidad de Pegado y Auto-Envío (Paste Latency)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Controla el tiempo entre la inserción del texto y el envío automático.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0L to "⚡ 0ms (Instantáneo)",
                            10L to "🔥 10ms (Turbo)",
                            25L to "🚀 25ms (Rápido)",
                            50L to "⏱️ 50ms (Normal)"
                        ).forEach { (delay, label) ->
                            val isSelected = pasteSpeedDelayMs == delay
                            if (isSelected) {
                                Button(
                                    onClick = {
                                        pasteSpeedDelayMs = delay
                                        prefs.pasteSpeedDelayMs = delay
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        pasteSpeedDelayMs = delay
                                        prefs.pasteSpeedDelayMs = delay
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Burst / Spam Speed selector
                Column {
                    Text(
                        text = "🔥 Velocidad de Ráfaga / Spam Continuo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Intervalo entre cada mensaje al mantener pulsado o activar ráfaga.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            25L to "⚡ 25ms Turbo",
                            50L to "🔥 50ms Ultra",
                            100L to "🚀 100ms",
                            180L to "⏱️ 180ms",
                            300L to "🛡️ 300ms"
                        ).forEach { (delay, label) ->
                            val isSelected = burstDelayMs == delay
                            if (isSelected) {
                                Button(
                                    onClick = {
                                        burstDelayMs = delay
                                        prefs.burstDelayMs = delay
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        burstDelayMs = delay
                                        prefs.burstDelayMs = delay
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                }
                            }
                        }
                    }
                }

                // Ultra Fast Mode Toggle (Hardware Batch Edit)
                SettingSwitchRow(
                    title = "🚀 Ultra Optimización por Lotes (Batch Edit)",
                    description = "Inyecta el texto usando el canal atómico de Android, eliminando el lag en WhatsApp, TikTok, Discord y navegadores.",
                    checked = ultraFastMode,
                    onCheckedChange = {
                        ultraFastMode = it
                        prefs.ultraFastMode = it
                    }
                )

                // Show Toast Toggle (Disable for max speed)
                SettingSwitchRow(
                    title = "🔔 Mostrar Notificación Toast al Pegar",
                    description = "Desactivado por defecto para evitar sobrecargar el sistema Android al pegar o spamear a toda velocidad.",
                    checked = showToastOnPaste,
                    onCheckedChange = {
                        showToastOnPaste = it
                        prefs.showToastOnPaste = it
                    }
                )

                // TikTok Auto-Detect Switch
                SettingSwitchRow(
                    title = "Auto-Detectar TikTok Live",
                    description = "Muestra la insignia TikTok Live y habilita el botón de ultra velocidad cuando TikTok está abierto.",
                    checked = tikTokAutoDetect,
                    onCheckedChange = {
                        tikTokAutoDetect = it
                        prefs.tikTokLiveAutoDetect = it
                    }
                )

                // TikTok Exclusive Mode Switch
                SettingSwitchRow(
                    title = "Modo Exclusivo TikTok Live",
                    description = "Mantiene el teclado con perfil ligero priorizado cuando estés dentro de TikTok Live.",
                    checked = tikTokOnlyMode,
                    onCheckedChange = {
                        tikTokOnlyMode = it
                        prefs.tikTokLiveOnlyMode = it
                    }
                )
            }
        }

        // 3. Backup & Export Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Copia de Seguridad & Exportar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Exporta tus snippets en formato JSON para respaldar o transferir a otro dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val jsonArray = JSONArray()
                            snippets.forEach { s ->
                                val obj = JSONObject()
                                obj.put("title", s.title)
                                obj.put("content", s.content)
                                jsonArray.put(obj)
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SnippetsBackup", jsonArray.toString(2))
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Backup JSON copiado al portapapeles 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exportar JSON")
                    }

                    Button(
                        onClick = { showImportJsonDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Importar JSON")
                    }
                }
            }
        }

        // 8. Créditos de los Creadores
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "👑 Créditos de los Creadores",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Desarrollado por: La Operativa",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "y los hermanos: el N",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Import JSON Dialog
    if (showImportJsonDialog) {
        var jsonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showImportJsonDialog = false },
            title = { Text("Importar Snippets JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Pega un array JSON de snippets con formato [{\"title\": \"...\", \"content\": \"...\"}]",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = { jsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("[{\"title\": \"Ejemplo\", \"content\": \"Texto\"}]") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val array = JSONArray(jsonText)
                            var importedCount = 0
                            val defaultCatId = categories.firstOrNull()?.id ?: 1
                            for (i in 0 until array.length()) {
                                val obj = array.getJSONObject(i)
                                val title = obj.optString("title", "Importado")
                                val content = obj.optString("content", "")
                                if (content.isNotBlank()) {
                                    viewModel.addSnippet(title, content, defaultCatId, false)
                                    importedCount++
                                }
                            }
                            Toast.makeText(context, "¡Se importaron $importedCount snippets!", Toast.LENGTH_SHORT).show()
                            showImportJsonDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al procesar JSON. Verifica la sintaxis.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = jsonText.isNotBlank()
                ) {
                    Text("Importar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportJsonDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAdminLoginDialog) {
        var inputPass by remember { mutableStateOf("") }
        var loginError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAdminLoginDialog = false },
            title = { Text("Acceso de Administrador", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Introduce el código de Administrador para desbloquear el registro y cambio de contraseñas:")
                    OutlinedTextField(
                        value = inputPass,
                        onValueChange = {
                            inputPass = it
                            loginError = false
                        },
                        label = { Text("Código Admin") },
                        singleLine = true,
                        isError = loginError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (loginError) {
                        Text("❌ Código incorrecto (Predeterminado: admin123)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputPass.trim() == prefs.adminPassword) {
                            showAdminLoginDialog = false
                            onAdminStatusChange(true)
                            Toast.makeText(context, "¡Modo Administrador activado!", Toast.LENGTH_SHORT).show()
                        } else {
                            loginError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("Verificar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminLoginDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
