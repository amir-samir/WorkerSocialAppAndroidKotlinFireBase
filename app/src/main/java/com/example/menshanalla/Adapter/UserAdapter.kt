package com.example.menshanalla.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.Model.Worker
import com.example.menshanalla.ProfileFragment
import com.example.menshanalla.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * this is the  adapter of the user to show in the search fragment.
 *
 * @author Amir Azim
 *
 *
 */
class UserAdapter (private var mContext: Context,
                   private var mUser: List<Worker>,
                   private var isFragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>()

{

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    lateinit private var followingButton: Button


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false )
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = mUser[position]
        holder.workerJobText.text = user.getWorkerJob()
        holder.workerNameText.text = user.getWorkerName()
        Picasso.get().load(user.getProfileImage()).placeholder(R.drawable.profile).into(holder.workerProfileImage)

        checkFollowingStatus(user.getWorkerId(), holder.followButton)

        holder.itemView.setOnClickListener(View.OnClickListener {
            val pref = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            pref.putString("profileId", user.getWorkerId())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container,ProfileFragment()).commit()


        })

        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString() == "Follow") {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following")
                        .child(user.getWorkerId())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getWorkerId())
                                        .child("Followers")
                                        .child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful){

                                            }

                                        }
                                }
                            }

                        }
                }
            }
            else{
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following")
                        .child(user.getWorkerId())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getWorkerId())
                                        .child("Followers")
                                        .child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful){

                                            }

                                        }
                                }
                            }

                        }
                }
            }
        }
    }

    private fun checkFollowingStatus(workerId: String, followButton: Button) {
      firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow")
                .child(it1.toString())
                .child("Following").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.child(workerId).exists()){
                            followButton.text = "Following"
                        }
                        else{
                            followButton.text = "Follow"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

    override fun getItemCount(): Int {
       return mUser.size
    }

    class ViewHolder (@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){
        var workerJobText: TextView = itemView.findViewById(R.id.worker_job_search)
        var workerNameText: TextView = itemView.findViewById(R.id.worker_full_name_search)
        var workerProfileImage: CircleImageView = itemView.findViewById(R.id.worker_profile_image_search)
        var followButton: Button = itemView.findViewById(R.id.follow_btn_search)
    }
}