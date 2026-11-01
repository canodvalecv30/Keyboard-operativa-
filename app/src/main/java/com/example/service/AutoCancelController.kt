package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global controller and event bus for cancelling automatic send / burst / loop operations
 * from any screen, from the full-screen floating button, or from the system overlay.
 */
object AutoCancelController {
    private val _isAutomaticActive = MutableStateFlow(false)
    val isAutomaticActive = _isAutomaticActive.asStateFlow()

    private val _statusText = MutableStateFlow("Cancelar")
    val statusText = _statusText.asStateFlow()

    private val cancelListeners = mutableListOf<() -> Unit>()

    fun registerListener(listener: () -> Unit) {
        synchronized(cancelListeners) {
            if (!cancelListeners.contains(listener)) {
                cancelListeners.add(listener)
            }
        }
    }

    fun unregisterListener(listener: () -> Unit) {
        synchronized(cancelListeners) {
            cancelListeners.remove(listener)
        }
    }

    fun notifyAutoStarted(status: String = "Cancelar Envío") {
        _isAutomaticActive.value = true
        _statusText.value = status
    }

    fun notifyAutoFinished() {
        _isAutomaticActive.value = false
        _statusText.value = "Cancelar"
    }

    fun triggerCancel() {
        val callbacks = synchronized(cancelListeners) {
            cancelListeners.toList()
        }
        callbacks.forEach {
            try {
                it.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _isAutomaticActive.value = false
        _statusText.value = "Cancelar"
    }
}
