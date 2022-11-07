package com.example.menshanalla.Posts

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.Adapter.YourPostsAdapter
import com.example.menshanalla.CustomerWorker.MainActivity
import com.example.menshanalla.Model.PostAndId
import com.example.menshanalla.Model.Posts
import com.example.menshanalla.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * in this Activity the users see his Posts to choose them to edit
 *
 * @author Amir Azim, Adnan Alqalaq
 *
 *
 */

class YourPostsActivity : AppCompatActivity() {

    //Define RecyclerView & Adapter For Posts
    private var recyclerView: RecyclerView? = null
    private var yourPostsAdapter: YourPostsAdapter? = null
    //Define FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    //Define a mutableList of type PostAndId (Data Class to save the posts(description and array of photos) and document id)
    private var posts: MutableList<PostAndId>? = null
    //Define the cancel Button
    private lateinit var cancelButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_posts)

        //initialise recyclerView For Posts
        recyclerView = findViewById(R.id.recycler_your_projects)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        //Initialise cancelButton
        cancelButton = findViewById(R.id.cancel_button_yourProjects)

        //Initialise firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        //Initialise list of PostAndId
        posts = mutableListOf()

        //call getPosts Function
        getPosts()

        //handle click on cancel Button
        cancelButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }


    /*get the posts of the current user from firestore*/
    private fun getPosts() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        firestore.collection(firebaseAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    //Log.d(TAG, "${document.id} => ${document.data}")
                    //Log.d(TAG, "${document.data}")
                    val dataclasses = document.toObject(Posts::class.java)
                    val postAndId = PostAndId(document.id,dataclasses)
                    posts!!.add(postAndId)
                    yourPostsAdapter =  YourPostsAdapter(this,posts!!)
                    recyclerView?.adapter = yourPostsAdapter
                }

                Log.d(ContentValues.TAG, posts.toString())


            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
}