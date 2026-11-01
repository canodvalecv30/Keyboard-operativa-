package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

@Composable
fun FastDownloadDialog(
    onDismiss: () -> Unit,
    sharedAppUrl: String = "https://ais-pre-hw2ctukycu6m7nlag7kezp-139962952742.us-east1.run.app"
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=" +
            Uri.encode(sharedAppUrl)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Descarga Rápida al Celular",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instalación express sin esperas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📲 QR Instantáneo", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("⚡ Descargar Link", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("💡 Trucos Rápidos", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // QR Tab
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Escanea con la cámara de tu celular:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .size(190.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(2.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SubcomposeAsyncImage(
                                        model = qrApiUrl,
                                        contentDescription = "Código QR de Descarga",
                                        modifier = Modifier.fillMaxWidth(),
                                        contentScale = ContentScale.Fit,
                                        loading = {
                                            Box(
                                                modifier = Modifier.size(40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(strokeWidth = 2.dp)
                                            }
                                        },
                                        error = {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.QrCode,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(60.dp),
                                                    tint = Color.Black
                                                )
                                                Text(
                                                    "Abre el link directo abajo",
                                                    fontSize = 11.sp,
                                                    color = Color.Black,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    )
                                }

                                Text(
                                    text = "Abre inmediatamente la app en tu teléfono sin cables ni configuraciones.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    1 -> {
                        // Direct Download Link Tab
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "🔗 Enlace Directo de la Aplicación",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = sharedAppUrl,
                                        modifier = Modifier.padding(10.dp),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 2
                                    )
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Enlace de Descarga", sharedAppUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "✅ Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copiar Enlace Directo", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Descarga la aplicación Spam de la Operativa")
                                            putExtra(Intent.EXTRA_TEXT, "Aquí tienes el enlace directo para descargar y usar la aplicación: $sharedAppUrl")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartir enlace por:"))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Compartir por WhatsApp / Telegram", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sharedAppUrl))
                                        context.startActivity(browserIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Abrir en el Navegador", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    2 -> {
                        // Tips Tab
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🚀 ¿Cómo pasarlo en menos de 10 segundos?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "1. Exportar APK en 1 Clic: En la esquina superior derecha de AI Studio, toca los 3 puntos (o Ajustes) y selecciona 'Download APK' / 'Export as APK'. El archivo se baja directo.",
                                        fontSize = 12.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "2. Evitar el escaneo lento de Play Protect: Al instalar, si Google Play Protect dice 'App no verificada', toca 'Más detalles' -> 'Instalar de todas formas' inmediatamente sin esperar.",
                                        fontSize = 12.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "3. Pasar por WhatsApp: Si descargas el APK en tu PC, arrástralo a tu chat personal de WhatsApp Web y en tu celular solo tócalo para instalar.",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Entendido")
            }
        }
    )
}
