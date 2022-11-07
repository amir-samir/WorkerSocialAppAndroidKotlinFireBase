package com.example.menshanalla.Adapter

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.menshanalla.Model.Posts
import com.example.menshanalla.R

/**
 * this is the parent adapter of the posts in the profile fragment.
 *
 * @author Amir Azim
 *
 *
 */

class PostsParentAdapter (
    private var mContext: Context,
    private var posts: MutableList<Posts>,
    private var isFragment: Boolean = false

): RecyclerView.Adapter<PostsParentAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val recycleView: RecyclerView = itemView.findViewById(R.id.rv_child)
        val tv_parent_title: TextView = itemView.findViewById(R.id.tv_parent_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(ContentValues.TAG,"Im Here")
        val view = LayoutInflater.from(mContext).inflate(R.layout.parent_rv_layout,parent,false)
        return PostsParentAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv_parent_title.setText(posts[position].posts!!.description)
        val childAdapter = PostsAdapter(mContext,posts.get(position))
        holder.recycleView.layoutManager = LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false)
        holder.recycleView.adapter = childAdapter
        childAdapter.notifyDataSetChanged()

    }

    override fun getItemCount(): Int {
        return posts!!.size
    }
}