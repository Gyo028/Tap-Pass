package com.example.tap_pass

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import java.text.NumberFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment() {

    private lateinit var balanceText: TextView
    private var currentBalance = 100.0 // Initial balance
    private lateinit var viewPager: ViewPager2
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var dotsIndicator: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private val images = listOf(R.drawable.ads1, R.drawable.ads2)

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
        val sendBtn: Button = view.findViewById(R.id.sendBtn)
        val receiveBtn: Button = view.findViewById(R.id.receiveBtn)
        val profileButton: ImageButton = view.findViewById(R.id.profileButton)
        val copyRfidButton: ImageButton = view.findViewById(R.id.copyRfidButton)
        viewPager = view.findViewById(R.id.carouselViewPager)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)

        val activity = requireActivity()
        val rfid = activity.intent.getStringExtra("rfid")
        val fullName = activity.intent.getStringExtra("fullName")
        val phone = activity.intent.getStringExtra("phone")

        welcomeText.text = "Welcome, $fullName!"
        userIdText.text = "rfid# $rfid"
        updateBalanceText()

        copyRfidButton.setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("RFID", rfid)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(activity, "RFID copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        loadBtn.setOnClickListener {
            val intent = Intent(activity, LoadNewActivity::class.java)
            loadAmountLauncher.launch(intent)
        }

        sendBtn.setOnClickListener {
            val intent = Intent(activity, SendActivity::class.java)
            startActivity(intent)
        }

        receiveBtn.setOnClickListener {
            val intent = Intent(activity, ReceiveQrActivity::class.java)
            intent.putExtra("rfid", rfid)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra("fullName", fullName)
            intent.putExtra("rfid", rfid)
            intent.putExtra("phone", phone)
            startActivity(intent)
        }

        setupCarousel()
        setupDots()

        return view
    }

    private fun setupCarousel() {
        carouselAdapter = CarouselAdapter(images)
        viewPager.adapter = carouselAdapter

        // Start at a large number to simulate infinite scrolling
        val startPosition = Int.MAX_VALUE / 2
        viewPager.setCurrentItem(startPosition, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position % images.size)
            }
        })

        // Auto-slide functionality
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

    private fun updateBalanceText() {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        balanceText.text = format.format(currentBalance)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}
