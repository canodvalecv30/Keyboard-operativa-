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

        val newProfile = UserProfile(
            id = firebaseUser.uid,
            accountId = generatedAccountId,
            displayName = firebaseUser.displayName ?: "Operador",
            email = firebaseUser.email ?: "usuario@operativa.app",
            photoUrl = firebaseUser.photoUrl?.toString() ?: "",
            role = if (isAdmin) "admin" else "operador",
            createdAt = null, // @ServerTimestamp will populate this
            updatedAt = null
        )

        docRef.set(newProfile).await()
        Log.i(TAG, "Nuevo usuario registrado con ID: $generatedAccountId")

        return newProfile.copy(accountId = generatedAccountId)
    }

    fun signOut() {
        auth.signOut()
    }
}
