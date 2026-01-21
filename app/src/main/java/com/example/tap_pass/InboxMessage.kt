package com.example.tap_pass

import com.google.firebase.Timestamp

data class InboxMessage(
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false,
    val type: String = ""
)