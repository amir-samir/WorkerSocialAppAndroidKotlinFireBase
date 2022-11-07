package com.example.menshanalla

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.Adapter.FollowingPostsParentAdapter
import com.example.menshanalla.Model.FollowingPosting
import com.example.menshanalla.Model.Posts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * in this Activity the users see the Posts the users they follow
 *
 * @author Amir Azim
 *
 *
 */

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [homeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class homeFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var postsAdapter: FollowingPostsParentAdapter? = null
    private var followingIds: ArrayList<String>? = null
    private var fullUserName: String? = null
    private var imageUri: String? = null
    private var posts: MutableList<Posts>? = null
    private var nameAndImage: HashMap<String, MutableList<Posts>?>? = null
    private var saveUid: String? = null
    private var finalPosts: MutableList<FollowingPosting>? = null
    private lateinit var empty: TextView
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        posts = mutableListOf()
        finalPosts = mutableListOf()
        nameAndImage = HashMap()
        followingIds = ArrayList()
        recyclerView = view.findViewById(R.id.recycler_view_home1)
        recyclerView?.setHasFixedSize(false)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        empty = view.findViewById(R.id.empty)

        getFollwingListOfIds()
        //gettingFollowerNameAndImage()
        //gettingFollowersPosts()

        return view
    }

    private fun getFollwingListOfIds(){
        val countFollowers =
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")

        countFollowers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists())
                {
                   for (document in snapshot.children){

                       followingIds!!.add(document.key.toString())
                       saveUid = document.key.toString()
                       val firestore = FirebaseFirestore.getInstance()
                       firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
                       firestore.collection(document.key.toString())
                           .get()
                           .addOnSuccessListener { result ->
                               for (document1 in result) {
                                   //Log.d(TAG, "${document.id} => ${document.data}")
                                   //Log.d(TAG, "${document.data}")
                                   val dataclasses = document1.toObject(Posts::class.java)
                                   posts!!.add(dataclasses)
                                   val followpost = FollowingPosting(document.key,posts!!)
                                   finalPosts!!.add(followpost)
                                   postsAdapter = context?.let {
                                       FollowingPostsParentAdapter(
                                           it,
                                           finalPosts
                                       )
                                   }
                                   recyclerView?.adapter = postsAdapter

                                   //Log.d(ContentValues.TAG, posts.toString())
                               }
                               if (posts!!.isNotEmpty()){
                                   empty.visibility = View.GONE
                               }




                           }
                           .addOnFailureListener { exception ->
                               Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                           }


                       //gettingFollowerNameAndImage(document.key.toString())
                     //  gettingFollowerNameAndImage(document.key.toString())
                      // gettingFollowersPosts(document.key.toString())
                   }
                   // gettingFollowersPosts()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun gettingFollowerNameAndImage(userid: String){
            val ref = FirebaseDatabase.getInstance().getReference("Workers")
            ref.child(userid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user info if they are not equal to null
                        val job = "${snapshot.child("workerJob").value}"
                        val fullName = "${snapshot.child("workerName").value}"
                        val profileImage = "${snapshot.child("profileImage").value}"
                        val uid = "${snapshot.child("workerId").value}"
                        val workPlaces = "${snapshot.child("workerPlaces").value}"
                        imageUri = profileImage
                        fullUserName = fullName
                        Log.d(ContentValues.TAG, imageUri!!)
                        Log.d(ContentValues.TAG, fullUserName!!)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })


    }

    private fun gettingFollowersPosts() {

        for (i in 0 until followingIds!!.size) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
            firestore.collection(followingIds!![i])
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        //Log.d(TAG, "${document.id} => ${document.data}")
                        //Log.d(TAG, "${document.data}")
                        val dataclasses = document.toObject(Posts::class.java)
                        posts!!.add(dataclasses)

                        //Log.d(ContentValues.TAG, posts.toString())
                    }


                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }
    }



}