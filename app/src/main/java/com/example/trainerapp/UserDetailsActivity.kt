package com.example.trainerapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.trainerapp.data.Message
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var imageProfile: ImageView
    private lateinit var textName: TextView
    private lateinit var textBodyType: TextView
    private lateinit var textDesiredWeight: TextView
    private lateinit var textGender: TextView
    private lateinit var textWeight: TextView
    private lateinit var textHeight: TextView
    private lateinit var textDateOfBirth: TextView
    private lateinit var textViewType: TextView // Added for displaying interest dynamically

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var chatButton: FloatingActionButton
    private lateinit var chatPopup: View
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatTextView: TextView
    private lateinit var imageButton: Button
    private lateinit var videoButton: Button
    private lateinit var closePopupButton: Button
    private lateinit var seeActivityButton: Button

    private val PICK_IMAGE_REQUEST = 1
    private val PICK_VIDEO_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize views
        imageProfile = findViewById(R.id.imageProfile)
        textName = findViewById(R.id.textName)
        textBodyType = findViewById(R.id.textBodyType)
        textDesiredWeight = findViewById(R.id.textDesiredWeight)
        textGender = findViewById(R.id.textGender)
        textWeight = findViewById(R.id.textWeight)
        textHeight = findViewById(R.id.textHeight)
        textDateOfBirth = findViewById(R.id.textDateOfBirth)
        textViewType = findViewById(R.id.textViewType) // Initialize interest TextView

        // Initialize chat components
        chatButton = findViewById(R.id.chatButton)
        chatPopup = findViewById(R.id.chatPopup)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        chatTextView = findViewById(R.id.chatTextView)
        imageButton = findViewById(R.id.imageButton)
        videoButton = findViewById(R.id.videoButton)
        closePopupButton = findViewById(R.id.closePopupButton)
        seeActivityButton = findViewById(R.id.Assigntraining)

        // Load user details
        val userId = intent.getStringExtra("userId")
        userId?.let { fetchUserDetails(it) }

        // Button click listeners
        seeActivityButton.setOnClickListener {
            userId?.let { uid ->
                val intent = Intent(this, AssignTrainingActivity::class.java)
                intent.putExtra("userId", uid)
                startActivity(intent)
            }
        }

        chatButton.setOnClickListener {
            toggleChatPopupVisibility(userId)
        }

        closePopupButton.setOnClickListener {
            chatPopup.visibility = View.GONE
        }

        sendButton.setOnClickListener {
            if (userId != null) {
                sendMessage()
            }
        }

        imageButton.setOnClickListener {
            chooseImage()
        }

        videoButton.setOnClickListener {
            chooseVideo()
        }
    }

    private fun fetchUserDetails(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve and display user details
                    textName.text = "Name: ${document.getString("name")}"
                    textBodyType.text = "Body Type: ${document.getString("bodyType")}"
                    textDesiredWeight.text = "Desired Weight: ${document.getDouble("desiredWeight")}"
                    textGender.text = "Gender: ${document.getString("gender")}"
                    textWeight.text = "Weight: ${document.getDouble("weight")}"
                    textHeight.text = "Height: ${document.getDouble("height")}"
                    val profileImageUrl = document.getString("profileImage")
                    profileImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .circleCrop()
                            .into(imageProfile)
                    }

                    // Fetch and display user's interest dynamically
                    fetchUserInterest(userId)

                    // Display date of birth
                    displayDateOfBirth(document)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserDetailsActivity", "Error fetching user details", e)
            }
    }

    private fun fetchUserInterest(userId: String) {
        db.collection("users").document(userId).collection("trainingSessions")
            .whereEqualTo("type", "Yoga")
            .get()
            .addOnSuccessListener { sessions ->
                for (session in sessions) {
                    val currentSession = session.getBoolean("currentSession") ?: false
                    val type = session.getString("type") ?: ""

                    // Display or use the fetched session information as needed
                    textViewType.text = "Current Yoga Session: $type"
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserDetailsActivity", "Error fetching training sessions", e)
            }
    }

    private fun displayDateOfBirth(document: DocumentSnapshot) {
        val dateOfBirth = document.get("dateOfBirth") as? Map<String, Any>?
        dateOfBirth?.let {
            val day = it["day"]
            val month = it["month"]
            val year = it["year"]
            textDateOfBirth.text = "Date of Birth: $day.$month.$year"
        }
    }

    private fun toggleChatPopupVisibility(userId: String?) {
        chatPopup.visibility = if (chatPopup.visibility == View.GONE) View.VISIBLE else View.GONE
        if (chatPopup.visibility == View.VISIBLE) {
            if (userId != null) {
                loadChatMessages(userId)
            }
        }
    }

    private fun loadChatMessages(userId: String) {
        db.collection("chats")
            .document(userId)
            .collection("messages")

            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("UserDetailsActivity", "Error fetching chat messages", e)
                    return@addSnapshotListener
                }

                val messages = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                }

                val chatContent = StringBuilder()
                messages?.forEach { message ->
                    message?.let {
                        chatContent.append("${it.senderId}: ${it.text}\n")
                    }
                }

                chatTextView.text = chatContent.toString()
            }
    }


    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        if (messageText.isEmpty()) {
            return
        }

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val message = Message(
                senderId = user.uid,
                text = messageText,
                timestamp = System.currentTimeMillis()
            )

            val userId = intent.getStringExtra("userId") ?: return@let

            db.collection("chats")
                .document(userId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener {
                    messageEditText.text.clear()
                }
                .addOnFailureListener { e ->
                    Log.e("UserDetailsActivity", "Error sending message", e)
                }
        }
    }


    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun chooseVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        startActivityForResult(intent, PICK_VIDEO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            when (requestCode) {
                PICK_IMAGE_REQUEST -> fileUri?.let { uploadFile(it, "image") }
                PICK_VIDEO_REQUEST -> fileUri?.let { uploadFile(it, "video") }
            }
        }
    }

    private fun uploadFile(fileUri: Uri, fileType: String) {
        val userId = intent.getStringExtra("userId") ?: return
        val storageRef =
            storage.reference.child("$fileType/${userId}/${System.currentTimeMillis()}")

        val uploadTask = storageRef.putFile(fileUri)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val message = Message(
                    senderId = userId,
                    timestamp = System.currentTimeMillis()
                ).apply {
                    when (fileType) {
                        "image" -> imageUrl = uri.toString()
                        "video" -> videoUrl = uri.toString()
                    }
                }

                db.collection("chats").document(userId).collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        // Handle success
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserDetailsActivity", "Error uploading file", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("UserDetailsActivity", "Error uploading file", e)
        }
    }
}
