package com.example.menshanalla.Posts

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.menshanalla.ImageSwitcherPicasso
import com.example.menshanalla.Model.Posts
import com.example.menshanalla.databinding.ActivityEditPostsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

/**
 * This class is for editing posts (delete or change information)
 *
 * @author Amir Azim
 *
 *
 */

class EditPostsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditPostsBinding
    //define fireBaseAut
    private lateinit var firebaseAuth: FirebaseAuth
    //define firebaseStorage
    private lateinit var storageReference: StorageReference
    //define fireStore
    private lateinit var firestore: FirebaseFirestore
    //Code For PickImages
    private val PICK_IMAGES_CODE = 0
    //Define an  Arraylist of Uri
    private var uriList: ArrayList<Uri?>? = null
    //Define an Arraylist of Uri
    private var uriListOriginal: ArrayList<Uri?>? = null
    //Define ArrayList to save the current pictures of post
    private var lastUriList: ArrayList<Any>? = null
    //Define a variable to save position of imageswitcher and Arrays
    private var position = 0
    //Define variable to save the count of selected images
    private var imagesCount = 0
    //Define images count or timer
    private var imagesCountForTimer = 0

    //define a variable to save the document id
    private var postId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set waiting status to invisible
        binding.waitingstatus.visibility = View.GONE

        //initialise firebase Services
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        //define and initialise the shared preference from the recycler view
        val pref = this?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.postId = pref.getString("PostId", "none")!!
        }


        //initialise list of Uri
        uriList = ArrayList()
        //initialise list of Uri
        uriListOriginal = ArrayList()
        //initialise count of selected images
        imagesCount = 0
        //initialise images count or timer
        imagesCountForTimer = 0
        //initialise ArrayList to save the current pictures of post
        lastUriList = ArrayList()

        binding.postImageView.setFactory { ImageView(applicationContext) }

        //handle click on imageSwitcher
        binding.postImageView.setOnClickListener {
            pickImages()
        }

        binding.cancelButton.setOnClickListener {
            startActivity(Intent(this, YourPostsActivity::class.java))
        }


        // call get current Posts method to get the posts of the current user from firestore
        getCurrentPosts()

        // handle click on next photo button
        binding.nextBtn.setOnClickListener {
            if (position < lastUriList!!.size-1){
                position++
                val mImageSwitcherPicasso =
                    ImageSwitcherPicasso(this, binding.postImageView)
                Picasso.get().load(lastUriList!![position].toString()).centerCrop().resize(binding.postImageView.width,binding.postImageView.height).into(mImageSwitcherPicasso)
            }
            else{
                Toast.makeText(this, "This is the last image", Toast.LENGTH_SHORT).show()
            }
        }

        // handle click on previous photo button
        binding.previousBtn.setOnClickListener {
            if (position > 0){
                position--
                val mImageSwitcherPicasso =
                    ImageSwitcherPicasso(this, binding.postImageView)
                Picasso.get().load(lastUriList!![position].toString()).centerCrop().resize(binding.postImageView.width,binding.postImageView.height).into(mImageSwitcherPicasso)
            }
            else{
                Toast.makeText(this, "This is the first image", Toast.LENGTH_SHORT).show()
            }
        }

        // handle click on uploadBtn
        binding.uploadBtn.setOnClickListener {

            val hashmap1 = hashMapOf<String,Any>()
            val description = binding.imageDescriptionTE.text.toString()
            if (description.isEmpty()){
                Toast.makeText(this@EditPostsActivity, "Please write the description of your post", Toast.LENGTH_SHORT).show()
            }
            if (lastUriList!!.isEmpty()){
                Toast.makeText(this@EditPostsActivity, "Please choose images to upload by clicking on the image", Toast.LENGTH_SHORT).show()
            }
            else{
                hashmap1["description"] = description
                hashmap1["posting"] = lastUriList!!
                val hashmap = hashMapOf(
                    "posts" to hashmap1
                )


                val firestore = FirebaseFirestore.getInstance()


                firestore.collection(firebaseAuth.uid.toString()).document(postId).set(hashmap)
                runYourPosts()
            }


        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_CODE){

            if (resultCode == Activity.RESULT_OK){

                if (data!!.clipData != null){
                    //multiple picture
                    //check number of pictures
                    lastUriList!!.clear()
                    binding.waitingstatus.visibility = View.VISIBLE
                    binding.waitingstatus.setText("Please wait, saving images")
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count){
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        uriListOriginal!!.add(imageUri)
                        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
                        photoReference.putFile(imageUri)
                            .continueWithTask { photoUploadTask ->
                                photoReference.downloadUrl
                            }.continueWithTask { downloadUrlTask ->
                                val newUri = downloadUrlTask.result
                                uriList!!.add(newUri)
                                lastUriList!!.add(newUri)
                                val hashmap1 = hashMapOf<String,Any>()
                                hashmap1["PostAdded"] = System.currentTimeMillis()
                                firestore.collection("AddedPost").add(hashmap1)
                            }.addOnCompleteListener {
                                Log.i(ContentValues.TAG, "Upload Succes")
                                imagesCount++
                                Log.i(ContentValues.TAG, imagesCount.toString())
                                timerr()
                                if (imagesCount == count){
                                    binding.waitingstatus.setTextColor(Color.GREEN)
                                    binding.waitingstatus.setText("Ready to upload")
                                    position = 0
                                    val mImageSwitcherPicasso =
                                        ImageSwitcherPicasso(this, binding.postImageView)
                                    Picasso.get().load(uriListOriginal!![position]).centerCrop().resize(binding.postImageView.width,binding.postImageView.height).into(mImageSwitcherPicasso)
                                    showUploadButton()
                                }
                            }
                    }

                }
                else{

                    //clear the images uri Arraylist
                    lastUriList!!.clear()
                    //only one picture
                    val imageUri = data.data

                    //set image to image switcher


                    val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
                    if (imageUri != null) {
                        photoReference.putFile(imageUri)
                            .continueWithTask { photoUploadTask ->
                                photoReference.downloadUrl
                            }.continueWithTask { downloadUrlTask ->
                                val newUri = downloadUrlTask.result
                                uriList!!.add(newUri)
                                lastUriList!!.add(newUri)
                                val hashmap1 = hashMapOf<String,Any>()
                                hashmap1["PostAdded"] = System.currentTimeMillis()
                                firestore.collection("AddedPost").add(hashmap1)
                            }.addOnCompleteListener {
                                Log.i(ContentValues.TAG, "Upload Succes")
                                imagesCount++
                                Log.i(ContentValues.TAG, imagesCount.toString())
                                timerr()
                                if (imagesCount == 1) {
                                    binding.waitingstatus.visibility = View.VISIBLE
                                    binding.waitingstatus.setTextColor(Color.GREEN)
                                    binding.waitingstatus.setText("Ready to upload")
                                    position = 0
                                    val mImageSwitcherPicasso =
                                        ImageSwitcherPicasso(this, binding.postImageView)
                                    Picasso.get().load(imageUri).centerCrop()
                                        .resize(
                                            binding.postImageView.width,
                                            binding.postImageView.height
                                        ).into(mImageSwitcherPicasso)
                                    showUploadButton()
                                }
                            }   }
                }
            }
        }


    }

    //start yourpostsActivity
    private fun runYourPosts() {
        startActivity(Intent(this, YourPostsActivity::class.java))
    }

    //open gallery to pick images
    private fun pickImages(){
        binding.uploadBtn.visibility = View.GONE
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select images to upload"),PICK_IMAGES_CODE)
    }

    //starts a timer
    private fun timerr(){
        var count:Int = 0
        val timer = Timer()
        timer.schedule(object: TimerTask(){
            override fun run() {
                count+=10
                binding.progressBarTil.progress = count

                if (count > 100){
                    timer.cancel()
                    imagesCountForTimer+= 100
                }
            }

        }, 0,100)


    }

    //make upload Button visible
    private fun showUploadButton(){
        binding.uploadBtn.visibility = View.VISIBLE

    }

    //get the posts of the current user from firestore
    private fun getCurrentPosts(){
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        firestore.collection(firebaseAuth.currentUser!!.uid)
            .document(postId).get()
            .addOnSuccessListener { result ->
                    val dataclasses = result.toObject(Posts::class.java)
                    binding.imageDescriptionTE.setText(dataclasses!!.posts!!.description)
                for (i in 0 until dataclasses.posts!!.posting!!.size){
                    lastUriList!!.add(dataclasses.posts.posting!![i])
                }
                if (lastUriList!!.isNotEmpty()) {
                    val mImageSwitcherPicasso =
                        ImageSwitcherPicasso(this, binding.postImageView)
                    Picasso.get().load(lastUriList!![position].toString())
                        .centerCrop()
                        .resize(binding.postImageView.width, binding.postImageView.height)
                        .into(mImageSwitcherPicasso)
                }


            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
}