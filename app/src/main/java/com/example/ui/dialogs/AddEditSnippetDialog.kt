package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.SnippetEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditSnippetDialog(
    categories: List<CategoryEntity>,
    snippetToEdit: SnippetEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, categoryId: Int, isFavorite: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(snippetToEdit?.title ?: "") }
    var content by remember { mutableStateOf(snippetToEdit?.content ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(snippetToEdit?.categoryId ?: (categories.firstOrNull()?.id ?: 1))
    }
    var isFavorite by remember { mutableStateOf(snippetToEdit?.isFavorite ?: false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Sin categoría"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (snippetToEdit == null) "Nuevo Snippet Spam de la Operativa" else "Editar Snippet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título (ej: Mi Correo, GG WP)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Texto / Respuesta para Pegar") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    minLines = 3,
                    maxLines = 5
                )

                // Macro Helper Chips
                Text(
                    text = "Insertar Variables Dinámicas / Macros:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "{saludo}" to "👋 Saludo",
                        "{envio}" to "⚡ Auto-Envío",
                        "{fecha}" to "📅 Fecha",
                        "{hora}" to "⏰ Hora",
                        "{portapapeles}" to "📋 Pegar Portapapeles",
                        "{datetime}" to "📆 Fecha+Hora"
                    ).forEach { (macro, label) ->
                        AssistChip(
                            onClick = { content += " $macro" },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    modifier = Modifier.height(14.dp)
                                )
                            }
                        )
                    }
                }

                // Favorite Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFavorite = !isFavorite }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Marcar como Favorito ⭐",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalContent = content.trim()
                    if (finalContent.isNotBlank()) {
                        val finalTitle = if (title.trim().isBlank()) {
                            if (finalContent.length > 25) finalContent.take(25) + "..." else finalContent
                        } else {
                            title.trim()
                        }
                        onSave(finalTitle, finalContent, selectedCategoryId, isFavorite)
                    }
                },
                enabled = content.isNotBlank()
            ) {
                Text("Guardar Frase", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
