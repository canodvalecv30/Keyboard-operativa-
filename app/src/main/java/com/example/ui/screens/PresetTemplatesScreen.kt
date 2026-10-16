package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.SnippetEntity
import com.example.ui.viewmodel.MainViewModel

data class PresetPack(
    val title: String,
    val description: String,
    val iconName: String,
    val category: CategoryEntity,
    val snippets: List<SnippetEntity>
)

@Composable
fun PresetTemplatesScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val presetPacks = listOf(
        PresetPack(
            title = "Atención al Cliente & Ventas 💼",
            description = "Plantillas para responder preguntas frecuentes, saludar clientes y confirmar pedidos.",
            iconName = "store",
            category = CategoryEntity(name = "Soporte y Ventas", colorHex = "#06B6D4"),
            snippets = listOf(
                SnippetEntity(categoryId = 0, title = "Bienvenida Cliente", content = "¡Hola! Gracias por comunicarte con nosotros. ¿En qué te podemos ayudar hoy? 😊"),
                SnippetEntity(categoryId = 0, title = "Confirmación de Pedido", content = "¡Tu pedido ha sido recibido con éxito! En breve te enviaremos el número de guía para el rastreo. 📦✨"),
                SnippetEntity(categoryId = 0, title = "Horario de Atención", content = "Nuestro horario de atención es de Lunes a Viernes de 9:00 AM a 6:00 PM. ⏰"),
                SnippetEntity(categoryId = 0, title = "Métodos de Pago", content = "Aceptamos tarjetas de crédito, débito, transferencia bancaria y PayPal. 💳")
            )
        ),
        PresetPack(
            title = "Creador de Contenido & Hashtags 📸",
            description = "Packs de hashtags optimizados para Instagram, TikTok y YouTube Shorts.",
            iconName = "share",
            category = CategoryEntity(name = "Hashtags Creador", colorHex = "#EC4899"),
            snippets = listOf(
                SnippetEntity(categoryId = 0, title = "Hashtags Fotografía", content = "#photooftheday #photography #nature #picoftheday #instadaily #visuals #art"),
                SnippetEntity(categoryId = 0, title = "Hashtags Fitness & Salud", content = "#fitness #workout #motivation #gym #fitlife #health #lifestyle #training"),
                SnippetEntity(categoryId = 0, title = "Hashtags Comida / Foodie", content = "#foodie #instafood #delicious #foodstagram #yummy #recipe #foodlover"),
                SnippetEntity(categoryId = 0, title = "Llamado a la Acción IG", content = "💬 ¡Déjame tu opinión en los comentarios y guarda este post si te sirvió! 📌")
            )
        ),
        PresetPack(
            title = "Gamer Streamer & Spam Chat 🎮",
            description = "Respuestas rápidas para chat de Twitch, Discord y partidas de videojuegos.",
            iconName = "sports_esports",
            category = CategoryEntity(name = "Gamer Chat", colorHex = "#8B5CF6"),
            snippets = listOf(
                SnippetEntity(categoryId = 0, title = "GG WP", content = "GG WP! Menuda partida amigos 🔥🎮"),
                SnippetEntity(categoryId = 0, title = "Discord Server", content = "Únete a nuestra comunidad de Discord oficial: discord.gg/ejemplo 🚀"),
                SnippetEntity(categoryId = 0, title = "Comando Redes Streamer", content = "Sigue nuestras transmisiones en vivo todos los días a las 8 PM! 🎥⚡️"),
                SnippetEntity(categoryId = 0, title = "Rage Meme Chat", content = "¡NOS HICIERON UN CLUTCH INCREÍBLE! 😱🔥")
            )
        ),
        PresetPack(
            title = "Arte Kaomoji & Expresiones Japonesas ฅ^•ﻌ•^ฅ",
            description = "Emoticones ASCII clásicos para personalizar tus chats.",
            iconName = "face",
            category = CategoryEntity(name = "Kaomoji Art", colorHex = "#F59E0B"),
            snippets = listOf(
                SnippetEntity(categoryId = 0, title = "Bear Hug", content = "ʕっ•ᴥ•ʔっ"),
                SnippetEntity(categoryId = 0, title = "Disapproval", content = "(ಠ_ಠ)"),
                SnippetEntity(categoryId = 0, title = "Sparkles Joy", content = "(⁠★⁠_⁠★⁠)✨"),
                SnippetEntity(categoryId = 0, title = "Cat Dance", content = "(=^･ω･^=)")
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Biblioteca de Plantillas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Importa paquetes de snippets pre-hechos con 1-click para usarlos en tu teclado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(presetPacks) { pack ->
                PresetPackCard(
                    pack = pack,
                    onImport = {
                        viewModel.importPresetBundle(listOf(pack.category), pack.snippets)
                        Toast.makeText(
                            context,
                            "¡Paquete '${pack.category.name}' importado con éxito! 🎉",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetPackCard(
    pack: PresetPack,
    onImport: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = pack.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pack.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sample Snippets preview chips
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                pack.snippets.take(3).forEach { snippet ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "• ${snippet.title}: ${snippet.content}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pack.snippets.size} Snippets listos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(onClick = onImport) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importar Paquete", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
