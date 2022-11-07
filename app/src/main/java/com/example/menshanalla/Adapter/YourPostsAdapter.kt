package com.example.menshanalla.Adapter

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.*
import com.example.menshanalla.Model.PostAndId
import com.example.menshanalla.Posts.EditPostsActivity
import com.example.menshanalla.Posts.YourPostsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * this is the parent adapter of the users project to choose them and edit them or delete them.
 *
 * @author Amir Azim
 *
 *
 */
class YourPostsAdapter(
    private var mContext: Context,
    private var posts: MutableList<PostAndId>,
    private var isFragment: Boolean = false

): RecyclerView.Adapter<YourPostsAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val recycleView: RecyclerView = itemView.findViewById(R.id.post_images_recycler_view)
        val your_posts_title: TextView = itemView.findViewById(R.id.posts_description_recycler)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_button_posts_recycler)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button_posts_recycler)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(ContentValues.TAG,"Im Here")
        val view = LayoutInflater.from(mContext).inflate(R.layout.your_posts_recycler_layout,parent,false)
        return YourPostsAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
        pref.putString("PostId", posts[position].uid)
        pref.apply()


        //handle click on delete post button
        holder.deleteButton.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
            firestore.collection(FirebaseAuth.getInstance().currentUser!!.uid).document(posts[position].uid.toString()).delete()
            (mContext as Activity).startActivity(Intent(mContext, YourPostsActivity::class.java))
        }

        //handle click on edit button
        holder.editButton.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("PostId", posts[position].uid)
            pref.apply()
            (mContext as Activity).startActivity(Intent(mContext, EditPostsActivity::class.java))
        }
        //set the description of the post
        holder.your_posts_title.text = posts[position].thePosts!!.posts!!.description
        // set the childAdapter and the second RecyclerView to send the posts to the childAdapter
        val childAdapter = YourPostsChildAdapter(mContext, posts[position].thePosts)
        holder.recycleView.layoutManager = LinearLayoutManager(mContext,
            LinearLayoutManager.HORIZONTAL,false)
        holder.recycleView.adapter = childAdapter
        childAdapter.notifyDataSetChanged()

    }

    override fun getItemCount(): Int {
        return posts!!.size
    }
}