package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryId: Int,
    val title: String,
    val content: String,
    val isFavorite: Boolean = false,
    val usageCount: Int = 0,
    val lastUsedTimestamp: Long = System.currentTimeMillis(),
    val createdTimestamp: Long = System.currentTimeMillis()
)
