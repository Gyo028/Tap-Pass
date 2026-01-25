package com.example.tap_pass.admin

data class PCUnit(
    var docId: String = "",
    val status: String = "AVAILABLE",
    val currentUserId: String? = null,
    var userFullName: String = "Loading..."
)



