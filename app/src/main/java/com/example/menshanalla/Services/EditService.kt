package com.example.menshanalla.Services

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.Adapter.ServiceChooseEdit
import com.example.menshanalla.CustomerWorker.MainActivity
import com.example.menshanalla.Model.EditServicesData
import com.example.menshanalla.Model.Service
import com.example.menshanalla.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class EditService : AppCompatActivity() {

    //Define RecyclerView & Adapter For Posts in Profile
    private var recyclerViewEditService: RecyclerView? = null
    private var editServiceAdapter: ServiceChooseEdit? = null

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    //List Of Services(DataClass)
    private var services: MutableList<Service>? = null
    private var serviceAndId: MutableList<EditServicesData>? = null
    //Define the cancel Button
    private lateinit var cancelButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_service)

        //initialise the cancel Button
        cancelButton = findViewById(R.id.cancel_button)

        //initialise the FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        //initialise a Mutablelist of Type Service
        services = mutableListOf()
        //initialise a Mutablelist of Type EditServicesData(id + Service)
        serviceAndId = mutableListOf()

        //initialise recyclerView For Services in EditService
        recyclerViewEditService = findViewById(R.id.recycler_edit_services)
        recyclerViewEditService?.setHasFixedSize(true)
        recyclerViewEditService?.layoutManager = LinearLayoutManager(this)

        //get the Services for currentUsr from Firestore and put them(id+Service) in the RecyclerView
        getServices()

        //handle click on cancelButton(run main Activity)
        cancelButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }



    }

    /*get the Services for currentUsr from Firestore and put them(id+Service) in the RecyclerView */
    private fun getServices(){
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        val idForServices = "Services"+firebaseAuth.currentUser!!.uid
        firestore.collection(idForServices)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val dataclasses = document.toObject(Service::class.java)
                    val saveServiceAndId = EditServicesData(document.id,dataclasses)
                    serviceAndId!!.add(saveServiceAndId)
                }
                if (serviceAndId!!.isNotEmpty()) {
                    editServiceAdapter = ServiceChooseEdit(this, serviceAndId!!)
                    recyclerViewEditService?.adapter = editServiceAdapter
                }

                Log.d(ContentValues.TAG, services.toString())


            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
}