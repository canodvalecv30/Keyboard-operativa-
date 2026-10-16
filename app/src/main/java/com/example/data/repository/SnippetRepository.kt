package com.example.data.repository

import com.example.data.db.CategoryDao
import com.example.data.db.SnippetDao
import com.example.data.model.CategoryEntity
import com.example.data.model.SnippetEntity
import kotlinx.coroutines.flow.Flow

class SnippetRepository(
    private val categoryDao: CategoryDao,
    private val snippetDao: SnippetDao
) {
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allSnippets: Flow<List<SnippetEntity>> = snippetDao.getAllSnippets()
    val favoriteSnippets: Flow<List<SnippetEntity>> = snippetDao.getFavoriteSnippets()

    fun getSnippetsByCategory(categoryId: Int): Flow<List<SnippetEntity>> {
        return snippetDao.getSnippetsByCategory(categoryId)
    }

    fun searchSnippets(query: String): Flow<List<SnippetEntity>> {
        return snippetDao.searchSnippets(query)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        snippetDao.deleteSnippetsByCategoryId(category.id)
        categoryDao.deleteCategory(category)
    }

    suspend fun insertSnippet(snippet: SnippetEntity): Long {
        return snippetDao.insertSnippet(snippet)
    }

    suspend fun updateSnippet(snippet: SnippetEntity) {
        snippetDao.updateSnippet(snippet)
    }

    suspend fun deleteSnippet(snippet: SnippetEntity) {
        snippetDao.deleteSnippet(snippet)
    }

    suspend fun deleteAllSnippets() {
        snippetDao.deleteAllSnippets()
    }

    suspend fun recordUsage(snippetId: Int) {
        snippetDao.incrementUsage(snippetId)
    }

    suspend fun toggleFavorite(snippet: SnippetEntity) {
        val updated = snippet.copy(isFavorite = !snippet.isFavorite)
        snippetDao.updateSnippet(updated)
    }
}
