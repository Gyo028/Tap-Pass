package com.example.tap_pass.admin

import com.google.firebase.Timestamp

data class CustomerActivity(
    var fullName: String = "",
    val pcNumber: String = "",
    val status: String = "", // Matches "STARTED" or "ENDED"
    val startTime: com.google.firebase.Timestamp? = null,
    val endTime: com.google.firebase.Timestamp? = null
)