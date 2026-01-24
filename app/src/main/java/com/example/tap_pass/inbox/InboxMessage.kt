package com.example.tap_pass.inbox

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class InboxMessage(
    var title: String = "",
    var message: String = "",

    // Use java.util.Date for easier formatting in the Adapter,
    // or keep Timestamp if your Adapter is already handling it.
    @ServerTimestamp
    var timestamp: Date? = null,

    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,

    var type: String = "",
    var userId: String = "" // Added this as your Fragment query filters by userId
)