package com.example.menshanalla.CustomerWorker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.menshanalla.*
import com.example.menshanalla.Posts.PostsActivity
import com.example.menshanalla.Projects.ProjectsActivity
import com.example.menshanalla.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * this is the main Activity of a Worker
 *
 * @author Amir Azim, Adnan Alqalaq
 *
 *
 */

class MainActivity : AppCompatActivity() {



    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

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
            R.id.nav_projects -> {
               startActivity(Intent(this, ProjectsActivity::class.java))
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
        //editBackPressed = "false"
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        firebaseAuth = FirebaseAuth.getInstance()
        changeFragment(homeFragment())





        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    fun changeFragment(fragment: Fragment){
        val fragmentChange = supportFragmentManager.beginTransaction()
        fragmentChange.replace(R.id.fragment_container, fragment)
        fragmentChange.commit()
    }



    private fun loadUserInfo() {
        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Workers")
          ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info if they are not equal to null
                  if("${snapshot.child("backFromEditProfilePressed").value}".equals("true")){
                      supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment())
                      .commit()
                  }

                    val hashmap: HashMap<String, Any> = HashMap()
                    hashmap["backFromEditProfilePressed"] = "false"

                    val reference = FirebaseDatabase.getInstance().getReference("Workers")
                    reference.child(firebaseAuth.uid!!)
                        .updateChildren(hashmap)

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun changeValueOfProfileBackPressed() {
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["backFromEditProfilePressed"] = "false"

        val reference = FirebaseDatabase.getInstance().getReference("Workers")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                //profile updated
                Toast.makeText(this, "worked", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                //failed to update profile
                Toast.makeText(this, "Failed to update Profile due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }
}