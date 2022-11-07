package com.example.menshanalla.Projects

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.menshanalla.databinding.ActivityAddProjectBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*

/**
 * this activity to add a project
 *
 * @author Amir Azim, Adnan Alqalaq, Mohammed Abou
 *
 *
 */
class AddProjectActivity : AppCompatActivity() {
    //define fireBaseAut
    private lateinit var firebaseAuth: FirebaseAuth
    //define firebaseStorage
    private lateinit var storageReference: StorageReference
    //define fireStore
    private lateinit var firestore: FirebaseFirestore
    //binding
    private lateinit var binding: ActivityAddProjectBinding
    //Code For PickImages
    private val PICK_IMAGES_CODE = 0
    //Save the ImageUri
    private var imageUri1 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set Upload Button And text for waiting Status Invisible
        binding.uploadBtn.visibility = View.GONE
        binding.waitingstatus.visibility = View.GONE

        //initialise firebase Services
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        //handle click on ImageView to open Gallery
        binding.projectImageView.setOnClickListener {
            pickImages()
        }

        //handle click on uploadButton to save image, description and price
        binding.uploadBtn.setOnClickListener {

            val hashmap1 = hashMapOf<String,Any>()
            val description = binding.imageDescriptionTE.text.toString()
            val costs = binding.projectPrice.text.toString()
            val paid = binding.paidProject.text.toString()
            val duration = binding.projectDuration.text.toString()
            val projectName = binding.projectName.text.toString()

            if (description.isEmpty()){
                Toast.makeText(this@AddProjectActivity, "Please write the description of this project", Toast.LENGTH_SHORT).show()
            }
            if (costs.isEmpty()){
                Toast.makeText(this@AddProjectActivity, "Please write the costs of this project", Toast.LENGTH_SHORT).show()
            }
            if (imageUri1 == ""){
                Toast.makeText(this@AddProjectActivity, "Please pick an image of this project", Toast.LENGTH_SHORT).show()
            }
            if (duration.isEmpty()){
                Toast.makeText(this@AddProjectActivity, "Please write the duration of this project", Toast.LENGTH_SHORT).show()
            }
            if (paid.isEmpty()){
                Toast.makeText(this@AddProjectActivity, "Please write the paid amount of this project", Toast.LENGTH_SHORT).show()
            }
            if (projectName.isEmpty()){
                Toast.makeText(this@AddProjectActivity, "Please write the name of this project", Toast.LENGTH_SHORT).show()
            }

            else {
                hashmap1["projectName"] = projectName
                hashmap1["description"] = description
                hashmap1["costs"] = costs
                hashmap1["imageUri"] = imageUri1
                hashmap1["duration"] = duration
                hashmap1["paid"] = paid
                hashmap1["restPayment"] = costs.toFloat().minus(paid.toFloat()).toString()
            }




            val firestore = FirebaseFirestore.getInstance()


            val nameToCollection = "Projects"+firebaseAuth.uid.toString()
            firestore.collection(nameToCollection).add(hashmap1)
            runProjectsActivity()

            //handle click on CancelButton
            binding.cancelButton.setOnClickListener {
                runProjectsActivity()
            }







        }

    }


    /* Start Main Activity*/
    private fun runProjectsActivity(){
        startActivity(Intent(this, ProjectsActivity::class.java))
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
                            hashmap1["ProjectAdded"] = System.currentTimeMillis()
                            firestore.collection("AddedProject").add(hashmap1)
                        }.addOnCompleteListener {
                            Log.i(ContentValues.TAG, "Upload Success")
                            timerr()

                            binding.waitingstatus.setTextColor(Color.GREEN)
                            binding.waitingstatus.setText("Ready to upload")

                            Picasso.get().load(imageUri).centerCrop().resize(binding.projectImageView.width,
                                binding.projectImageView.height).into(binding.projectImageView)
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