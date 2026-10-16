package com.example.data.db

import com.example.data.model.CategoryEntity
import com.example.data.model.SnippetEntity

object PrepopulatedData {

    val defaultCategories = listOf(
        CategoryEntity(id = 1, name = "Respuestas Rápidas", iconName = "chat", colorHex = "#6366F1", orderIndex = 0),
        CategoryEntity(id = 2, name = "Social & Hashtags", iconName = "share", colorHex = "#EC4899", orderIndex = 1),
        CategoryEntity(id = 3, name = "Gaming & Spam", iconName = "sports_esports", colorHex = "#8B5CF6", orderIndex = 2),
        CategoryEntity(id = 4, name = "E-Mail & Contacto", iconName = "email", colorHex = "#06B6D4", orderIndex = 3),
        CategoryEntity(id = 5, name = "Kaomoji & Emojis", iconName = "face", colorHex = "#F59E0B", orderIndex = 4),
        CategoryEntity(id = 6, name = "Macros & Fechas", iconName = "event", colorHex = "#10B981", orderIndex = 5)
    )

    val defaultSnippets = emptyList<SnippetEntity>()
}
