package com.example.tap_pass

import com.google.firebase.Timestamp

data class Transaction(
    val amount: Double = 0.0,
    val createdAt: com.google.firebase.Timestamp? = null,
    val otherUserRfid: String = "",
    val otherUserName: String = "", // Make sure this is here!
    val type: String = ""
)