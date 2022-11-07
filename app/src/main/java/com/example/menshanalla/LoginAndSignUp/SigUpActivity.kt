package com.example.handwerkeryarab

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.menshanalla.CustomerWorker.CustomerActivity
import com.example.menshanalla.CustomerWorker.MainActivity
import com.example.menshanalla.LoginAndSignUp.LoginActivity
import com.example.menshanalla.R
import com.example.menshanalla.Model.WorkerModel
import com.example.menshanalla.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * this is the signUp
 *
 * @author  Adnan Alqalaq
 *
 *
 */

class SignUpActivity : AppCompatActivity() {

    //ViewBinding
    private lateinit var binding: ActivitySignUpBinding
    //ProgressDiaog
    private lateinit var progressDialog: ProgressDialog

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    //database reference
    private lateinit var databaseReference: DatabaseReference
    private var email = ""
    private var password = ""

    //define a text to check if user is a customer or a worker

    private var workerOrCustomer = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //set Background with gradient(4 colors)
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TR_BL,  intArrayOf(
                ContextCompat.getColor(this, R.color.darkBlue),
                ContextCompat.getColor(this, R.color.lila),
                ContextCompat.getColor(this, R.color.hellBlue),
                ContextCompat.getColor(this, R.color.hellGreen),
                )
        )


        binding.mainRelativeLayout.background = gradientDrawable
        //Configure Progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("please wait")
        progressDialog.setMessage("Creating account...")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //handle already have an account button
        binding.alreadyRegistered.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // handle click im a customer
        binding.costumerButton.setOnClickListener {
            workerOrCustomer = "customer"
            binding.costumerButton.setBackgroundDrawable(resources.getDrawable(R.drawable.button_design_worker_or_customer))
            binding.workerButton.setBackgroundDrawable(resources.getDrawable(R.drawable.design_button_login))
        }

        // handle click im a worker
        binding.workerButton.setOnClickListener {
            workerOrCustomer = "worker"
            binding.costumerButton.setBackgroundDrawable(resources.getDrawable(R.drawable.design_button_login))
            binding.workerButton.setBackgroundDrawable(resources.getDrawable(R.drawable.button_design_worker_or_customer))
        }
        //handle click, begin signup
        binding.signUpBtn.setOnClickListener {
            //validate data
            validateData()
        }


    }

    private fun validateData() {
        //get data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //invalid email format
            binding.emailEt.error = "Invalid email format"
        }
        else if (TextUtils.isEmpty(password)){
            //password empty
            binding.passwordEt.error = "Please enter password"
        }
        else if (password.length < 6){
            //password length is less than 6
            binding.passwordEt.error = "Password too short should be at least 6 charachters"
        }
        else if (workerOrCustomer == ""){
            binding.signUpBtn.error = "Please select your role"
        }
        else{
            //data is valid, continue signup
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {
        //show progress
        progressDialog.show()

        // create account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //signup success
                progressDialog.dismiss()
                //get currrent user
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(
                    this,
                    "Account created successfully with email $email",
                    Toast.LENGTH_SHORT
                ).show()

                //open profile && create data
                if (workerOrCustomer.equals("worker")) {
                    val uid = firebaseAuth.currentUser?.uid
                    // val uid1 = databaseReference.push().key!!
                    databaseReference = FirebaseDatabase.getInstance().getReference("Workers")
                    val worker = WorkerModel(
                        uid,
                        "Full Name",
                        "your job",
                        "Place",
                        "worker",
                        "https://firebasestorage.googleapis.com/v0/b/menshanalla-54d8e.appspot.com/o/ProfileImages%2FIHAmqRAcc5dIJ7q6YKiHG6XvMVO2?alt=media&token=fb428ef8-0049-47e5-8a9f-83d854c388c1",
                        0.00,
                        0.00,
                        "false"
                    )
                    if (uid != null) {
                        databaseReference.child(uid).setValue(worker).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Inshallah yesht8el",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(this@SignUpActivity, "msht8lesh", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

                if (workerOrCustomer.equals("customer")) {
                    val uid = firebaseAuth.currentUser?.uid
                    // val uid1 = databaseReference.push().key!!
                    databaseReference = FirebaseDatabase.getInstance().getReference("Workers")
                    val worker = WorkerModel(
                        uid,
                        "Full Name",
                        "your job",
                        "Place",
                        "customer",
                        "https://firebasestorage.googleapis.com/v0/b/menshanalla-54d8e.appspot.com/o/ProfileImages%2FIHAmqRAcc5dIJ7q6YKiHG6XvMVO2?alt=media&token=fb428ef8-0049-47e5-8a9f-83d854c388c1",
                        0.00,
                        0.00,
                        "false"
                    )
                    if (uid != null) {
                        databaseReference.child(uid).setValue(worker).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Inshallah yesht8el",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(this@SignUpActivity, "msht8lesh", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    startActivity(Intent(this, CustomerActivity::class.java))
                    finish()
                }
            }
                    .addOnFailureListener { e ->
                        //signup failed
                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "SignUp failed due to ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // go back to previous activity, if backButton in actionbar clicked
        return super.onSupportNavigateUp()
    }
}