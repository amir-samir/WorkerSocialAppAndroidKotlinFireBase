package com.example.menshanalla.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.menshanalla.Model.Posts
import com.example.menshanalla.Model.Service
import com.example.menshanalla.R

/**
 * this is the  adapter of the services recyclerView in the ProfileFragment.
 *
 * @author Amir Azim
 *
 *
 */

class ServicesProfileAdapter (
    private var mContext: Context,
    private var services: MutableList<Service>,
    private var isFragment: Boolean = false

): RecyclerView.Adapter<ServicesProfileAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val serviceDesc: TextView = itemView.findViewById(R.id.descriptionService)
        val servicePrice: TextView = itemView.findViewById(R.id.priceService)
        val serviceImage: ImageView = itemView.findViewById(R.id.serviceImageRecyc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.service_profile_recycler_item,parent,false)
        return ServicesProfileAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.serviceDesc.setText(services[position].description)
        holder.servicePrice.setText(services[position].price)
        Glide.with(mContext).load(services[position].imageUri).centerCrop()
            .into(holder.serviceImage)


    }

    override fun getItemCount(): Int {
        return services.size
    }

}