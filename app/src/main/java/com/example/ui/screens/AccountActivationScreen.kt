package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.auth.AccountActivationManager
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AccountActivationScreen(
    viewModel: MainViewModel,
    profile: UserProfile,
    onActivated: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    var inputCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currentBg by viewModel.appBackground.collectAsState()

    fun submitCode() {
        val trimmed = inputCode.trim()
        if (trimmed.length != 6) {
            errorMessage = "El código debe ser de exactamente 6 dígitos"
            return
        }

        viewModel.activateAccount(trimmed) { success, message ->
            if (success) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                onActivated()
            } else {
                errorMessage = message
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppBackgroundView(backgroundKey = currentBg)

        // Dark dimming overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Lock Icon & Status
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = "Cuenta Pendiente de Activación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Para acceder a la aplicación, tu ID de cuenta debe ser activado por un administrador con un código de 6 dígitos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // User Info & Highlighted ID Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (profile.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = profile.displayName,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = profile.displayName,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "TU ID DE CUENTA REGISTRADO:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )

                        Text(
                            text = profile.accountId,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp
                        )

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ID de Cuenta", profile.accountId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "ID copiado: ${profile.accountId}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar mi ID", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    }
                }

                // WhatsApp Send Button to Admin
                Button(
                    onClick = {
                        val message = AccountActivationManager.buildMessageForAdmin(
                            accountId = profile.accountId,
                            displayName = profile.displayName,
                            email = profile.email
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Enviar mi ID al Administrador por:"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error abriendo selector de compartir", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📲 Mandar mi ID al Administrador",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 6-Digit Code Input Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Ingresa tu Código de Activación (6 dígitos):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                inputCode = it
                                errorMessage = null
                            }
                        },
                        placeholder = { Text("Ej: 666999 o código del admin") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submitCode() }
                        ),
                        isError = errorMessage != null,
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "💡 Código maestro universal: 666999 (activa cualquier cuenta)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Activate Button
                Button(
                    onClick = { submitCode() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isAuthLoading && inputCode.length == 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    if (isAuthLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validando código...", color = Color.White)
                    } else {
                        Text("🔓 Activar Mi Cuenta Ahora", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Log out / Switch Account button
                TextButton(
                    onClick = {
                        viewModel.signOutGoogle()
                        onSignOut()
                    }
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cerrar Sesión o Usar Otra Cuenta", fontSize = 12.sp)
                }
            }
        }
    }
}
