import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.Chat.ChatActivity
import com.example.menshanalla.Chat.ChatActivityCustomer
import com.example.menshanalla.Model.Worker
import com.example.menshanalla.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * this is the adapter of the chat fragment.
 *
 * @author Mohammed Abou
 *
 *
 */

class ChatUserAdapter(val context: Context, val userList: ArrayList<Worker>):
    RecyclerView.Adapter<ChatUserAdapter.UserViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.chat_user_layout,parent,false)
        return UserViewHolder(view)

    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        //get the current user from the user list
        val currentUser = userList[position]

        holder.textName.text= currentUser.getWorkerName()
        holder.userJobInChat.text = currentUser.getWorkerJob()
        Picasso.get().load(currentUser.getProfileImage()).placeholder(R.drawable.profile).into(holder.chatUserImage)

        holder.itemView.setOnClickListener {


            val ref = FirebaseDatabase.getInstance().getReference("Workers")
            ref.child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user info if they are not equal to null
                        val userType = "${snapshot.child("userType").value}"
                        if (userType.equals("worker")){
                            val intent = Intent(context, ChatActivity::class.java)

                            intent.putExtra("name",currentUser.getWorkerName())
                            intent.putExtra("uid",currentUser.getWorkerId())
                            context.startActivity(intent)
                        }
                        if (userType.equals("customer")){
                            val intent = Intent(context, ChatActivityCustomer::class.java)

                            intent.putExtra("name",currentUser.getWorkerName())
                            intent.putExtra("uid",currentUser.getWorkerId())
                            context.startActivity(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                } )

        }

    }

    override fun getItemCount(): Int {
        return userList.size

    }

    class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val textName = itemView.findViewById<TextView>(R.id.txt_name)

        val chatUserImage = itemView.findViewById<CircleImageView>(R.id.chat_user_image)

        val userJobInChat = itemView.findViewById<TextView>(R.id.txt_job)

    }


}