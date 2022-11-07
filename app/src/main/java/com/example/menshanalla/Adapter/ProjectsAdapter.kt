package com.example.menshanalla.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.menshanalla.Projects.EditProjectActivity
import com.example.menshanalla.Model.ProjectAndId
import com.example.menshanalla.Projects.ProjectsActivity
import com.example.menshanalla.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * this is the  adapter of the projects recyclerView in the ProjectsActivity.
 *
 * @author Amir Azim
 *
 *
 */

class ProjectsAdapter (
    private var mContext: Context,
    private var projects: MutableList<ProjectAndId>,
    private var isFragment: Boolean = false

): RecyclerView.Adapter<ProjectsAdapter.ViewHolder>(){

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val projectImage: ImageView = itemView.findViewById(R.id.project_image_recycler)
        val projectName: TextView = itemView.findViewById(R.id.project_Name_recycler)
        val projectDesc: TextView = itemView.findViewById(R.id.project_description_recycler)
        val projectDuration: TextView = itemView.findViewById(R.id.project_duration_recycler)
        val projectCosts: TextView = itemView.findViewById(R.id.project_costs_recycler)
        val projectPaid: TextView = itemView.findViewById(R.id.project_paid_recycler)
        val projectRest: TextView = itemView.findViewById(R.id.project_rest_recycler)
        val deleteProjectButton: ImageButton = itemView.findViewById(R.id.delete_button_project_recycler)
        val editProjectButton: ImageButton = itemView.findViewById(R.id.edit_button_project_recycler)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.project_layout,parent,false)
        return ProjectsAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
        pref.putString("ProjectId", projects[position].uid)
        pref.apply()

        //set the project image
        Glide.with(mContext).load(projects[position].theProject!!.imageUri).centerCrop()
            .into(holder.projectImage)
        //set the project name
        holder.projectName.text = projects[position]!!.theProject!!.projectName
        //set the project description
        holder.projectDesc.text = projects[position]!!.theProject!!.description
        // set the project duration
        holder.projectDuration.text = "Duration:" + projects[position]!!.theProject!!.duration
        // set the project costs
        holder.projectCosts.text = "Total costs:" + projects[position]!!.theProject!!.costs.toString()
        // set the project already paid amount
        holder.projectPaid.text = "Already paid:" + projects[position]!!.theProject!!.paid.toString()
        // set the rest Amount to pay of the project
        holder.projectRest.text = "Rest:" + projects[position]!!.theProject!!.restPayment.toString()

        //handle click on delete project button
        holder.deleteProjectButton.setOnClickListener {
            val userIdServices = "Projects"+firebaseUser!!.uid
            val firestore = FirebaseFirestore.getInstance()
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
            firestore.collection(userIdServices).document(projects[position].uid.toString()).delete()
            (mContext as Activity).startActivity(Intent(mContext, ProjectsActivity::class.java))

        }
        holder.editProjectButton.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("ProjectId", projects[position].uid)
            pref.apply()
            (mContext as Activity).startActivity(Intent(mContext, EditProjectActivity::class.java))
        }


    }

    override fun getItemCount(): Int {
        return projects.size
    }



}