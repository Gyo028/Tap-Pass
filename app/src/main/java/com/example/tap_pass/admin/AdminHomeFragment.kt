package com.example.tap_pass.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tap_pass.R
import com.example.tap_pass.login_register.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.util.Locale

class AdminHomeFragment : Fragment() {

    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth // Added for Logout
    private var earningsListener: ListenerRegistration? = null
    private var pcListener: ListenerRegistration? = null

    private lateinit var earningsAmountText: TextView
    private lateinit var pcRecyclerView: RecyclerView
    private lateinit var pcAdapter: PCAdapter
    private val pcList = mutableListOf<PCUnit>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)

        fstore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() // Initialize Auth

        earningsAmountText = view.findViewById(R.id.earningsAmountText)
        pcRecyclerView = view.findViewById(R.id.pcRecyclerView)

        // --- Logout Button Logic ---
        val profileButton: ImageButton = view.findViewById(R.id.profileButton)
        profileButton.setOnClickListener { v ->
            showPopup(v)
        }
        // ---------------------------

        setupPCGrid()
        listenToEarnings()
        listenToPCStatus()

        return view
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.admin_profile_menu, popup.menu)

        // Force Icons to show via reflection
        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popup)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.javaPrimitiveType)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_logout -> {
                    logoutAdmin()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun logoutAdmin() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        // Clear activity stack so admin cannot press "back" to return to the dashboard
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    // ... (Keep your existing setupPCGrid, listenToEarnings, listenToPCStatus, and onDestroyView)

    private fun setupPCGrid() {
        pcRecyclerView.layoutManager = GridLayoutManager(context, 2)
        pcAdapter = PCAdapter(pcList)
        pcRecyclerView.adapter = pcAdapter
    }

    private fun listenToEarnings() {
        earningsListener = fstore.collection("load_topup")
            .whereEqualTo("status", "processed")
            .addSnapshotListener { snapshot, error ->
                if (!isAdded || view == null) return@addSnapshotListener
                if (error != null) {
                    Log.e("AdminHome", "Earnings Error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    var totalSum = 0.0
                    for (doc in snapshot.documents) {
                        val amount = doc.getDouble("amount") ?: 0.0
                        totalSum += amount
                    }
                    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                    earningsAmountText.text = format.format(totalSum)
                }
            }
    }

    private fun listenToPCStatus() {
        pcListener = fstore.collection("pcs")
            .addSnapshotListener { snapshot, error ->
                if (!isAdded || view == null) return@addSnapshotListener
                if (error != null) {
                    Log.e("AdminHome", "PC Status Error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    pcList.clear()
                    for (doc in snapshot.documents) {
                        val pc = doc.toObject(PCUnit::class.java)
                        pc?.let { unit ->
                            unit.docId = doc.id
                            pcList.add(unit)
                            val isOccupied = unit.status == "BUSY" || unit.status == "OCCUPIED"
                            val userId = unit.currentUserId ?: ""
                            if (isOccupied && userId.isNotEmpty()) {
                                fstore.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener { userDoc ->
                                        unit.userFullName = userDoc.getString("fullName") ?: "Unknown User"
                                        pcAdapter.notifyDataSetChanged()
                                    }
                            } else {
                                unit.userFullName = if (isOccupied) "Unknown User" else ""
                            }
                        }
                    }
                    pcAdapter.notifyDataSetChanged()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        earningsListener?.remove()
        pcListener?.remove()
    }
}