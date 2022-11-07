package com.example.menshanalla.Services

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.menshanalla.CustomerWorker.MainActivity
import com.example.menshanalla.databinding.ActivityServiceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*

class ServiceActivity : AppCompatActivity() {
    //define fireBaseAut
    private lateinit var firebaseAuth: FirebaseAuth
    //define firebaseStorage
    private lateinit var storageReference: StorageReference
    //define fireStore
    private lateinit var firestore: FirebaseFirestore
    //binding
    private lateinit var binding: ActivityServiceBinding
    //Code For PickImages
    private val PICK_IMAGES_CODE = 0
    //Save the ImageUri
    private var imageUri1 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set Upload Button And text for waiting Status Invisible
        binding.uploadBtn.visibility = View.GONE
        binding.waitingstatus.visibility = View.GONE

        //initialise firebase Services
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        //handle click on ImageView to open Gallery
        binding.postImageView.setOnClickListener {
            pickImages()
        }

        //handle click on uploadButton to save image, description and price
        binding.uploadBtn.setOnClickListener {

            val hashmap1 = hashMapOf<String,Any>()
            val description = binding.imageDescriptionTE.text.toString()
            val price = binding.servicePrice.text.toString()
            if (description.isEmpty()){
                Toast.makeText(this@ServiceActivity, "Please write the description of service", Toast.LENGTH_SHORT).show()
            }
            if (price.isEmpty()){
                Toast.makeText(this@ServiceActivity, "Please write the price of service", Toast.LENGTH_SHORT).show()
            }
            if (imageUri1 == ""){
                Toast.makeText(this@ServiceActivity, "Please pick an image of service", Toast.LENGTH_SHORT).show()
            }

            else {
                hashmap1["description"] = description
                hashmap1["price"] = price
                hashmap1["imageUri"] = imageUri1
            }




            val firestore = FirebaseFirestore.getInstance()


            val nameToCollection = "Services"+firebaseAuth.uid.toString()
            firestore.collection(nameToCollection).add(hashmap1)
            runMain()

           //handle click on CancelButton
            binding.cancelButton.setOnClickListener {
                runMain()
            }







        }

    }


    /* Start Main Activity*/
    private fun runMain(){
        startActivity(Intent(this, MainActivity::class.java))
    }


    /* start a new Intent to open the Gallery to choose a Picture*/
    private fun pickImages(){
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


                binding.waitingstatus.visibility = View.VISIBLE
                binding.waitingstatus.setText("Please wait, saving images")
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
                                hashmap1["ServiceAdded"] = System.currentTimeMillis()
                                firestore.collection("AddedService").add(hashmap1)
                            }.addOnCompleteListener {
                                Log.i(ContentValues.TAG, "Upload Success")
                                timerr()

                                    binding.waitingstatus.setTextColor(Color.GREEN)
                                    binding.waitingstatus.setText("Ready to upload")

                                    Picasso.get().load(imageUri).centerCrop().resize(binding.postImageView.width,
                                        binding.postImageView.height).into(binding.postImageView)
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
        binding.uploadBtn.visibility = View.VISIBLE

    }


}