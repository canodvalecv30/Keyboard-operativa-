package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthManager(
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "GoogleAuthManager"
    }

    private val auth = FirebaseAuth.getInstance()

    private fun Context.findActivity(): Activity? {
        var current = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    suspend fun signInWithGoogle(context: Context): Result<UserProfile?> {
        val activity = context.findActivity()
            ?: return Result.failure(Exception("Contexto de Activity no encontrado para lanzar selector de Google"))

        val serverClientId = activity.getString(R.string.default_web_client_id)
        val credentialManager = CredentialManager.create(activity)

        return try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user

                if (user != null) {
                    val profile = userRepository.getOrCreateProfile(user)
                    Result.success(profile)
                } else {
                    Result.failure(Exception("No se pudo obtener el usuario de Google"))
                }
            } else {
                Result.failure(Exception("Tipo de credencial no soportado"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "El usuario canceló el inicio de sesión con Google")
            Result.success(null) // User intentionally dismissed the account picker
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando sesión con Google", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        userRepository.signOut()
    }
}
