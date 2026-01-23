package com.example.tap_pass

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var balanceText: TextView
    private lateinit var welcomeText: TextView
    private lateinit var userIdText: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var dotsIndicator: LinearLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout // Added

    private lateinit var fstore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private val images = listOf(R.drawable.advertise1, R.drawable.advertise2)

    // Buttons
    private lateinit var loadBtn: Button
    private lateinit var sendBtn: Button
    private lateinit var receiveBtn: Button
    private lateinit var profileButton: ImageButton
    private lateinit var copyRfidButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        balanceText = view.findViewById(R.id.balanceText)
        welcomeText = view.findViewById(R.id.welcomeText)
        userIdText = view.findViewById(R.id.userIdText)
        viewPager = view.findViewById(R.id.carouselViewPager)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout) // Added

        loadBtn = view.findViewById(R.id.loadBtn)
        sendBtn = view.findViewById(R.id.sendBtn)
        receiveBtn = view.findViewById(R.id.receiveBtn)
        profileButton = view.findViewById(R.id.profileButton)
        copyRfidButton = view.findViewById(R.id.copyRfidButton)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        fstore = FirebaseFirestore.getInstance()

        // Setup SwipeRefreshLayout colors
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.purple_500))

        // Set Refresh Listener
        swipeRefreshLayout.setOnRefreshListener {
            fetchUserData()
        }

        // Initial data load
        fetchUserData()

        // Setup carousel
        setupCarousel()
        setupDots()

        return view
    }

    private fun fetchUserData() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            fstore.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName") ?: ""
                        val balance = document.getDouble("balance") ?: 0.0
                        val rfid = document.getString("rfidUid") ?: ""
                        val phone = document.getString("phone") ?: ""

                        // Welcome text (first name only)
                        val firstName = fullName.split(" ").firstOrNull() ?: fullName
                        welcomeText.text = "Welcome, $firstName!"

                        // Balance formatting
                        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                        balanceText.text = format.format(balance)

                        // RFID
                        userIdText.text = "RFID#: $rfid"

                        // Set up button listeners
                        copyRfidButton.setOnClickListener {
                            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("RFID", rfid)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(requireContext(), "RFID copied to clipboard", Toast.LENGTH_SHORT).show()
                        }

                        loadBtn.setOnClickListener {
                            startActivity(Intent(requireActivity(), LoadActivity::class.java))
                        }

                        sendBtn.setOnClickListener {
                            startActivity(Intent(requireActivity(), SendActivity::class.java))
                        }

                        receiveBtn.setOnClickListener {
                            val intent = Intent(requireActivity(), ReceiveQrActivity::class.java)
                            intent.putExtra("rfid", rfid)
                            startActivity(intent)
                        }

                        profileButton.setOnClickListener {
                            val intent = Intent(requireActivity(), ProfileActivity::class.java)
                            intent.putExtra("fullName", fullName)
                            intent.putExtra("rfid", rfid)
                            intent.putExtra("phone", phone)
                            startActivity(intent)
                        }

                    } else {
                        Log.d("HomeFragment", "No user document found")
                    }
                    // Stop refreshing animation
                    swipeRefreshLayout.isRefreshing = false
                }
                .addOnFailureListener { e ->
                    Log.e("HomeFragment", "Error fetching user data", e)
                    // Stop refreshing animation even on failure
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(requireContext(), "Failed to refresh data", Toast.LENGTH_SHORT).show()
                }
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupCarousel() {
        carouselAdapter = CarouselAdapter(images)
        viewPager.adapter = carouselAdapter

        val startPosition = Int.MAX_VALUE / 2
        viewPager.setCurrentItem(startPosition, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position % images.size)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                // Disable SwipeRefresh while the user is actively dragging the carousel horizontally
                swipeRefreshLayout.isEnabled = (state == ViewPager2.SCROLL_STATE_IDLE)
            }
        })

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                }
            }
        }, 3000, 3000)
    }

    private fun setupDots() {
        dotsIndicator.removeAllViews()
        for (i in images.indices) {
            val dot = ImageView(requireContext())
            dot.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive))
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(8, 0, 8, 0)
            dotsIndicator.addView(dot, params)
        }
    }

    private fun updateDots(currentPosition: Int) {
        for (i in 0 until dotsIndicator.childCount) {
            val dot = dotsIndicator.getChildAt(i) as ImageView
            val drawable = if (i == currentPosition) R.drawable.dot_active else R.drawable.dot_inactive
            dot.setImageDrawable(ContextCompat.getDrawable(requireContext(), drawable))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}