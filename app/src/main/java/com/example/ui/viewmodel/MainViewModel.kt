package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.SnippetEntity
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SnippetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = SnippetRepository(database.categoryDao(), database.snippetDao())
    val preferences = PreferencesRepository(application)

    private val _appBackground = MutableStateFlow(preferences.appBackground)
    val appBackground: StateFlow<String> = _appBackground

    fun updateAppBackground(newBackground: String) {
        preferences.appBackground = newBackground
        _appBackground.value = newBackground
    }

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSnippets: StateFlow<List<SnippetEntity>> = repository.allSnippets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<Int?>(null)

    val filteredSnippets: StateFlow<List<SnippetEntity>> = combine(
        allSnippets,
        searchQuery,
        selectedCategoryFilter
    ) { snippets, query, categoryId ->
        snippets.filter { snippet ->
            val matchesCategory = categoryId == null || snippet.categoryId == categoryId
            val matchesQuery = query.isBlank() ||
                    snippet.title.contains(query, ignoreCase = true) ||
                    snippet.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSnippet(title: String, content: String, categoryId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            val snippet = SnippetEntity(
                categoryId = categoryId,
                title = title.trim(),
                content = content,
                isFavorite = isFavorite
            )
            repository.insertSnippet(snippet)
        }
    }

    fun updateSnippet(snippet: SnippetEntity) {
        viewModelScope.launch {
            repository.updateSnippet(snippet)
        }
    }

    fun deleteSnippet(snippet: SnippetEntity) {
        viewModelScope.launch {
            repository.deleteSnippet(snippet)
        }
    }

    fun deleteAllSnippets() {
        viewModelScope.launch {
            repository.deleteAllSnippets()
        }
    }

    fun toggleFavorite(snippet: SnippetEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(snippet)
        }
    }

    fun addCategory(name: String, iconName: String = "chat", colorHex: String = "#6366F1") {
        viewModelScope.launch {
            val category = CategoryEntity(
                name = name.trim(),
                iconName = iconName,
                colorHex = colorHex
            )
            repository.insertCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun recordSnippetUsage(snippetId: Int) {
        viewModelScope.launch {
            repository.recordUsage(snippetId)
        }
    }

    fun importPresetBundle(newCategories: List<CategoryEntity>, newSnippets: List<SnippetEntity>) {
        viewModelScope.launch {
            newCategories.forEach { category ->
                val newCatId = repository.insertCategory(category).toInt()
                val relatedSnippets = newSnippets.filter { it.categoryId == category.id }
                relatedSnippets.forEach { snippet ->
                    repository.insertSnippet(snippet.copy(id = 0, categoryId = newCatId))
                }
            }
        }
    }
}
