package com.example.handwerkeryarab

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.menshanalla.CustomerWorker.CustomerActivity
import com.example.menshanalla.CustomerWorker.MainActivity
import com.example.menshanalla.databinding.ActivityProfileEditBinding
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.HashMap

/**
 * in this fragment the user can edit his profile and its used for other profiles
 *
 * @author Amir Azim, Adnan Alqalaq
 *
 *
 */


//viewbinding
private lateinit var binding: ActivityProfileEditBinding

//firebase auth
private lateinit var firebaseAuth: FirebaseAuth

//image uri
private var imageUri: Uri?=null

//progress dialog
private lateinit var progressDialog: ProgressDialog

//database reference
private lateinit var databaseReference: DatabaseReference

//datastorage reference
private lateinit var storageReference: StorageReference

//ImageUri
private lateinit var img: Uri
//Firebase database
private lateinit var database: FirebaseDatabase
//firebase storage
private lateinit var storage : FirebaseStorage
//alert Dialog
private lateinit var dialog: AlertDialog.Builder

private lateinit var googleMap: GoogleMap
private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

private lateinit var currentLatLong: LatLng


private lateinit var geocoder : Geocoder
private lateinit var myadresses: List<Address>

private lateinit var searchView: SearchView
private lateinit var markerOptions: MarkerOptions



class ProfileEditActivity : AppCompatActivity(), OnMapReadyCallback {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)




        //setup alert Dialog
        dialog = AlertDialog.Builder(this)
            .setMessage("Updating Profile Image")
            .setCancelable(false)

        //setup database
        database = FirebaseDatabase.getInstance()
        //setup firebase storage
        storage = FirebaseStorage.getInstance()


        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //initial firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        //setup full name and try to save Data
        val uid = firebaseAuth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Workers")
        loadUserInfo()

        binding.map.onCreate(savedInstanceState)
        binding.map.onResume()

        binding.map.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        geocoder = Geocoder(this)
        searchView = binding.searchBar
        binding.myCurrentLocation.setOnClickListener{
            getcurrentLocation()
        }
        //handle back Button
        binding.backBtn.setOnClickListener {

            onBackPressed()
        }

        //handle take a Photo from Gallery/Kamera
        binding.profileIv.setOnClickListener {
            showImageAttachMenu()
        }

        //handle update Button
        binding.updateBtn.setOnClickListener {
            validateData()
        }

        val ref = FirebaseDatabase.getInstance().getReference("Workers")
        ref.child(firebaseAuth.currentUser!!.uid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info if they are not equal to null
                    val userType = "${snapshot.child("userType").value}"
                    if (userType.equals("worker")){
                        binding.jobTil.visibility = View.VISIBLE
                        binding.jobEt.visibility = View.VISIBLE
                    }
                    if (userType.equals("customer")){
                       binding.jobTil.visibility = View.GONE
                       binding.jobEt.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            } )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null){
            if (data.data != null){
                img = data.data!!

                binding.profileIv.setImageURI(img)
            }
        }
    }

    private var name = ""
    private var job = ""
    private var userType = ""
    private var workPlaces = ""
    private fun validateData() {
        //get data
        name = binding.nameEt.text.toString().trim()


        //validate data
        if (name.isEmpty()){
            //name not entered
            Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show()
        }
        else {
            //name is entered

            if (imageUri == null){
                //update without image
                updateProfile("")
            }
            else{
                //update with image
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        //image path and nam, use uid to replace previous
        val filePathAndName = "ProfileImages/"+ firebaseAuth.uid

        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot->
                //image uploaded, get url of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener{e->
                //failed to upload image
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload image due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating profile")
        job = binding.jobEt.text.toString().trim()
       // userType = binding.userTypeEt.text.toString().trim()
       // workPlaces = binding.workPlacesEt.text.toString().trim()
        //setup info to update to db
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["workerName"] = "${name}"
        if (job != null){
            hashmap["workerJob"] = "${job.lowercase()}"
        }
        if (imageUri != null){
            hashmap["profileImage"] = uploadedImageUrl
        }
        if (myadresses.isNotEmpty()){
            val address: Address = myadresses[0]
            hashmap["workerPlaces"] = address.getAddressLine(0).toString()
        }


        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Workers")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                //profile updated
                progressDialog.dismiss()
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                //failed to update profile
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update Profile due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

        val ref = FirebaseDatabase.getInstance().getReference("Workers")
        ref.child(firebaseAuth.currentUser!!.uid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info if they are not equal to null
                    val userType = "${snapshot.child("userType").value}"
                    if (userType.equals("worker")){
                        startActivityChooser(1)
                    }
                    if (userType.equals("customer")){
                        startActivityChooser(2)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            } )

    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Workers")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info if they are not equal to null
                    val job = "${snapshot.child("workerJob").value}"
                    val name = "${snapshot.child("workerName").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val uid = "${snapshot.child("workerId").value}"
                    val userType = "${snapshot.child("userType").value}"
                    val workPlaces = "${snapshot.child("workerPlaces").value}"

                    //set data
                    binding.nameEt.setText(name)
                    binding.jobEt.setText(job)
                   // binding.userTypeEt.setText(userType)
                    binding.wordPlaceEt.setText(workPlaces)


                    //set image
                    try{
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(com.example.menshanalla.R.drawable.ic_person_gray)
                            .into(binding.profileIv)

                    }
                    catch (e: Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            } )
    }

    private fun showImageAttachMenu(){
        /*Show popup menu with options Camera, Gallery to upload an image*/

        //setup popup menu
        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener { item->
            //get id of clicked item
            val id = item.itemId
            if (id == 0){
                //Camera clicked
                pickImageCamera()
            }
            else if (id == 1){
                //Gallery clicked
                pickImageGallery()

            }

            true

        }

    }

    private fun openCamera(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->

        }
    }

    private fun pickImageCamera() {
        //intent to pick image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        //intent to pick image from gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            //get uri of image
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //set to imageView
                binding.profileIv.setImageURI(imageUri)
            }
            else{
                //canceled
                Toast.makeText(this,"Canceled",Toast.LENGTH_SHORT).show()
            }
        }
    )

    //used to handle result of gallery intent(new way in replacement of startactivityforresults)
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //set to imageView
                binding.profileIv.setImageURI(imageUri)
            }
            else{
                //canceled
                Toast.makeText(this,"Canceled",Toast.LENGTH_SHORT).show()
            }
        }
    )

    //Go to Other Fragments from Activity
    private fun changeFragment(fragment: Fragment){
        val fragmentChange = supportFragmentManager.beginTransaction()
        fragmentChange.replace(com.example.menshanalla.R.id.fragment_container, fragment)
        fragmentChange.commit()
    }

    private fun startActivityChooser(int: Int){
        if (int == 1){
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            startActivity(Intent(this, CustomerActivity::class.java))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        checkPermission()
        googleMap = map
        googleMap.isMyLocationEnabled = true
        //googleMap.setOnMarkerClickListener(this)
        map.setOnMapClickListener(object : GoogleMap.OnMapClickListener{
            override fun onMapClick(latLng: LatLng) {
                markerOptions = MarkerOptions().position(latLng)
                geocoder = Geocoder(applicationContext, Locale.getDefault())

                try {
                    myadresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1)
                    val myadressess = myadresses.get(0).getAddressLine(0).toString()
                    searchView.setQuery("$myadressess", false)
                } catch (e: Exception) {
                }
                map.clear()
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                map.addMarker(markerOptions)

            }
        })






                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        val locationName : String = searchView.query.toString()
                        try {

                            myadresses = geocoder.getFromLocationName("$locationName",1)
                            if(myadresses.isNotEmpty()){
                                Places.initialize(applicationContext,"AIzaSyDd6mCoEgv5Lq-gPvB1xTjw4E6vqtKT5-o")
                                val address1: Address = myadresses[0]
                                val hashmap: java.util.HashMap<String, Any> = java.util.HashMap()
                                //hashmap["workerPlaces"] = address1.getAddressLine(0).toString()
                                binding.wordPlaceEt.setText(address1.getAddressLine(0).toString())
                                Log.d(ContentValues.TAG, "onQueryTextSubmit: " + address1)


                                //update to db
                                val reference = FirebaseDatabase.getInstance().getReference("Workers")
                                reference.child(firebaseAuth.uid!!)
                                    .updateChildren(hashmap)
                                    .addOnSuccessListener {
                                        showMessage("saved location")
                                        Log.d(ContentValues.TAG, "savedLocation11111111111111111")

                                    }
                                    .addOnFailureListener {e->
                                        showMessage("error while saving location")
                                    }
                            }
                        } catch (e: Exception) {
                        }


                        return false
                    }

                    override fun onQueryTextChange(p0: String?): Boolean {
                        return false
                    }

                })












        map.uiSettings.isZoomControlsEnabled = true
    }

    private fun checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

            return
        }
    }



    private fun showMessage(string: String){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    private fun getcurrentLocation(){
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                currentLatLong = LatLng(it.latitude, it.longitude)
                markerOptions = MarkerOptions().position(currentLatLong)
                geocoder = Geocoder(this, Locale.getDefault())
                try {
                    myadresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    val address2: Address = myadresses[0]
                    binding.wordPlaceEt.setText(address2.getAddressLine(0).toString())
                } catch (e: Exception) {

                }
                googleMap.clear()
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLatLong, 12f
                    )
                )
                googleMap.addMarker(markerOptions)
            }
        }.addOnFailureListener {

        }
    }
}