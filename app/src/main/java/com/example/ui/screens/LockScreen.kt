package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import com.example.R
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LockScreen(
    viewModel: MainViewModel,
    onUnlocked: (Boolean) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val prefs = viewModel.preferences
    val onlineAuth = viewModel.onlineAuth
    val currentBg by viewModel.appBackground.collectAsState()
    val isOnline by remember { mutableStateOf(onlineAuth.isNetworkAvailable()) }

    fun verifyPassword() {
        if (password.isBlank()) {
            showError = true
            errorMessage = "Introduce tu contraseña o PIN"
            return
        }
        val result = onlineAuth.authenticate(password)
        when (result) {
            is com.example.data.auth.AuthResult.Success -> {
                focusManager.clearFocus()
                keyboardController?.hide()
                onUnlocked(result.isAdmin)
            }
            is com.example.data.auth.AuthResult.Expired -> {
                showError = true
                errorMessage = result.message
            }
            is com.example.data.auth.AuthResult.Blocked -> {
                showError = true
                errorMessage = result.message
            }
            is com.example.data.auth.AuthResult.InvalidCredentials -> {
                showError = true
                errorMessage = result.message
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // App Dynamic Background Image
        AppBackgroundView(backgroundKey = currentBg)

        // Dark dim overlay for high contrast readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Carátula / Cover Image Badge
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.autopaste_icon_1784711570775),
                        contentDescription = "Carátula Operativa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Online Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOnline) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOnline) "🟢 Servidor En Línea • Validación Activa" else "🟠 Modo Local • Sin Conexión",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }

                Text(
                    text = "Acceso Protegido",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Spam de la Operativa Keyboard\nIntroduce tu contraseña de usuario o código de administrador.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    label = { Text("Contraseña o PIN (David / Usuario / Admin)") },
                    placeholder = { Text("Ej: 046 (David), 1234, admin123") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { verifyPassword() }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Ver contraseña"
                            )
                        }
                    },
                    isError = showError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                // Quick Auto Keyboard Open / Close Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("🟢 Abrir Teclado", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("🔴 Cerrar Teclado", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (showError) {
                    Text(
                        text = if (errorMessage.isNotBlank()) errorMessage else "❌ Credencial incorrecta. (David: 046 | General: 1234 | Admin: admin123)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { verifyPassword() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6)
                    )
                ) {
                    Text(
                        text = "🔓 Desbloquear con Validación Online",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = " O REGÍSTRATE ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                // GOOGLE SIGN-IN & REGISTRATION (Core requirement)
                val currentUserProfile by viewModel.currentUserProfile.collectAsState()
                val isAuthLoading by viewModel.isAuthLoading.collectAsState()

                if (currentUserProfile != null) {
                    val profile = currentUserProfile!!
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "👤 Sesión Activa: ${profile.displayName}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "ID DE CUENTA: ${profile.accountId}",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }

                            if (!profile.isActive && profile.role != "admin") {
                                Text(
                                    text = "⚠️ Requiere activación con código de 6 dígitos",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFBBF24)
                                )
                            }
                            Button(
                                onClick = {
                                    val isAdmin = profile.role == "admin"
                                    onUnlocked(isAdmin)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Continuar como ${profile.displayName.take(15)}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.signInWithGoogle(context) { success, msg ->
                                if (success) {
                                    val profile = viewModel.currentUserProfile.value
                                    val isAdmin = profile?.role == "admin"
                                    onUnlocked(isAdmin)
                                } else if (msg != null) {
                                    showError = true
                                    errorMessage = msg
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isAuthLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F2937)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCBD5E1))
                    ) {
                        if (isAuthLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF4285F4))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Registrando con Google...", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GoogleIconBadge(modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Registrarse o Entrar con Google",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "💡 David: 046 | General: 1234 | Admin: admin123",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Creator Credits Panel requested by user
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "👑 Créditos de los Creadores",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Desarrollado por: La Operativa",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "y los hermanos: el N",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBackgroundView(backgroundKey: String, modifier: Modifier = Modifier) {
    when (backgroundKey) {
        "PRESET_DEFAULT" -> {
            Image(
                painter = painterResource(id = R.drawable.img_app_bg_1784787034073),
                contentDescription = "Fondo de la App",
                modifier = modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        "PRESET_NEON" -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F051D),
                                Color(0xFF1B0A3A),
                                Color(0xFF2E0854),
                                Color(0xFF001122)
                            )
                        )
                    )
            )
        }
        "PRESET_GOLD" -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A1500),
                                Color(0xFF2C2200),
                                Color(0xFF3E3100),
                                Color(0xFF141000)
                            )
                        )
                    )
            )
        }
        else -> {
            coil.compose.AsyncImage(
                model = backgroundKey,
                contentDescription = "Fondo Personalizado",
                modifier = modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.img_app_bg_1784787034073)
            )
        }
    }
}
