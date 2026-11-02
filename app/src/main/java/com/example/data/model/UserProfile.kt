package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    @DocumentId
    val id: String = "",
    val accountId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: String = "operador",
    val isActive: Boolean = false,
    val activationCode: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
