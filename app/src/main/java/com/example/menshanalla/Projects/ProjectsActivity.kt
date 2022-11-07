package com.example.menshanalla.Projects

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.*
import com.example.menshanalla.Adapter.ProjectsAdapter
import com.example.menshanalla.Model.PrjectData
import com.example.menshanalla.Model.ProjectAndId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.fragment_profile.*

/**
 * in this Activity the user see his Projects to choose them to edit or add a new project
 *
 * @author Amir Azim
 *
 *
 */

class ProjectsActivity : AppCompatActivity() {

    //Define RecyclerView & Adapter For Projects
    private var recyclerViewProjects: RecyclerView? = null
    private var projectsAdapter: ProjectsAdapter? = null
    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    //List Of Projects with their Ids(DataClass)
    private var projectAndId: MutableList<ProjectAndId>? = null
    private lateinit var empty: TextView
    private lateinit var editProjects_toolbar1: LinearLayout


    //define addProjectButton
  private lateinit var addProjectButton: ImageButton

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
        setContentView(R.layout.activity_projects)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        editProjects_toolbar1 = findViewById(R.id.editProjects_toolbar1)

        empty = findViewById(R.id.empty)

        //initialise addProjectButton
        addProjectButton = findViewById(R.id.addProject_button)

        //handle click on addProjectButton(open AddProjectActivity)
        addProjectButton.setOnClickListener {
            startActivity(Intent(this, AddProjectActivity::class.java))
        }

        //initialise the FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        //initialise a Mutablelist of Type projectAndId
        projectAndId = mutableListOf()
        //initialise recyclerView For Projects
        recyclerViewProjects = findViewById(R.id.recycler_edit_projects)
        recyclerViewProjects?.setHasFixedSize(true)
        recyclerViewProjects?.layoutManager = LinearLayoutManager(this)

        //get the Projects of the currentUsr from Firestore and put them(id+Project) in the RecyclerView
        getProjects()




    }

    /*get the Projects of the currentUsr from Firestore and put them(id+Project) in the RecyclerView */
    private fun getProjects() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        val idForServices = "Projects"+firebaseAuth.currentUser!!.uid
        firestore.collection(idForServices)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val dataclasses = document.toObject(PrjectData::class.java)
                    val saveProjectAndId = ProjectAndId(document.id,dataclasses)
                    projectAndId!!.add(saveProjectAndId)
                }
                if (projectAndId!!.isNotEmpty()) {
                    empty.visibility = View.GONE
                    projectsAdapter = ProjectsAdapter(this, projectAndId!!)
                    recyclerViewProjects?.adapter = projectsAdapter
                }
                else{
                    empty.visibility = View.VISIBLE
                }



            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }


    /*this function takes a fragment and replace it inside the framelayout(fragment_container_projects) to change fragments on click on the navbar items*/
    fun changeFragment(fragment: Fragment){
        empty.visibility = View.GONE
        editProjects_toolbar1.visibility = View.GONE
        recyclerViewProjects!!.visibility = View.GONE
        val fragmentChange = supportFragmentManager.beginTransaction()
        fragmentChange.replace(R.id.fragment_container, fragment)
        fragmentChange.commit()
    }

}