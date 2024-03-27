package com.example.finwise_app


import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.FragmentContainerView
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction



class Navigation : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_view)

        // Set the default fragment to be loaded
        loadFragment(ExpenseActivity(), supportFragmentManager)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(ExpenseActivity(), supportFragmentManager)
                    true
                }

//                R.id.nav_graph -> {
//                    loadFragment(PieChartFragment(), supportFragmentManager)
//                    true
//                }
//
//                R.id.nav_remainder -> {
//                    loadFragment(RemainderFragment(), supportFragmentManager)
//                    true
//                }
//
                R.id.nav_profile -> {
                    loadFragment(profile(), supportFragmentManager)
                    true
                }

                else -> false
            }
        }



    }
    private fun loadFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}