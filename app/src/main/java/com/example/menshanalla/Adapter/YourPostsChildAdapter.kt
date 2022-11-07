package com.example.menshanalla.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.menshanalla.Model.Posts
import com.example.menshanalla.R

/**
 * this is the child adapter of the users project to choose them and edit them or delete them.
 *
 * @author Amir Azim
 *
 *
 */

class YourPostsChildAdapter(
    private var mContext: Context,
    private var posts: Posts?,
    private var isFragment: Boolean = false

): RecyclerView.Adapter<YourPostsChildAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val cardImage: ImageView = itemView.findViewById(R.id.iv_child_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.child_rv_layout,parent,false)
        return YourPostsChildAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //holder.imageDesc.text = posts.posts!!.description
        //holder.cardImage.setImageURI(imagesList[position].get(0).toUri())
        Glide.with(mContext).load(posts!!.posts!!.posting!![position]).centerCrop()
            .into(holder.cardImage)


    }

    override fun getItemCount(): Int {
        return posts!!.posts!!.posting!!.size
    }

}