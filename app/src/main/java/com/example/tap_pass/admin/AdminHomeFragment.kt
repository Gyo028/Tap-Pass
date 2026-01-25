package com.example.tap_pass.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tap_pass.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.util.Locale

class AdminHomeFragment : Fragment() {

    private lateinit var fstore: FirebaseFirestore
    private var earningsListener: ListenerRegistration? = null
    private lateinit var earningsAmountText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ensure this layout filename matches your XML (e.g., fragment_admin_home.xml)
        val view = inflater.inflate(R.layout.fragment_admin, container, false)

        fstore = FirebaseFirestore.getInstance()
        earningsAmountText = view.findViewById(R.id.earningsAmountText)

        listenToEarnings()

        return view
    }

    private fun listenToEarnings() {
        earningsListener = fstore.collection("load_topup")
            .whereEqualTo("status", "processed")
            .addSnapshotListener { snapshot, error ->

                // SAFETY CHECK: If user switched tabs before Firestore finished, stop here.
                if (!isAdded || view == null) return@addSnapshotListener

                if (error != null) {
                    Log.e("AdminHome", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var sum = 0.0
                    val count = snapshot.size()

                    for (doc in snapshot.documents) {
                        val amount = doc.getDouble("amount") ?: 0.0
                        sum += amount
                    }

                    // Using your new TotalEarnings data class
                    val summary = TotalEarnings(
                        amount = sum,
                        totalTransactions = count
                    )

                    updateUI(summary)
                }
            }
    }

    private fun updateUI(summary: TotalEarnings) {
        // Double check UI reference is still valid
        if (isAdded && ::earningsAmountText.isInitialized) {
            val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
            earningsAmountText.text = format.format(summary.amount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop the listener immediately when the fragment is destroyed/swapped
        earningsListener?.remove()
    }
}