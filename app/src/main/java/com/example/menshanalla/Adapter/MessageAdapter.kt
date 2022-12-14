package com.example.menshanalla.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.Chat.Message
import com.example.menshanalla.R
import com.google.firebase.auth.FirebaseAuth

/**
 * this is the adapter of the Messages.
 *
 * @author Mohammed Abou
 *
 *
 */

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_Receive = 1
    val ITEM_SENT = 2




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == 1){
            //inflate receive
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive,parent,false)
            return ReceiveViewHolder(view)
        } else{
            // infalate sent
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent,parent,false)
            return SentViewHolder(view)
        }



    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage = messageList[position]
        if(holder.javaClass == SentViewHolder::class.java){
            // do the stuff  for sent view holder

            val viewHolder = holder as SentViewHolder
            holder.sentMessage.text = currentMessage.message

        }else{
            // do stuff for recieve view holder
            val viewHolder = holder as ReceiveViewHolder
            holder.receiveMessage.text = currentMessage.message
        }

    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)) {
            return ITEM_SENT
        }
        else {
            return ITEM_Receive
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    //initialize the textView from send
    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val sentMessage = itemView.findViewById<TextView>(R.id.txt_sent_message)
    }

    //initialize the textView from receive
    class ReceiveViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receiveMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)

    }


}