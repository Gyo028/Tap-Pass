package com.example.tap_pass

import com.google.firebase.Timestamp

data class SessionHistory(
    val pcNumber: String = "PC-Unknown",
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val totalCost: Double = 0.0,
    val status: String = ""
)