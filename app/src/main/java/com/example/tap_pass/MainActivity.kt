package com.example.tap_pass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener(navListener)

        // Load the HomeFragment by default when the activity is first created
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
        }
    }

    private val navListener = NavigationBarView.OnItemSelectedListener { item ->
        // Find the currently displayed fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        when (item.itemId) {
            R.id.navigation_home -> {
                // Avoid reloading the fragment if it's already visible
                if (currentFragment !is HomeFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                }
                return@OnItemSelectedListener true
            }
            R.id.navigation_inbox -> {
                if (currentFragment !is InboxFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, InboxFragment()).commit()
                }
                return@OnItemSelectedListener true
            }
            R.id.navigation_transactions -> {
                if (currentFragment !is TransactionsFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, TransactionsFragment()).commit()
                }
                return@OnItemSelectedListener true
            }
            R.id.navigation_spending -> {
                if (currentFragment !is SpendingFragment) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SpendingFragment()).commit()
                }
                return@OnItemSelectedListener true
            }
        }
        false
    }
}
