package com.example.data.auth

import kotlin.math.abs

/**
 * Manages 6-digit activation codes for account IDs,
 * including the universal master code "666999" and deterministic 6-digit admin codes.
 */
object AccountActivationManager {

    /**
     * Universal master activation code that activates any account ID.
     */
    const val MASTER_ACTIVATION_CODE = "666999"

    /**
     * Generates a unique, deterministic 6-digit code for a specific account ID (e.g. "OP-86031").
     * Always produces exactly 6 digits (between 100000 and 999999).
     */
    fun generateActivationCodeForId(accountId: String): String {
        val clean = accountId.uppercase().trim()
        val hash = abs(clean.hashCode())
        val codeNum = (hash % 900000) + 100000
        return codeNum.toString()
    }

    /**
     * Validates whether an entered code is valid for an account:
     * - Returns true if code == "666999" (Master Code)
     * - Returns true if code == generated 6-digit code for that account ID
     */
    fun validateActivationCode(accountId: String, inputCode: String): Boolean {
        val trimmed = inputCode.trim()
        if (trimmed == MASTER_ACTIVATION_CODE) {
            return true
        }
        val expected = generateActivationCodeForId(accountId)
        return trimmed == expected
    }

    /**
     * Builds pre-formatted WhatsApp share message for sending the ID to the admin.
     */
    fun buildMessageForAdmin(accountId: String, displayName: String, email: String): String {
        return "Hola Administrador de Spam de la Operativa, me acabo de registrar.\n\n" +
                "👤 Usuario: $displayName\n" +
                "📧 Correo: $email\n" +
                "🏷️ Mi ID de Cuenta es: $accountId\n\n" +
                "Por favor activa mi ID de cuenta o envíame mi código de 6 dígitos para ingresar a la aplicación."
    }

    /**
     * Builds pre-formatted WhatsApp message for an admin sending the 6-digit code to a user.
     */
    fun buildMessageForUser(accountId: String, code: String): String {
        return "¡Hola! Tu cuenta en Spam de la Operativa ha sido aprobada.\n\n" +
                "🏷️ ID de Cuenta: $accountId\n" +
                "🔑 Tu Código de Activación de 6 dígitos es: $code\n\n" +
                "Ingresa este código en la pantalla de la app para desbloquear tu acceso."
    }
}
