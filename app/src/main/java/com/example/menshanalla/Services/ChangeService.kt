package com.example.menshanalla.Services

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.menshanalla.Model.Service
import com.example.menshanalla.databinding.ActivityChangeServiceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*

class ChangeService : AppCompatActivity() {

    //binding
    private lateinit var binding: ActivityChangeServiceBinding
    //define a variable to save the document id
    private var serviceId = ""
    //define fireBaseAut
    private lateinit var firebaseAuth: FirebaseAuth
    //define firebaseStorage
    private lateinit var storageReference: StorageReference
    //define fireStore
    private lateinit var firestore: FirebaseFirestore
    //Code For PickImages
    private val PICK_IMAGES_CODE = 0
    //Save the ImageUri
    private var imageUri1 = ""
    //Save LastImageUri
    private var imageUri1Last = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //initialise firebase Services
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        //define and initialise the shared preference from the recycler view
        val pref = this?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.serviceId = pref.getString("ServerId", "none")!!
        }


        //Set update Button And text for waiting Status Invisible
        binding.updateBtnChanges.visibility = View.VISIBLE
        binding.waitingstatusChanges.visibility = View.GONE

        //get from firestore the Picture,description and price of current Service
        val userIdServices = "Services"+firebaseAuth!!.uid
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        firestore.collection(userIdServices).document(serviceId).get()
            .addOnSuccessListener { result ->
                val dataclasses = result.toObject(Service::class.java)
                binding.imageDescriptionTEChanges.setText(dataclasses!!.description)
                binding.servicePriceChanges.setText(dataclasses!!.price)
                imageUri1Last = dataclasses.imageUri
                Picasso.get().load(dataclasses.imageUri).centerCrop().resize(binding.postImageViewChanges.width,
                    binding.postImageViewChanges.height).into(binding.postImageViewChanges)
            }


        //handle click on ImageView to open Gallery
        binding.postImageViewChanges.setOnClickListener {
            pickImages()
        }

        //handle click on update button
        binding.updateBtnChanges.setOnClickListener {
            val hashmap1 = hashMapOf<String,Any>()
            val description = binding.imageDescriptionTEChanges.text.toString()
            val price = binding.servicePriceChanges.text.toString()
            if (description.isEmpty()){
                Toast.makeText(this@ChangeService, "Please write the description of service", Toast.LENGTH_SHORT).show()
            }
            if (price.isEmpty()){
                Toast.makeText(this@ChangeService, "Please write the price of service", Toast.LENGTH_SHORT).show()
            }
            if (imageUri1 == "" && description.isNotEmpty() && price.isNotEmpty() ){
                hashmap1["description"] = description
                hashmap1["price"] = price
                hashmap1["imageUri"] = imageUri1Last
            }

            else {
                hashmap1["description"] = description
                hashmap1["price"] = price
                hashmap1["imageUri"] = imageUri1
            }




            val firestore = FirebaseFirestore.getInstance()


            val nameToCollection = "Services"+firebaseAuth.uid.toString()
            firestore.collection(nameToCollection).document(serviceId).set(hashmap1)
            runEditServiceActivity()
        }

        //handle click on cancelButton
        binding.cancelButtonChanges.setOnClickListener {
            runEditServiceActivity()
        }

    }

    private fun runEditServiceActivity() {
        startActivity(Intent(this, EditService::class.java))
    }

    /* start a new Intent to open the Gallery to choose a Picture*/
    private fun pickImages(){
        binding.updateBtnChanges.visibility = View.GONE
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select images to upload"),PICK_IMAGES_CODE)
    }

    /* After selecting an Image Save the Image to FirebaseStorage and download the new uri to save in FireStore*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_CODE){

            if (resultCode == Activity.RESULT_OK){


                binding.waitingstatusChanges.visibility = View.VISIBLE
                binding.waitingstatusChanges.setText("Please wait, saving images")
                //only one picture
                var imageUri = data!!.data



                val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
                if (imageUri != null) {
                    photoReference.putFile(imageUri)
                        .continueWithTask { photoUploadTask ->
                            photoReference.downloadUrl
                        }.continueWithTask { downloadUrlTask ->
                            val newUri = downloadUrlTask.result
                            imageUri1 = newUri.toString()
                            val hashmap1 = hashMapOf<String,Any>()
                            hashmap1["ServiceEdited"] = System.currentTimeMillis()
                            firestore.collection("EditedService").add(hashmap1)
                        }.addOnCompleteListener {
                            Log.i(ContentValues.TAG, "Upload Success")
                            timerr()

                            binding.waitingstatusChanges.setTextColor(Color.GREEN)
                            binding.waitingstatusChanges.setText("Ready to upload")

                            Picasso.get().load(imageUri).centerCrop().resize(binding.postImageViewChanges.width,
                                binding.postImageViewChanges.height).into(binding.postImageViewChanges)
                            showUploadButton()

                        }   }
            }
        }
    }

    /*Starts a Timer*/
    private fun timerr(){
        var count:Int = 0
        val timer = Timer()
        timer.schedule(object: TimerTask(){
            override fun run() {
                count+=10
                binding.progressBarTil.progress = count

                if (count > 100){
                    timer.cancel()
                }
            }

        }, 0,100)


    }

    /*make UploadButton Visible*/
    private fun showUploadButton(){
        binding.updateBtnChanges.visibility = View.VISIBLE

    }
}