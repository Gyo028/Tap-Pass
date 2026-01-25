package com.example.tap_pass.home_main

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.tap_pass.inbox.InboxFragment
import com.example.tap_pass.R
import com.example.tap_pass.sessions.SpendingFragment
import com.example.tap_pass.transactions.TransactionsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- ADDED THIS TO HIDE STATUS BAR ---
        hideSystemUI()

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener(navListener)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }
    }

    // Modern Immersive Mode Logic
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars())
                // Allows users to swipe down to temporarily see clock/battery
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private val navListener = NavigationBarView.OnItemSelectedListener { item ->
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        when (item.itemId) {
            R.id.navigation_home -> {
                if (currentFragment !is HomeFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                }
                true
            }
            R.id.navigation_inbox -> {
                if (currentFragment !is InboxFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, InboxFragment()).commit()
                }
                true
            }
            R.id.navigation_transactions -> {
                if (currentFragment !is TransactionsFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, TransactionsFragment()).commit()
                }
                true
            }
            R.id.navigation_spending -> {
                if (currentFragment !is SpendingFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SpendingFragment()).commit()
                }
                true
            }
            else -> false
        }
    }
}