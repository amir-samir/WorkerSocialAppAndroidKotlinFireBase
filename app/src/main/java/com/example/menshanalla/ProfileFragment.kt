package com.example.menshanalla

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.menshanalla.LoginAndSignUp.LoginActivity
import com.example.handwerkeryarab.ProfileEditActivity
import com.example.menshanalla.Adapter.PostsParentAdapter
import com.example.menshanalla.Adapter.ServicesProfileAdapter
import com.example.menshanalla.Chat.ChatActivity
import com.example.menshanalla.Model.Posts
import com.example.menshanalla.Model.Service
import com.example.menshanalla.Model.WorkerModel
import com.example.menshanalla.Posts.PostsActivity
import com.example.menshanalla.Posts.YourPostsActivity
import com.example.menshanalla.Services.EditService
import com.example.menshanalla.Services.ServiceActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * in this fragment the user can see his profile and its used for other profiles
 *
 * @author Amir Azim, Adnan Alqalaq
 *
 *
 */

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    //Define RecyclerView & Adapter For Posts in Profile
    private var recyclerView: RecyclerView? = null
    private var postsAdapter: PostsParentAdapter? = null
    //Define RecyclerView & Adapter For Services in Profile
    private var recyclerViewServices: RecyclerView? = null
    private var serviceProfileAdapter: ServicesProfileAdapter? = null
    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    //FirebaseDataBase
    private lateinit var databaseReference: DatabaseReference
    //EditProfile Button
    private lateinit var profileEditBtn: ImageButton
    //logoutButton
    private lateinit var logoutBtn: Button

    private lateinit var nameTv: TextView

    private lateinit var jobTv: TextView

    private lateinit var jobTil: LinearLayout

    private lateinit var jobET: TextView

    private lateinit var address: TextView

    private lateinit var profileTv: ImageView

    private lateinit var contactbutton: ImageButton

    private lateinit var emailTv: TextView

    private lateinit var followers: TextView

    private lateinit var following: TextView

    private lateinit var totalRating: TextView

    private lateinit var numberOfRaters: TextView

    private lateinit var editAndAddServices: LinearLayout

    private lateinit var editAndAddPosts: LinearLayout

    private var uriLoadTest: ArrayList<List<String>>? = null


    //variable to save the userId
    private lateinit var userId: String
    private lateinit var secondUserName: String
    private lateinit var secondUserJob: String
    private lateinit var secondUserProfileImage: String
    private lateinit var currentUserName: String
    private lateinit var currentUserJob: String
    private lateinit var currentUserProfileImage: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var ratingBar: RatingBar
    private var ratingSum: Float = 0.0f
    private var ratingPersonNumber: Float = 0.0f
    private var posts: MutableList<Posts>? = null
    private var workerOrCustomer = ""

    private var services: MutableList<Service>? = null
    private lateinit var addService: ImageButton
    private lateinit var editService: ImageButton
    private lateinit var editPost: ImageButton
    private lateinit var addPost: ImageButton



    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)



        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)


        uriLoadTest = ArrayList()
        posts = mutableListOf()
        services = mutableListOf()
        logoutBtn = view.findViewById(R.id.logoutBtn)
        profileEditBtn = view.findViewById(R.id.profileEditBtn)
        nameTv = view.findViewById(R.id.nameTv)
        jobTv = view.findViewById(R.id.jobTv)
        jobTil = view.findViewById(R.id.jobTil)
        jobET = view.findViewById(R.id.jobEt)
        address = view.findViewById(R.id.addressProfile)
        ratingBar = view.findViewById(R.id.ratingWorker)
        profileTv = view.findViewById(R.id.profileTv)
        emailTv = view.findViewById(R.id.emailTv)
        contactbutton = view.findViewById(R.id.contactbutton)
        followers = view.findViewById(R.id.followers)
        following = view.findViewById(R.id.following)
        totalRating = view.findViewById(R.id.totalRating)
        numberOfRaters = view.findViewById(R.id.numberOfRaters)
        editAndAddPosts = view.findViewById(R.id.posts)
        editAndAddServices = view.findViewById(R.id.adressLayout)
        addService = view.findViewById(R.id.add_services)
        editService = view.findViewById(R.id.edit_services)
        addPost = view.findViewById(R.id.add_posts)
        editPost = view.findViewById(R.id.edit_posts)
        addPost.visibility = View.VISIBLE
        editPost.visibility = View.VISIBLE

        //initialise recyclerView For Posts in Profile
        recyclerView = view.findViewById(R.id.recycler_view_posts_parent)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        //initialise recyclerView For Services in Profile
        recyclerViewServices = view.findViewById(R.id.recycler_services_prices)
        recyclerViewServices?.setHasFixedSize(true)
        recyclerViewServices?.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null){
            this.userId = pref.getString("profileId", "none")!!
            val ref = FirebaseDatabase.getInstance().getReference("Workers")
            ref.child(userId)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user info if they are not equal to null
                        secondUserJob = "${snapshot.child("workerJob").value}"
                        secondUserName = "${snapshot.child("workerName").value}"
                        secondUserProfileImage = "${snapshot.child("profileImage").value}"

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                } )

            ref.child(firebaseUser.uid)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user info if they are not equal to null
                        currentUserJob = "${snapshot.child("workerJob").value}"
                        currentUserName = "${snapshot.child("workerName").value}"
                        currentUserProfileImage = "${snapshot.child("profileImage").value}"

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                } )
        }

        if (userId == firebaseUser.uid){
            logoutBtn.text = "Logout"
            contactbutton.visibility = View.GONE
            ratingBar.visibility = View.GONE

        }

        else if (userId != firebaseUser.uid){
            getFollowStatus()
            editAndAddPosts.visibility = View.VISIBLE
            editAndAddServices.visibility = View.VISIBLE
            addPost.visibility = View.GONE
            editPost.visibility = View.GONE
            addService.visibility = View.GONE
            editService.visibility = View.GONE
            ratingBar.visibility = View.VISIBLE
            profileEditBtn.visibility = View.GONE
            emailTv.visibility = View.INVISIBLE
        }

        val ref = FirebaseDatabase.getInstance().getReference("Workers")
        ref.child(userId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info if they are not equal to null
                    val userType = "${snapshot.child("userType").value}"
                    if (userType.equals("worker")){
                        workerOrCustomer = "worker"
                    }
                    if (userType.equals("customer")){
                        workerOrCustomer = "customer"
                        jobTv.visibility = View.GONE
                        jobET.visibility = View.GONE
                        jobTil.visibility = View.GONE
                        ratingBar.visibility = View.GONE
                        totalRating.visibility = View.GONE
                        numberOfRaters.visibility = View.GONE
                        editAndAddPosts.visibility = View.GONE
                        editAndAddServices.visibility = View.GONE
                        recyclerViewServices!!.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            } )

        getDocument()
        getRating()
        getServices()

        ratingBar.setOnRatingBarChangeListener  { ratingBar1, rating, fromUser ->

            firebaseUser?.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Rating")
                    .child(userId).child(firebaseUser.uid).setValue(rating)
            }

        }

        firebaseAuth = FirebaseAuth.getInstance()

        //check if user logged In
        checkUser()

        //Load the User Information from the Firebase RealDatabase
        loadUserInfo()



        //handle click on editServices
        editService.setOnClickListener {
            startActivity(Intent(context, EditService::class.java))
        }
        //handle click on add service
        addService.setOnClickListener {
            startActivity(Intent(context, ServiceActivity::class.java))
        }

        //handle click on edit posts
        editPost.setOnClickListener {
            startActivity(Intent(context, YourPostsActivity::class.java))
        }
        //handle click on add posts
        addPost.setOnClickListener {
            startActivity(Intent(context, PostsActivity::class.java))
        }

        //handle click logout
        logoutBtn.setOnClickListener {
            val checkButtonContent = logoutBtn.text.toString()
            if (  checkButtonContent == "Logout"){
                firebaseAuth.signOut()
                checkUser()
            }
            else if (checkButtonContent == "Follow"){
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(userId).setValue(true)
                }
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(userId)
                        .child("Followers").child(it1.toString()).setValue(true)
                }
                getNumberOfFollowers()
                getNumberOfFollowing()
            }
            else{
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(userId).removeValue()
                }
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(userId)
                        .child("Followers").child(it1.toString()).removeValue()
                }
                getNumberOfFollowers()
                getNumberOfFollowing()
            }

        }

        //handle click editButton
        profileEditBtn.setOnClickListener {
            val intent = Intent (activity, ProfileEditActivity::class.java)
            activity!!.startActivity(intent)
            activity!!.finish()
        }

        // handle contact me button
        contactbutton.setOnClickListener {
            databaseReference = FirebaseDatabase.getInstance().getReference("user")
            val worker1 = WorkerModel(userId,secondUserName,secondUserJob,"Place","worker/customer",secondUserProfileImage)
            // val worker = Worker(uid,"Full Name","Your Job","Place","Worker/Customer","",0.00,0.00,"false")
            if ( userId != null){
                databaseReference.child(firebaseUser.uid).child(userId).setValue(worker1).addOnCompleteListener {
                    if (it.isSuccessful){

                    }else{

                    }
                }
            }

            databaseReference = FirebaseDatabase.getInstance().getReference("user")
            val worker2 = WorkerModel(firebaseUser.uid,currentUserName,currentUserJob,"Place","worker/customer",secondUserProfileImage)
            if ( userId != null){
                databaseReference.child(userId).child(firebaseUser.uid).setValue(worker2).addOnCompleteListener {
                    if (it.isSuccessful){

                    }else{

                    }
                }
            }

            val intent = Intent(context, ChatActivity::class.java)

            intent.putExtra("name",secondUserName)
            intent.putExtra("uid",userId)
            context!!.startActivity(intent)
        }

        getNumberOfFollowers()
        getNumberOfFollowing()



        return view




    }

    private fun getFollowStatus() {

       val followStat = firebaseUser?.uid.let { it1 ->
           FirebaseDatabase.getInstance().reference
               .child("Follow")
               .child(it1.toString())
               .child("Following")
       }

        if (followStat != null){
            followStat.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                  if (snapshot.child(userId).exists())
                  {
                      logoutBtn.text = "Following"
                  }
                  else {
                      logoutBtn.text = "Follow"
                  }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun loadUserInfo() {
        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Workers")
        ref.child(userId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info if they are not equal to null
                    val job = "${snapshot.child("workerJob").value}"
                    val fullName = "${snapshot.child("workerName").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val uid = "${snapshot.child("workerId").value}"
                    val workPlaces = "${snapshot.child("workerPlaces").value}"

                    //set data
                    address.text = workPlaces
                    nameTv.text = fullName
                    jobTv.text = job

                    //set image
                    try{
                        Glide.with(this@ProfileFragment)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(profileTv)

                    }
                    catch (e: Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            } )
    }

    private fun checkUser() {
        //check if user is logged in
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            //user not null, user is logged in
            //get user info
            val email = firebaseUser.email
            //set to text View
            emailTv.text = email
        }
        else{
            //user is null, user is not loggedin -> return to Login
            //startActivity(Intent(this, LoginActivity::class.java))
            val intent = Intent (getActivity(), LoginActivity::class.java)
            getActivity()!!.startActivity(intent)
            getActivity()!!.finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //init firebaseAuth


    }

    private fun getNumberOfFollowers(){
        val countFollowers =
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(userId)
                .child("Followers")

        countFollowers.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists())
                {
                    followers.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getNumberOfFollowing(){
        val countFollowers =
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(userId)
                .child("Following")

        countFollowers.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists())
                {
                    following.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

   private fun getDocument() {
       val firestore = FirebaseFirestore.getInstance()
       firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
       firestore.collection(userId)
           .get()
           .addOnSuccessListener { result ->
               for (document in result) {
                   //Log.d(TAG, "${document.id} => ${document.data}")
                   //Log.d(TAG, "${document.data}")
                   val dataclasses = document.toObject(Posts::class.java)
                   posts!!.add(dataclasses)
                   postsAdapter = context?.let { PostsParentAdapter(it,posts!!) }
                   recyclerView?.adapter = postsAdapter
               }

               Log.d(TAG, posts.toString())


           }
           .addOnFailureListener { exception ->
               Log.d(TAG, "Error getting documents: ", exception)
           }
   }
  private fun getRating(){
      val countRaters =
          FirebaseDatabase.getInstance().reference
              .child("Rating")
              .child(userId)

      countRaters.addValueEventListener(object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {

              if (snapshot.exists())
              {
                  ratingSum = 0.0f
                  ratingPersonNumber = snapshot.children.count().toFloat()
                  for (document in snapshot.children){

                      Log.d(TAG, document.value.toString())
                      ratingSum += document.value.toString().toFloat()
                      Log.d(TAG, ratingSum.toString())

                  }

              }

             val totalRat = ratingSum.div(ratingPersonNumber)
              totalRating.setText("$totalRat")
              numberOfRaters.setText(Html.fromHtml("Rating<sup>${ratingPersonNumber.toInt()}</sup>" ))

              val countRaters1 =
                  FirebaseDatabase.getInstance().reference
                      .child("Rating")
                      .child(userId).addValueEventListener(object: ValueEventListener {
                          override fun onDataChange(snapshot: DataSnapshot) {
                              //get user info if they are not equal to null
                              if (snapshot.child(firebaseUser!!.uid).exists()) {
                                  val ratingOfCurrentUser =
                                      "${snapshot.child(firebaseUser!!.uid).value}"
                                  ratingBar.rating = ratingOfCurrentUser.toFloat()
                              }


                          }

                          override fun onCancelled(error: DatabaseError) {
                          }

                      } )
          }

          override fun onCancelled(error: DatabaseError) {

          }
      })
  }

    private fun getServices(){
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        val idForServices = "Services"+userId
        firestore.collection(idForServices)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    //Log.d(TAG, "${document.id} => ${document.data}")
                    //Log.d(TAG, "${document.data}")
                    val dataclasses = document.toObject(Service::class.java)
                    services!!.add(dataclasses)
                    serviceProfileAdapter = context?.let { ServicesProfileAdapter(it,services!!) }
                    recyclerViewServices?.adapter = serviceProfileAdapter
                }
                if (services!!.isEmpty()){
                    recyclerViewServices!!.visibility = View.GONE
                }

                Log.d(TAG, services.toString())


            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }


   }