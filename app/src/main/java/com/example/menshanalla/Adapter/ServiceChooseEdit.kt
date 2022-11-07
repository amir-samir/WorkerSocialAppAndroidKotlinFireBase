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
import com.example.menshanalla.*
import com.example.menshanalla.Model.EditServicesData
import com.example.menshanalla.Services.ChangeService
import com.example.menshanalla.Services.EditService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
/**
 * this is the  adapter of the services list to choose to edit them or delete them.
 *
 * @author Amir Azim
 *
 *
 */
class ServiceChooseEdit (
    private var mContext: Context,
    private var services: MutableList<EditServicesData>,
    private var isFragment: Boolean = false

): RecyclerView.Adapter<ServiceChooseEdit.ViewHolder>(){

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val serviceDesc: TextView = itemView.findViewById(R.id.descriptionServiceEdit)
        val servicePrice: TextView = itemView.findViewById(R.id.priceServiceEdit)
        val serviceImage: ImageView = itemView.findViewById(R.id.serviceImageRecycEdit)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteService_button)
        val editButton: ImageButton = itemView.findViewById(R.id.editService_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.recycler_edit_services_item,parent,false)
        return ServiceChooseEdit.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val pref = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
        pref.putString("ServerId", services[position].uid)
        pref.apply()

        holder.serviceDesc.setText(services[position].theService!!.description)
        holder.servicePrice.setText(services[position].theService!!.price)
        Glide.with(mContext).load(services[position].theService!!.imageUri).centerCrop()
            .into(holder.serviceImage)
        holder.deleteButton.setOnClickListener {
            val userIdServices = "Services"+firebaseUser!!.uid
            val firestore = FirebaseFirestore.getInstance()
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
            firestore.collection(userIdServices).document(services[position].uid.toString()).delete()
            (mContext as Activity).startActivity(Intent(mContext, EditService::class.java))

        }
        holder.editButton.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            pref.putString("ServerId", services[position].uid)
            pref.apply()
            (mContext as Activity).startActivity(Intent(mContext, ChangeService::class.java))
        }


    }

    override fun getItemCount(): Int {
        return services.size
    }



}