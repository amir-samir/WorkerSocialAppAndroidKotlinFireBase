package com.example.menshanalla.Posts

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menshanalla.ImageSwitcherPicasso
import com.example.menshanalla.CustomerWorker.MainActivity
import com.example.menshanalla.Model.Posts
import com.example.menshanalla.databinding.ActivityPostsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*


/**
 * Here you can add a posts
 *
 * @author Amir Azim, Mohammed Abou
 *
 *
 */
class PostsActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var binding: ActivityPostsBinding
    private var uriList: ArrayList<Uri?>? = null
    private var uriListOriginal: ArrayList<Uri?>? = null
    private var uriLoadTest: ArrayList<List<String>>? = null
    private var posts: MutableList<Posts>? = null
    private var position = 0
    private val PICK_IMAGES_CODE = 0
    private lateinit var firestore: FirebaseFirestore
    //progress dialog
    private lateinit var progressDialog: ProgressDialog
    //alert Dialog
    private lateinit var dialog: AlertDialog.Builder
    private var ready: Boolean = false
    private var imagesCount = 0
    private var imagesCountForTimer = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imagesCount = 0
        imagesCountForTimer = 0

        binding.uploadBtn.visibility = View.GONE
        binding.waitingstatus.visibility = View.GONE

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //setup alert Dialog
        dialog = AlertDialog.Builder(this)
            .setMessage("Updating Profile Image")
            .setCancelable(false)

        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firestore = FirebaseFirestore.getInstance()

        uriList = ArrayList()
        uriLoadTest = ArrayList()
        uriListOriginal = ArrayList()
        posts = mutableListOf()
        binding.postImageView.setFactory { ImageView(applicationContext) }

        binding.postImageView.setOnClickListener {
             pickImages()
        }

        binding.nextBtn.setOnClickListener {
            if (position < uriListOriginal!!.size-1){
                position++
                val mImageSwitcherPicasso =
                    ImageSwitcherPicasso(this, binding.postImageView)
                Picasso.get().load(uriListOriginal!![position]).centerCrop().resize(binding.postImageView.width,binding.postImageView.height).into(mImageSwitcherPicasso)
            }
            else{
                Toast.makeText(this, "This is the last image", Toast.LENGTH_SHORT).show()
            }
        }

        binding.previousBtn.setOnClickListener {
             if (position > 0){
                 position--
                 val mImageSwitcherPicasso =
                     ImageSwitcherPicasso(this, binding.postImageView)
                 Picasso.get().load(uriListOriginal!![position]).centerCrop().resize(binding.postImageView.width,binding.postImageView.height).into(mImageSwitcherPicasso)
             }
            else{
                 Toast.makeText(this, "This is the first image", Toast.LENGTH_SHORT).show()
             }
        }

        binding.cancelButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.uploadBtn.setOnClickListener {

            val hashmap1 = hashMapOf<String,Any>()
            val description = binding.imageDescriptionTE.text.toString()
            if (description.isEmpty()){
                Toast.makeText(this@PostsActivity, "Please write the description of this post", Toast.LENGTH_SHORT).show()
            }
            if (description.isNotEmpty()){
                hashmap1["description"] = description
            }
            if (uriList!!.isEmpty()){
                Toast.makeText(this@PostsActivity, "Please choose some photos to  post", Toast.LENGTH_SHORT).show()
            }
            else{
                hashmap1["posting"] = uriList!!
                val hashmap = hashMapOf(
                    "posts" to hashmap1
                )


                val firestore = FirebaseFirestore.getInstance()


                firestore.collection(firebaseAuth.uid.toString()).add(hashmap)
                runMain()
            }










        }



    }

    private fun runMain(){
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_CODE){

            if (resultCode == Activity.RESULT_OK){

                if (data!!.clipData != null){
                    //multiple picture
                    //check number of pictures
                    binding.waitingstatus.visibility = View.VISIBLE
                    binding.waitingstatus.setText("Please wait, saving images")
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count){
                        ready = false
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        uriListOriginal!!.add(imageUri)
                       val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
                        photoReference.putFile(imageUri)
                            .continueWithTask { photoUploadTask ->
                               photoReference.downloadUrl
                            }.continueWithTask { downloadUrlTask ->
                                val newUri = downloadUrlTask.result
                                uriList!!.add(newUri)
                                val hashmap1 = hashMapOf<String,Any>()
                                hashmap1["PostAdded"] = System.currentTimeMillis()
                                firestore.collection("AddedPost").add(hashmap1)
                            }.addOnCompleteListener {
                                Log.i(TAG, "Upload Succes")
                                imagesCount++
                                Log.i(TAG, imagesCount.toString())
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
                                val hashmap1 = hashMapOf<String,Any>()
                                hashmap1["PostAdded"] = System.currentTimeMillis()
                                firestore.collection("AddedPost").add(hashmap1)
                            }.addOnCompleteListener {
                                Log.i(TAG, "Upload Succes")
                                imagesCount++
                                Log.i(TAG, imagesCount.toString())
                                timerr()
                                if (imagesCount == 1) {
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

    private fun pickImages(){
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select images to upload"),PICK_IMAGES_CODE)
    }

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

    private fun showUploadButton(){
        binding.uploadBtn.visibility = View.VISIBLE

    }




}