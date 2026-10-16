package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.keyboard.AutoPasteKeyboardLayout
import com.example.ui.viewmodel.MainViewModel

@Composable
fun KeyboardSandboxScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit
) {
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

    val categories by viewModel.categories.collectAsState()
    val snippets by viewModel.allSnippets.collectAsState()

    var testInputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Sandbox Info & Input Box
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Tablero Teclado Auto-Pegado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Abre o cierra el teclado automáticamente y toca cualquier snippet para pegar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Auto Keyboard Open & Close Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                        Toast.makeText(context, "⚡ Teclado abierto automáticamente", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Abrir Teclado",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🟢 Abrir Teclado", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        Toast.makeText(context, "⚡ Teclado cerrado automáticamente", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardHide,
                        contentDescription = "Cerrar Teclado",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🔴 Cerrar Teclado", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Interactive Live Test Box
            OutlinedTextField(
                value = testInputText,
                onValueChange = { testInputText = it },
                label = { Text("Campo de Prueba de Pegado") },
                placeholder = { Text("Los snippets pegados aparecerán aquí...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .focusRequester(focusRequester),
                trailingIcon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (testInputText.isNotBlank()) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("AutoPaste Text", testInputText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "¡Texto copiado al portapapeles! 📋", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { testInputText = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpiar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            // Quick Actions Bar under input box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Caracteres: ${testInputText.length}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            val trimmed = testInputText.trim()
                            if (trimmed.isNotBlank()) {
                                val variosCat = categories.find { it.name.contains("Varios", ignoreCase = true) }
                                val catId = variosCat?.id ?: (categories.firstOrNull()?.id ?: 1)
                                val autoTitle = if (trimmed.length > 20) trimmed.take(20) + "..." else trimmed
                                viewModel.addSnippet(autoTitle, trimmed, catId, false)
                                Toast.makeText(context, "¡Frase guardada en Varios para auto-pegar! 💾", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        enabled = testInputText.isNotBlank()
                    ) {
                        Text("💾 Guardar en Varios", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            if (testInputText.isNotBlank()) {
                                Toast.makeText(context, "¡Mensaje simulado enviado! 🚀", Toast.LENGTH_SHORT).show()
                                testInputText = ""
                                // Auto close keyboard on send
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = testInputText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Custom Spam de la Operativa Keyboard
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            tonalElevation = 8.dp,
            shadowElevation = 6.dp
        ) {
            AutoPasteKeyboardLayout(
                categories = categories,
                snippets = snippets,
                autoSpace = viewModel.preferences.autoSpaceAfterPaste,
                autoEnter = viewModel.preferences.autoEnterAfterPaste,
                hapticsEnabled = viewModel.preferences.hapticFeedbackEnabled,
                keyHeightScale = viewModel.preferences.keyHeightScale,
                initialGridMode = viewModel.preferences.isGridMode,
                initialAutoSendActive = viewModel.preferences.isAutoSendActive,
                initialColumnCount = viewModel.preferences.gridColumnCount,
                initialButtonsReversed = viewModel.preferences.isButtonsReversed,
                onUpdateButtonsReversed = { viewModel.preferences.isButtonsReversed = it },
                initialBurstDelayMs = viewModel.preferences.burstDelayMs,
                onUpdateBurstDelay = { newDelay ->
                    viewModel.preferences.burstDelayMs = newDelay
                },
                onInsertText = { text ->
                    testInputText += text
                },
                onDeleteLastChar = {
                    if (testInputText.isNotEmpty()) {
                        testInputText = testInputText.dropLast(1)
                    }
                },
                onInsertNewline = {
                    testInputText += "\n"
                },
                onClearAllText = {
                    testInputText = ""
                },
                onOpenSettings = onOpenSettings,
                onSnippetUsed = { snippet ->
                    viewModel.recordSnippetUsage(snippet.id)
                }
            )
        }
    }
}
