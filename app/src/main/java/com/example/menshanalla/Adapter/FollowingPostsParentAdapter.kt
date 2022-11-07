package com.example.menshanalla.Adapter

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.Model.FollowingPosting
import com.example.menshanalla.Model.Posts
import com.example.menshanalla.ProfileFragment
import com.example.menshanalla.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

/**
 * this is the parent adapter of the post of followings recyclerView.
 *
 * @author Amir Azim
 *
 *
 */

class FollowingPostsParentAdapter  (
    private var mContext: Context,
    private var finalPosts: MutableList<FollowingPosting>? = null,
    //private var uid: String,
    private var isFragment: Boolean = false

): RecyclerView.Adapter<FollowingPostsParentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recycleView: RecyclerView = itemView.findViewById(R.id.follow_post_child)
        val tv_follow_parent_description: TextView = itemView.findViewById(R.id.tv_follow_parent_title)
        val tv_parent_fullname: TextView = itemView.findViewById(R.id.tv_parent_fullname)
        val tv_parent_job: TextView = itemView.findViewById(R.id.tv_parent_job)
        var workerProfileImage: CircleImageView = itemView.findViewById(R.id.worker_profile_image_posts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(ContentValues.TAG, "Im Here")
        val view = LayoutInflater.from(mContext).inflate(R.layout.parent_followpost_layout, parent, false)
        return FollowingPostsParentAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv_follow_parent_description.setText(finalPosts!![position].thePosts!![position].posts!!.description)
        val ref = FirebaseDatabase.getInstance().getReference("Workers")
        ref.child(finalPosts!![position].uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info if they are not equal to null
                    val job = "${snapshot.child("workerJob").value}"
                    val fullName = "${snapshot.child("workerName").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    holder.tv_parent_fullname.setText(fullName)
                    holder.tv_parent_job.setText(job)
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(holder.workerProfileImage)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        val childAdapter = FollowingPostsAdapter(mContext,finalPosts!!.get(position).thePosts!!.get(position)
        )
        holder.recycleView.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.HORIZONTAL, false
        )
        holder.recycleView.adapter = childAdapter
        childAdapter.notifyDataSetChanged()

        holder.itemView.setOnClickListener(View.OnClickListener {
            val pref = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            pref.putString("profileId", finalPosts!![position].uid.toString())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                ProfileFragment()
            ).commit()


        })

    }

    override fun getItemCount(): Int {
        return finalPosts!!.size
    }
}