package com.example.tap_pass.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tap_pass.admin.CustomerActivityFragment
import com.example.tap_pass.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_home -> selectedFragment = AdminFragment()
                R.id.navigation_request -> selectedFragment = RequestFragment()
                R.id.navigation_activity -> selectedFragment = CustomerActivityFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, selectedFragment).commit()
            }
            true
        }

        // Set the default fragment
        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.navigation_home
        }
    }
}
