package com.example.menshanalla.CustomerWorker

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.menshanalla.*
import com.example.menshanalla.databinding.ActivityCustomerBinding

/**
 * this is the main Activity of a customer
 *
 * @author Amir Azim, Adnan Alqalaq
 *
 *
 */

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                changeFragment(homeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_search -> {
                changeFragment(SearchFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_chat -> {
                changeFragment(ChatFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_profile -> {
                changeFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }


        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navViewCustomer

        changeFragment(homeFragment())
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    fun changeFragment(fragment: Fragment){
        val fragmentChange = supportFragmentManager.beginTransaction()
        fragmentChange.replace(R.id.fragment_container, fragment)
        fragmentChange.commit()
    }
}