package com.example.menshanalla.LoginAndSignUp

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.handwerkeryarab.SignUpActivity
import com.example.menshanalla.CustomerWorker.CustomerActivity
import com.example.menshanalla.CustomerWorker.MainActivity
import com.example.menshanalla.R
import com.example.menshanalla.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * this is the login Activity
 *
 * @author Amir Azim, Mohammed Abou
 *
 *
 */

class LoginActivity : AppCompatActivity() {

    //ViewBinding
    private lateinit var binding: ActivityLoginBinding

    //ActionBar
   // private lateinit var actionBar: ActionBar

    //ProgressDialog
    private lateinit var progressDialog: ProgressDialog

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""
    private var password = ""
    private var workerOrCustomer = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //configure actionbar
//        actionBar = supportActionBar!!
//        actionBar.title = "Login"

        binding.waitTV.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        //set Background with gradient(4 colors)
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TR_BL,  intArrayOf(
                ContextCompat.getColor(this, R.color.darkBlue),
                ContextCompat.getColor(this, R.color.lila),
                ContextCompat.getColor(this, R.color.hellBlue),
                ContextCompat.getColor(this, R.color.hellGreen),
            )
        )


        binding.mainRelativeLayout1.background = gradientDrawable

        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("please wait")
        progressDialog.setMessage("Logging In...")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog.show()
        checkUser()

        //handle click, open register activity
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        //handle click, begin login
        binding.loginBtn.setOnClickListener{
            //before logging in, validate data
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
            //password field empty
            binding.passwordEt.error = "Please enter password"
        }
        else{
            //data is validated, begin login
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        //show progress
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                //login success

                //get user info
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email

                binding.progressBar.visibility = View.VISIBLE
                binding.waitTV.visibility = View.VISIBLE
                val ref = FirebaseDatabase.getInstance().getReference("Workers")
                ref.child(firebaseAuth.uid!!)
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
                Toast.makeText(this, "Logged In as $email", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()

            }
            .addOnFailureListener { e->
                //login failed
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun checkUser() {
        //if user is already logged in go to profile activity
        //get current user

        progressDialog.show()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            binding.progressBar.visibility = View.VISIBLE
            binding.waitTV.visibility = View.VISIBLE
            val ref = FirebaseDatabase.getInstance().getReference("Workers")
            ref.child(firebaseAuth.uid!!)
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

                    }

                } )

        }


        progressDialog.dismiss()

    }

    private fun startActivityChooser(int: Int){
        if (int == 1){
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            startActivity(Intent(this, CustomerActivity::class.java))
        }
    }
}