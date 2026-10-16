package com.example.service

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.db.AppDatabase
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SnippetRepository
import com.example.ui.keyboard.AutoPasteKeyboardLayout
import com.example.ui.theme.AutoPasteTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AutoPasteInputMethodService : InputMethodService(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var repository: SnippetRepository? = null
    private var prefs: PreferencesRepository? = null
    private val currentPackageNameFlow = kotlinx.coroutines.flow.MutableStateFlow("")

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateRegistryController.performRestore(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        initViewTreeOwners()
        try {
            val database = AppDatabase.getDatabase(this)
            repository = SnippetRepository(database.categoryDao(), database.snippetDao())
            prefs = PreferencesRepository(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initViewTreeOwners() {
        try {
            window?.window?.decorView?.let { decorView ->
                decorView.setViewTreeLifecycleOwner(this)
                decorView.setViewTreeViewModelStoreOwner(this)
                decorView.setViewTreeSavedStateRegistryOwner(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateInputView(): View {
        initViewTreeOwners()
        val composeView = ComposeView(this)

        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        if (lifecycleRegistry.currentState == Lifecycle.State.CREATED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        val safeRepository = repository
        val safePrefs = prefs ?: PreferencesRepository(this)

        composeView.setContent {
            val categories = safeRepository?.allCategories?.collectAsState(initial = emptyList())?.value ?: emptyList()
            val snippets = safeRepository?.allSnippets?.collectAsState(initial = emptyList())?.value ?: emptyList()
            val activePkg by currentPackageNameFlow.collectAsState()
            val isTikTokApp = activePkg.lowercase().let { pkg ->
                pkg.contains("tiktok") || pkg.contains("musically") || pkg.contains("trill") || pkg.contains("zhiliao") || pkg.contains("aweme")
            }

            AutoPasteTheme(darkTheme = safePrefs.isDarkTheme) {
                AutoPasteKeyboardLayout(
                    categories = categories,
                    snippets = snippets,
                    autoSpace = safePrefs.autoSpaceAfterPaste,
                    autoEnter = safePrefs.autoEnterAfterPaste,
                    hapticsEnabled = safePrefs.hapticFeedbackEnabled,
                    keyHeightScale = safePrefs.keyHeightScale,
                    isTikTokDetected = isTikTokApp,
                    tikTokLiveOnlyMode = safePrefs.tikTokLiveOnlyMode,
                    initialBurstDelayMs = safePrefs.burstDelayMs,
                    initialPasteSpeedDelayMs = safePrefs.pasteSpeedDelayMs,
                    initialShowToastOnPaste = safePrefs.showToastOnPaste,
                    initialUltraFastMode = safePrefs.ultraFastMode,
                    initialGridMode = safePrefs.isGridMode,
                    initialAutoSendActive = safePrefs.isAutoSendActive,
                    initialColumnCount = safePrefs.gridColumnCount,
                    initialButtonsReversed = safePrefs.isButtonsReversed,
                    onUpdateButtonsReversed = { safePrefs.isButtonsReversed = it },
                    onUpdateBurstDelay = { newDelay ->
                        safePrefs.burstDelayMs = newDelay
                    },
                    onUpdatePasteSpeedDelay = { newDelay ->
                        safePrefs.pasteSpeedDelayMs = newDelay
                    },
                    onUpdateShowToastOnPaste = { safePrefs.showToastOnPaste = it },
                    onUpdateUltraFastMode = { safePrefs.ultraFastMode = it },
                    onInsertText = { text ->
                        var didCommit = false
                        try {
                            val ic = currentInputConnection
                            if (ic != null) {
                                ic.beginBatchEdit()
                                ic.commitText(text, 1)
                                didCommit = true
                                ic.endBatchEdit()
                            }
                        } catch (e: Exception) {
                            if (!didCommit) {
                                try {
                                    currentInputConnection?.commitText(text, 1)
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    onFastPasteAndSend = { text, autoSend ->
                        try {
                            val ic = currentInputConnection
                            if (ic != null) {
                                var didCommit = false
                                try {
                                    ic.beginBatchEdit()
                                    ic.commitText(text, 1)
                                    didCommit = true
                                    ic.endBatchEdit()
                                } catch (e: Exception) {
                                    if (!didCommit) {
                                        try {
                                            ic.commitText(text, 1)
                                        } catch (_: Exception) {}
                                    }
                                }

                                if (autoSend) {
                                    val editorInfo = currentInputEditorInfo
                                    val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_UNSPECIFIED
                                    if (action == EditorInfo.IME_ACTION_SEND || action == EditorInfo.IME_ACTION_GO || action == EditorInfo.IME_ACTION_DONE) {
                                        ic.performEditorAction(action)
                                    } else {
                                        val sendHandled = ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
                                        if (!sendHandled) {
                                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onDeleteLastChar = {
                        try {
                            val ic = currentInputConnection
                            if (ic != null) {
                                ic.beginBatchEdit()
                                ic.deleteSurroundingText(1, 0)
                                ic.endBatchEdit()
                            }
                        } catch (e: Exception) {
                            currentInputConnection?.deleteSurroundingText(1, 0)
                        }
                    },
                    onInsertNewline = {
                        try {
                            val ic = currentInputConnection
                            val editorInfo = currentInputEditorInfo
                            if (ic != null) {
                                val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_UNSPECIFIED
                                if (action == EditorInfo.IME_ACTION_SEND || action == EditorInfo.IME_ACTION_GO || action == EditorInfo.IME_ACTION_DONE) {
                                    ic.performEditorAction(action)
                                } else {
                                    val sendHandled = ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
                                    if (!sendHandled) {
                                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onClearAllText = {
                        try {
                            val ic = currentInputConnection
                            if (ic != null) {
                                ic.beginBatchEdit()
                                ic.performContextMenuAction(android.R.id.selectAll)
                                ic.commitText("", 1)
                                ic.endBatchEdit()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onOpenSettings = {
                        try {
                            val intent = packageManager.getLaunchIntentForPackage(packageName)
                            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (intent != null) {
                                startActivity(intent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onSnippetUsed = { snippet ->
                        safeRepository?.let { repo ->
                            serviceScope.launch {
                                try {
                                    repo.recordUsage(snippet.id)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                )
            }
        }

        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentPackageNameFlow.value = info?.packageName ?: ""
        initViewTreeOwners()
        when (lifecycleRegistry.currentState) {
            Lifecycle.State.CREATED -> {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
            Lifecycle.State.STARTED -> {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
            else -> {}
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        if (lifecycleRegistry.currentState == Lifecycle.State.RESUMED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            serviceScope.cancel()
        } catch (e: Exception) {}
        if (lifecycleRegistry.currentState == Lifecycle.State.RESUMED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        if (lifecycleRegistry.currentState == Lifecycle.State.STARTED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
