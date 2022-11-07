package com.example.menshanalla.Projects

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
import com.example.menshanalla.Model.PrjectData
import com.example.menshanalla.databinding.ActivityEditProjectBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*

/**
 * in this Activity the users can edit their projects and change information
 *
 * @author Amir Azim
 *
 *
 */

class EditProjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProjectBinding

    //define a variable to save the document id
    private var projectId = ""
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
        binding = ActivityEditProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialise firebase Services
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        //define and initialise the shared preference from the recycler view
        val pref = this?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.projectId = pref.getString("ProjectId", "none")!!
        }

        //Set update Button And text for waiting Status Invisible
        binding.updateBtn.visibility = View.VISIBLE
        binding.waitingstatusEdit.visibility = View.GONE

        //get from firestore the Picture,description and price of current Service
        val userIdServices = "Projects"+firebaseAuth!!.uid
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        firestore.collection(userIdServices).document(projectId).get()
            .addOnSuccessListener { result ->
                val dataclasses = result.toObject(PrjectData::class.java)
                if (dataclasses != null) {
                    // set current image
                    Picasso.get().load(dataclasses.imageUri).centerCrop().resize(
                        binding.projectImageViewEdit.width,
                        binding.projectImageViewEdit.height
                    ).into(binding.projectImageViewEdit)
                    //set current project name
                    binding.projectNameEdit.setText(dataclasses!!.projectName)
                    //set current project description
                    binding.imageDescriptionTEEdit.setText(dataclasses!!.description)
                    //set current project duration
                    binding.projectDurationEdit.setText(dataclasses!!.duration)
                    // set current Project costs
                    binding.projectPriceEdit.setText(dataclasses!!.costs)
                    // set current amount already paid of current project
                    binding.paidProjectEdit.setText(dataclasses!!.paid)
                    //save the current image uri to the var imageUri1Last
                    imageUri1Last = dataclasses.imageUri.toString()
                }

            }

        //handle click on ImageView to open Gallery
        binding.projectImageViewEdit.setOnClickListener {
            pickImages()
        }

        //handle click on update button
        binding.updateBtn.setOnClickListener {
            val hashmap1 = hashMapOf<String,Any>()
            val description = binding.imageDescriptionTEEdit.text.toString()
            val costs = binding.projectPriceEdit.text.toString()
            val paid = binding.paidProjectEdit.text.toString()
            val duration = binding.projectDurationEdit.text.toString()
            val projectName = binding.projectNameEdit.text.toString()
            if (description.isEmpty()){
                Toast.makeText(this@EditProjectActivity, "Please write the description of service", Toast.LENGTH_SHORT).show()
            }
            if (description.isEmpty()){
                Toast.makeText(this@EditProjectActivity, "Please write the description of this project", Toast.LENGTH_SHORT).show()
            }
            if (costs.isEmpty()){
                Toast.makeText(this@EditProjectActivity, "Please write the costs of this project", Toast.LENGTH_SHORT).show()
            }
            if (imageUri1Last == ""){
                Toast.makeText(this@EditProjectActivity, "Please pick an image of this project", Toast.LENGTH_SHORT).show()
            }
            if (duration.isEmpty()){
                Toast.makeText(this@EditProjectActivity, "Please write the duration of this project", Toast.LENGTH_SHORT).show()
            }
            if (paid.isEmpty()){
                Toast.makeText(this@EditProjectActivity, "Please write the paid amount of this project", Toast.LENGTH_SHORT).show()
            }
            if (projectName.isEmpty()){
                Toast.makeText(this@EditProjectActivity, "Please write the name of this project", Toast.LENGTH_SHORT).show()
            }

            else {
                hashmap1["projectName"] = projectName
                hashmap1["description"] = description
                hashmap1["costs"] = costs
                hashmap1["imageUri"] = imageUri1Last
                hashmap1["duration"] = duration
                hashmap1["paid"] = paid
                hashmap1["restPayment"] = costs.toFloat().minus(paid.toFloat()).toString()
            }




            val firestore = FirebaseFirestore.getInstance()


            val nameToCollection = "Projects"+firebaseAuth.uid.toString()
            firestore.collection(nameToCollection).document(projectId).set(hashmap1)
            runProjectsActivity()
        }

        //handle click on cancelButton
        binding.cancelButton.setOnClickListener {
            runProjectsActivity()
        }


    }

    // starts the Activity (ProjectsActivity)
    private fun runProjectsActivity() {
        startActivity(Intent(this, ProjectsActivity::class.java))
    }


    /* start a new Intent to open the Gallery to choose a Picture*/
    private fun pickImages(){
        binding.updateBtn.visibility = View.GONE
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


                binding.waitingstatusEdit.visibility = View.VISIBLE
                binding.waitingstatusEdit.text = "Please wait, saving images"
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

                            binding.waitingstatusEdit.setTextColor(Color.GREEN)
                            binding.waitingstatusEdit.setText("Ready to upload")

                            Picasso.get().load(imageUri).centerCrop().resize(binding.projectImageViewEdit.width,
                                binding.projectImageViewEdit.height).into(binding.projectImageViewEdit)
                            imageUri1Last = imageUri1
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
                binding.progressBarTilEdit.progress = count

                if (count > 100){
                    timer.cancel()
                }
            }

        }, 0,100)


    }

    /*make UploadButton Visible*/
    private fun showUploadButton(){
        binding.updateBtn.visibility = View.VISIBLE

    }
}