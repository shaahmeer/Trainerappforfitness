package com.example.trainerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainerapp.adapter.UserAdapter
import com.example.trainerapp.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), UserAdapter.UserClickListener {

    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private var users: MutableList<User> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        userAdapter = UserAdapter(users, this)
        recyclerViewUsers.adapter = userAdapter

        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                users.clear()
                for (document in documents) {
                    val userId = document.id
                    val name = document.getString("name") ?: ""
                    val profileImage = document.getString("profileImage") ?: ""
                    val user = User(userId, name, profileImage)
                    users.add(user)
                }
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    override fun onUserClick(user: User) {
        val intent = Intent(this, UserDetailsActivity::class.java)
        intent.putExtra("userId", user.id)
        startActivity(intent)
    }
}
