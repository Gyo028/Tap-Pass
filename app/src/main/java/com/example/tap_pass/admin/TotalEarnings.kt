package com.example.tap_pass.admin

data class TotalEarnings(
    val amount: Double = 0.0,
    val totalTransactions: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)