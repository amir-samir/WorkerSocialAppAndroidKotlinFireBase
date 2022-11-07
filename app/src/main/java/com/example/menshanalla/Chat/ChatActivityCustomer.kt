package com.example.menshanalla.Chat

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.*
import com.example.menshanalla.Adapter.MessageAdapter
import com.example.menshanalla.R
import com.example.menshanalla.databinding.ActivityChatCustomerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * this is the Activity to open when a customer open the chat
 *
 * @author Amir Azim, Mohammed Abou
 *
 *
 */

class ChatActivityCustomer : AppCompatActivity() {

    private lateinit var binding: ActivityChatCustomerBinding
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var  messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var  dataBaseRef : DatabaseReference
    var receiverRoom : String? = null
    var senderRoom: String? =null


    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                changeFragment(homeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_search -> {
                changeFragment(SearchFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_chat -> {
                changeFragment(ChatFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_profile -> {
                changeFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }


        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name =  intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        dataBaseRef = FirebaseDatabase.getInstance().getReference()

        val navView: BottomNavigationView = binding.navViewChat


        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = name

        chatRecyclerView = binding.chatRecyclerView
        chatRecyclerView.visibility = View.VISIBLE
        binding.sentButton.visibility = View.VISIBLE
        binding.messageBox.visibility = View.VISIBLE
        messageBox = binding.messageBox
        sendButton = binding.sentButton
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this,messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter


        retrieveData()

        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val messageObject = Message(message,senderUid)

            dataBaseRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    dataBaseRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }

            messageBox.setText("")
            retrieveData()

        }
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)



    }


    fun changeFragment(fragment: Fragment){
        chatRecyclerView.visibility = View.GONE
        binding.sentButton.visibility = View.GONE
        binding.messageBox.visibility = View.GONE
        val fragmentChange = supportFragmentManager.beginTransaction()
        fragmentChange.replace(R.id.fragment_container, fragment)
        fragmentChange.commit()
    }

    private fun retrieveData() {
        dataBaseRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList!!.clear()

                    for (postSnapshot in snapshot.children) {
                        val message: Message? = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}