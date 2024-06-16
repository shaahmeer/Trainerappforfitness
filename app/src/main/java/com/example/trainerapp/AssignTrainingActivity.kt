package com.example.trainerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainerapp.adapter.DateAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class AssignTrainingActivity : AppCompatActivity() {

    private lateinit var recyclerViewDates: RecyclerView
    private lateinit var editTextTrainingSession: EditText
    private lateinit var spinnerTrainingType: Spinner
    private lateinit var editTextTrainingDuration: EditText
    private lateinit var editTextMealPlan: EditText
    private lateinit var editTextTrainingIntensity: EditText
    private lateinit var editTextTrainingFocus: EditText
    private lateinit var editTextEquipmentNeeded: EditText
    private lateinit var editTextAdditionalNotes: EditText
    private lateinit var spinnerTrainingLocation: Spinner
    private lateinit var spinnerTrainer: Spinner
    private lateinit var buttonSaveSession: Button
    private lateinit var buttonUploadVideo: Button
    private lateinit var selectedDate: Date
    private lateinit var adapter: DateAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private var videoUri: Uri? = null

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                videoUri = uri
                Toast.makeText(this, "Video selected: $uri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_training)

        recyclerViewDates = findViewById(R.id.recyclerViewDates)
        editTextTrainingSession = findViewById(R.id.editTextTrainingSession)
        spinnerTrainingType = findViewById(R.id.spinnerTrainingType)
        editTextTrainingDuration = findViewById(R.id.editTextTrainingDuration)
        editTextMealPlan = findViewById(R.id.editTextMealPlan)
        editTextTrainingIntensity = findViewById(R.id.editTextTrainingIntensity)
        editTextTrainingFocus = findViewById(R.id.editTextTrainingFocus)
        editTextEquipmentNeeded = findViewById(R.id.editTextEquipmentNeeded)
        editTextAdditionalNotes = findViewById(R.id.editTextAdditionalNotes)
        spinnerTrainingLocation = findViewById(R.id.spinnerTrainingLocation)
        spinnerTrainer = findViewById(R.id.spinnerTrainer)
        buttonSaveSession = findViewById(R.id.buttonSaveSession)
        buttonUploadVideo = findViewById(R.id.buttonUploadVideo)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId") ?: ""
        if (userId.isEmpty()) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show()
            finish()
        }

        selectedDate = Calendar.getInstance().time

        setupDateRecyclerView()
        setupSpinnerTrainingType()
        setupSpinnerTrainingLocation()
        setupSpinnerTrainer()

        buttonSaveSession.setOnClickListener {
            saveTrainingSession()
        }

        buttonUploadVideo.setOnClickListener {
            openGalleryForVideo()
        }
    }

    private fun setupDateRecyclerView() {
        val dates = getDatesInMonth()

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewDates.layoutManager = layoutManager
        adapter = DateAdapter(dates, selectedDate) { date ->
            selectedDate = date
        }
        recyclerViewDates.adapter = adapter
    }

    private fun setupSpinnerTrainingType() {
        val trainingTypes = arrayOf("Select Training Type", "Yoga", "Weight Training", "Cardio", "Pilates")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainingTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTrainingType.adapter = adapter
    }

    private fun setupSpinnerTrainingLocation() {
        val locations = arrayOf("Select Location", "Gym", "Park", "Home", "Studio")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTrainingLocation.adapter = adapter
    }

    private fun setupSpinnerTrainer() {
        val trainers = arrayOf("Select Trainer", "Trainer A", "Trainer B", "Trainer C", "Trainer D")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, trainers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTrainer.adapter = adapter
    }

    private fun getDatesInMonth(): List<Date> {
        val cal = Calendar.getInstance()
        val dates = mutableListOf<Date>()

        cal.time = selectedDate
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            dates.add(cal.time)
        }

        return dates
    }

    private fun saveTrainingSession() {
        val session = editTextTrainingSession.text.toString().trim()
        val trainingType = spinnerTrainingType.selectedItem.toString()
        val duration = editTextTrainingDuration.text.toString().trim()
        val mealPlan = editTextMealPlan.text.toString().trim()
        val intensity = editTextTrainingIntensity.text.toString().trim()
        val focus = editTextTrainingFocus.text.toString().trim()
        val equipment = editTextEquipmentNeeded.text.toString().trim()
        val additionalNotes = editTextAdditionalNotes.text.toString().trim()
        val location = spinnerTrainingLocation.selectedItem.toString()
        val trainer = spinnerTrainer.selectedItem.toString()

        if (session.isEmpty() || trainingType == "Select Training Type" || duration.isEmpty() || mealPlan.isEmpty() ||
            intensity.isEmpty() || focus.isEmpty() || equipment.isEmpty() || additionalNotes.isEmpty() ||
            location == "Select Location" || trainer == "Select Trainer") {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateKey = sdf.format(selectedDate)

        val sessionData = hashMapOf(
            "date" to dateKey,
            "session" to session,
            "trainingType" to trainingType,
            "duration" to duration,
            "mealPlan" to mealPlan,
            "intensity" to intensity,
            "focus" to focus,
            "equipment" to equipment,
            "additionalNotes" to additionalNotes,
            "location" to location,
            "trainer" to trainer
        )

        // Check if a video is selected
        videoUri?.let { uri ->
            // Upload the video to Firebase Storage
            uploadVideoToFirebase(uri, sessionData)
        } ?: run {
            // No video selected, save session data directly to Firestore
            saveSessionToFirestore(sessionData)
        }
    }

    private fun openGalleryForVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        pickVideoLauncher.launch(Intent.createChooser(intent, "Select Video"))
    }

    private fun uploadVideoToFirebase(uri: Uri, sessionData: HashMap<String, String>) {

         val storageRef = FirebaseStorage.getInstance().reference.child("videos/${UUID.randomUUID()}")
         storageRef.putFile(uri)
             .addOnSuccessListener { /* Handle successful upload */ }
             .addOnFailureListener { e -> /* Handle unsuccessful upload */ }
             .addOnProgressListener { taskSnapshot -> /* Handle upload progress */ }
             .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                     // Get the download URL
                     storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                         sessionData["videoUrl"] = downloadUri.toString()
                         saveSessionToFirestore(sessionData)
                     }
                 } else {
                     Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show()
                 }
             }
    }

    private fun saveSessionToFirestore(sessionData: HashMap<String, String>) {
        db.collection("users").document(userId)
            .collection("trainingSessions")
            .document(sessionData["date"].toString())
            .set(sessionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Training session saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving session: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
