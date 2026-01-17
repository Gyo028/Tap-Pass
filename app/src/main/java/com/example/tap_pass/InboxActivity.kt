package com.example.tap_pass

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class InboxActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // This will take the user back to the previous screen
            finish()
        }
    }
}
