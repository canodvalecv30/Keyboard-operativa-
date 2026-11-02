package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

class UserRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "UserRepository"
        private const val USERS_COLLECTION = "users"

        fun create(context: Context): UserRepository {
            val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            return UserRepository(firestore)
        }
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun observeProfile(userId: String): Flow<UserProfile?> {
        return db.collection(USERS_COLLECTION).document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
    }

    suspend fun getOrCreateProfile(firebaseUser: FirebaseUser): UserProfile {
        val docRef = db.collection(USERS_COLLECTION).document(firebaseUser.uid)
        val snapshot = docRef.get().await()

        if (snapshot.exists()) {
            val existing = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            if (existing != null) {
                return existing
            }
        }

        // Generate a recognizable, unique 5-digit account ID with prefix "OP-"
        val numericHash = abs(firebaseUser.uid.hashCode()) % 90000 + 10000
        val generatedAccountId = "OP-$numericHash"

        val isAdmin = firebaseUser.email?.contains("operativa", ignoreCase = true) == true ||
                firebaseUser.displayName?.contains("david", ignoreCase = true) == true

        val sixDigitCode = com.example.data.auth.AccountActivationManager.generateActivationCodeForId(generatedAccountId)

        val newProfile = UserProfile(
            id = firebaseUser.uid,
            accountId = generatedAccountId,
            displayName = firebaseUser.displayName ?: "Operador",
            email = firebaseUser.email ?: "usuario@operativa.app",
            photoUrl = firebaseUser.photoUrl?.toString() ?: "",
            role = if (isAdmin) "admin" else "operador",
            isActive = isAdmin, // Admins active by default, regular operators require activation
            activationCode = sixDigitCode,
            createdAt = null, // @ServerTimestamp will populate this
            updatedAt = null
        )

        docRef.set(newProfile).await()
        Log.i(TAG, "Nuevo usuario registrado con ID: $generatedAccountId (Activo: $isAdmin)")

        return newProfile.copy(accountId = generatedAccountId)
    }

    fun observeAllProfiles(): Flow<List<UserProfile>> {
        return db.collection(USERS_COLLECTION)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
            }
    }

    suspend fun updateUserActivation(userId: String, isActive: Boolean): Result<Unit> {
        return try {
            db.collection(USERS_COLLECTION).document(userId)
                .update(
                    mapOf(
                        "isActive" to isActive,
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user activation", e)
            Result.failure(e)
        }
    }

    suspend fun activateAccountWithCode(userId: String, accountId: String, code: String): Result<Boolean> {
        val isValid = com.example.data.auth.AccountActivationManager.validateActivationCode(accountId, code)
        if (!isValid) {
            return Result.success(false)
        }

        return try {
            db.collection(USERS_COLLECTION).document(userId)
                .update(
                    mapOf(
                        "isActive" to true,
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error activating account with code", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
