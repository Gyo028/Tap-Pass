package com.example.tap_pass.admin

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.Date

// Serializable allows us to pass this object via Bundle to the Detail Fragment
data class TopUpRequest(
    @get:Exclude var docId: String = "",
    val userId: String = "",
    val fullName: String = "",
    val amount: Double = 0.0, // Changed to Double to match currency input
    val rfidUid: String = "",
    val status: String = "pending",
    val proof: String = "",   // This will store the raw string/Uri
    val remarks: String = "",
    @ServerTimestamp val timestamp: Date? = null
) : Serializable