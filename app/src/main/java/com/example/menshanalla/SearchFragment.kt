package com.example.menshanalla

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menshanalla.Adapter.UserAdapter
import com.example.menshanalla.Model.Worker
import com.example.menshanalla.R.layout.fragment_search
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * in this fragment the user that the current user searched for, are being saved and loaded in a RecyclerView
 *
 * @author Amir Azim
 *
 *
 */

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<Worker>? = null
    private lateinit var search_text: EditText
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        search_text = view.findViewById(R.id.search_text)
        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<Worker>,true) }
        recyclerView?.adapter = userAdapter

       search_text.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }


            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               if (search_text.text.toString() == ""){

               }
                else{
                    recyclerView?.visibility = View.VISIBLE

                   collectResultUser()
                   returnResultUser(p0.toString().lowercase())
               }
            }
           override fun afterTextChanged(p0: Editable?) {

           }
        } )

        return view
    }

    private fun returnResultUser(searchValue: String) {
        val workerRefQuery = FirebaseDatabase.getInstance().getReference()
            .child("Workers")
            .orderByChild("workerJob")
            .startAt(searchValue)
            .endAt(searchValue + "\uf8ff")

        workerRefQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                    mUser?.clear()

                    for (snapshot in dataSnapshot.children)
                    {
                        val worker = snapshot.getValue(Worker::class.java)
                        if (worker != null && worker.getWorkerId() != FirebaseAuth.getInstance().currentUser!!.uid){
                            mUser?.add(worker)
                        }
                    }

                    userAdapter?.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun collectResultUser() {
        val workerRef = FirebaseDatabase.getInstance().getReference().child("Workers")
        workerRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (search_text.text.toString() == ""){
                    mUser?.clear()

                    for (snapshot in dataSnapshot.children)
                    {
                        val worker = snapshot.getValue(Worker::class.java)
                        if (worker != null){
                            mUser?.add(worker)
                        }
                    }

                    userAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}