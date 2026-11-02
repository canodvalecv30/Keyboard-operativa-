package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.auth.AccountActivationManager
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AdminIdManagerSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProfiles by viewModel.allUserProfiles.collectAsState()
    var searchIdQuery by remember { mutableStateOf("") }
    var lookupAccountId by remember { mutableStateOf("") }
    var generatedCodeResult by remember { mutableStateOf<String?>(null) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1E1B4B).copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.25f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "👑 Panel Admin: Control de IDs y Códigos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Activa y desactiva IDs para permitir o bloquear el acceso",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Master Code Notice Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFFEAB308)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CÓDIGO MAESTRO UNIVERSAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = AccountActivationManager.MASTER_ACTIVATION_CODE,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEAB308),
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Código Maestro", AccountActivationManager.MASTER_ACTIVATION_CODE)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Código Maestro 666999 copiado", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEAB308)),
                        border = BorderStroke(1.dp, Color(0xFFEAB308))
                    ) {
                        Text("Copiar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick 6-digit Code Calculator for Any ID
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔍 Generador de Código de 6 Dígitos por ID:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = lookupAccountId,
                            onValueChange = {
                                lookupAccountId = it
                                generatedCodeResult = if (it.isNotBlank()) {
                                    AccountActivationManager.generateActivationCodeForId(it)
                                } else null
                            },
                            placeholder = { Text("Ej: OP-86031", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (generatedCodeResult != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF8B5CF6),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = generatedCodeResult!!,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    if (generatedCodeResult != null && lookupAccountId.isNotBlank()) {
                        Button(
                            onClick = {
                                val message = AccountActivationManager.buildMessageForUser(
                                    accountId = lookupAccountId,
                                    code = generatedCodeResult!!
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, message)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Enviar código de activación por:"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mandar Código ($generatedCodeResult) al Usuario por WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

            // Registered Accounts List (Activate/Deactivate Controls)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 Cuentas e IDs Registrados (${allProfiles.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            val filtered = if (searchIdQuery.isBlank()) {
                allProfiles
            } else {
                allProfiles.filter {
                    it.accountId.contains(searchIdQuery, ignoreCase = true) ||
                            it.displayName.contains(searchIdQuery, ignoreCase = true) ||
                            it.email.contains(searchIdQuery, ignoreCase = true)
                }
            }

            if (filtered.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (allProfiles.isEmpty()) "Aún no hay usuarios registrados en la base de datos." else "No se encontraron IDs con esa búsqueda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtered.forEach { user ->
                        val codeForThisUser = AccountActivationManager.generateActivationCodeForId(user.accountId)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (user.isActive) Color(0xFF0F172A) else Color(0xFF450A0A).copy(alpha = 0.6f),
                            border = BorderStroke(
                                1.dp,
                                if (user.isActive) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (user.photoUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = user.photoUrl,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF8B5CF6),
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(user.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = user.displayName.ifBlank { "Operador" },
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = user.email,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    // Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (user.isActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (user.isActive) "🟢 Activo" else "🔴 Inactivo",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Account ID & 6-Digit Code Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ID: ${user.accountId}", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 13.sp)

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Código: $codeForThisUser", fontWeight = FontWeight.Bold, color = Color(0xFFEAB308), fontSize = 12.sp)
                                    }

                                    // WhatsApp Share Button for this specific user
                                    IconButton(
                                        onClick = {
                                            val message = AccountActivationManager.buildMessageForUser(
                                                accountId = user.accountId,
                                                code = codeForThisUser
                                            )
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, message)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Enviar código al usuario:"))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Compartir código", tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Toggle Activation Switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (user.isActive) "Permitir acceso a la app" else "Acceso bloqueado a la app",
                                        fontSize = 11.sp,
                                        color = if (user.isActive) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )

                                    Switch(
                                        checked = user.isActive,
                                        onCheckedChange = { newStatus ->
                                            viewModel.toggleUserActivation(user.id, newStatus) { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
