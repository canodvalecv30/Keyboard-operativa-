package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.GoogleAuthManager
import com.example.data.db.AppDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.SnippetEntity
import com.example.data.model.UserProfile
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SnippetRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
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
    val onlineAuth = com.example.data.auth.OnlineAuthManager(application, preferences)

    // Firebase Auth & User Profile Management with Account ID
    val userRepository = UserRepository.create(application)
    val googleAuthManager = GoogleAuthManager(userRepository)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading

    val allUserProfiles: StateFlow<List<UserProfile>> = userRepository.observeAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                viewModelScope.launch {
                    try {
                        val profile = userRepository.getOrCreateProfile(user)
                        _currentUserProfile.value = profile
                        // Real-time listener for instant status updates from admin
                        userRepository.observeProfile(user.uid).collect { updatedProfile ->
                            if (updatedProfile != null) {
                                _currentUserProfile.value = updatedProfile
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                _currentUserProfile.value = null
            }
        }
    }

    fun activateAccount(code: String, onResult: (Boolean, String) -> Unit) {
        val current = _currentUserProfile.value ?: run {
            onResult(false, "No hay sesión activa")
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = userRepository.activateAccountWithCode(current.id, current.accountId, code)
            _isAuthLoading.value = false
            result.onSuccess { activated ->
                if (activated) {
                    _currentUserProfile.value = current.copy(isActive = true)
                    onResult(true, "✅ ¡Cuenta activada con éxito! Ya puedes usar la aplicación.")
                } else {
                    onResult(false, "❌ Código inválido. Solicita tu código de 6 dígitos al administrador o usa el código maestro 666999.")
                }
            }.onFailure { err ->
                onResult(false, "Error de activación: ${err.message}")
            }
        }
    }

    fun toggleUserActivation(userId: String, newActive: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.updateUserActivation(userId, newActive)
            result.onSuccess {
                val statusText = if (newActive) "activada" else "desactivada"
                onResult(true, "Cuenta $statusText con éxito")
            }.onFailure { err ->
                onResult(false, "Error al actualizar cuenta: ${err.message}")
            }
        }
    }

    fun signInWithGoogle(context: Context, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = googleAuthManager.signInWithGoogle(context)
            _isAuthLoading.value = false
            result.onSuccess { profile ->
                if (profile != null) {
                    _currentUserProfile.value = profile
                    val statusMsg = if (profile.isActive) "Cuenta Activa" else "Pendiente de Activación"
                    onResult(true, "¡Bienvenido ${profile.displayName}! ID: ${profile.accountId} ($statusMsg)")
                } else {
                    onResult(false, null)
                }
            }.onFailure { err ->
                onResult(false, err.message ?: "Error al autenticar con Google")
            }
        }
    }

    fun signOutGoogle() {
        googleAuthManager.signOut()
        _currentUserProfile.value = null
    }

    private val _onlineUsers = MutableStateFlow(onlineAuth.getUsers())
    val onlineUsers: StateFlow<List<com.example.data.auth.OnlineUser>> = _onlineUsers

    private val _isSyncingOnline = MutableStateFlow(false)
    val isSyncingOnline: StateFlow<Boolean> = _isSyncingOnline

    fun refreshOnlineUsers() {
        _onlineUsers.value = onlineAuth.getUsers()
    }

    fun syncOnline(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncingOnline.value = true
            val (success, message) = onlineAuth.syncWithOnlineServer()
            _onlineUsers.value = onlineAuth.getUsers()
            _isSyncingOnline.value = false
            onComplete(success, message)
        }
    }

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
