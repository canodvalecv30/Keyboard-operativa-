package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AccountProfileCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onAuthSuccess: (UserProfile) -> Unit = {}
) {
    val context = LocalContext.current
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF4285F4).copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cuenta de Usuario & ID",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Registro oficial con Google y Firebase",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (userProfile != null) {
                    val isActive = userProfile!!.isActive
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isActive) Color(0xFF10B981) else Color(0xFFEF4444))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isActive) "Activa" else "Inactiva",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            if (userProfile == null) {
                // Not registered / not signed in with Google yet
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Únete y obtén tu ID único de operador",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Al registrarte con tu cuenta de Google, se te asignará automáticamente un ID de cuenta único (ej: OP-86031) para identificarte en el sistema y sincronizar tus frases en la nube.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                viewModel.signInWithGoogle(context) { success, message ->
                                    if (success) {
                                        viewModel.currentUserProfile.value?.let { onAuthSuccess(it) }
                                        Toast.makeText(context, message ?: "¡Sesión iniciada con éxito!", Toast.LENGTH_LONG).show()
                                    } else if (message != null) {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isAuthLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F2937)
                            ),
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1))
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Conectando con Google...", color = Color(0xFF1F2937), fontWeight = FontWeight.Bold)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GoogleIconBadge(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Registrarse con Google",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val profile = userProfile!!

                // Display User Profile with Highlighted Account ID
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User Header (Avatar + Name + Role)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (profile.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = profile.displayName,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color(0xFF4285F4), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF4285F4),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = profile.displayName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.displayName.ifBlank { "Operador de la App" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = profile.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (profile.role == "admin") Color(0xFF8B5CF6).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (profile.role == "admin") "👑 Admin" else "🟢 Operador",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (profile.role == "admin") Color(0xFF8B5CF6) else Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // PROMINENT ACCOUNT ID BADGE (The core user request)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "ID DE CUENTA ASIGNADO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = profile.accountId,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("ID de Cuenta", profile.accountId)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "✅ ID copiado: ${profile.accountId}", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8))
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copiar ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Sign Out Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.signOutGoogle()
                                    Toast.makeText(context, "Sesión de Google cerrada", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cerrar Sesión", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    GoogleIconBadge(modifier = modifier)
}

@Composable
fun GoogleIconBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "G",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF4285F4)
            )
        }
    }
}
