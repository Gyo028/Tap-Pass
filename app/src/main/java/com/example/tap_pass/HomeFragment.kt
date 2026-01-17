package com.example.tap_pass

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var balanceText: TextView
    private var currentBalance = 0.0

    // This launcher is now deprecated since the loading is manual review
    private val loadAmountLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // We don't automatically update balance anymore, but the launcher is kept for now.
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val welcomeText: TextView = view.findViewById(R.id.welcomeText)
        val userIdText: TextView = view.findViewById(R.id.userIdText)
        balanceText = view.findViewById(R.id.balanceText)
        val loadBtn: Button = view.findViewById(R.id.loadBtn)
        val profileButton: ImageButton = view.findViewById(R.id.profileButton)

        val activity = requireActivity()
        val username = activity.intent.getStringExtra("username")
        val rfid = activity.intent.getStringExtra("rfid")
        val fullName = activity.intent.getStringExtra("fullName")
        val phone = activity.intent.getStringExtra("phone")

        welcomeText.text = "Welcome, $username!"
        userIdText.text = "rfid# $rfid"
        updateBalanceText()

        loadBtn.setOnClickListener {
            val intent = Intent(activity, LoadNewActivity::class.java)
            loadAmountLauncher.launch(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra("fullName", fullName)
            intent.putExtra("username", username)
            intent.putExtra("rfid", rfid)
            intent.putExtra("phone", phone)
            startActivity(intent)
        }

        return view
    }

    private fun updateBalanceText() {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        balanceText.text = format.format(currentBalance)
    }
}
