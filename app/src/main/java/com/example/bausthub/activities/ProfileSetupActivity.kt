package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val studentIdInput = findViewById<EditText>(R.id.etStudentId)
        val departmentInput = findViewById<EditText>(R.id.etDepartment)
        val batchInput = findViewById<EditText>(R.id.etBatch)
        val saveButton = findViewById<Button>(R.id.btnSaveProfile)
        val progressBar = findViewById<ProgressBar>(R.id.pbProfileSetup)

        val name = intent.getStringExtra("name") ?: ""
        val emailFromIntent = intent.getStringExtra("email") ?: ""

        saveButton.setOnClickListener {
            val uid = auth.currentUser?.uid
            val email = auth.currentUser?.email ?: emailFromIntent

            val studentId = studentIdInput.text.toString().trim()
            val department = departmentInput.text.toString().trim()
            val batch = batchInput.text.toString().trim()

            if (uid == null) {
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (studentId.isEmpty() || department.isEmpty() || batch.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            // Firestore-এ ডাটা রাখার জন্য Map তৈরি
            val userProfile = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "studentId" to studentId,
                "department" to department,
                "batch" to batch,
                "postsCount" to 0,
                "followersCount" to 0,
                "followingCount" to 0
            )

            // "students" নামক কালেকশনে ডাটা সেভ করা
            db.collection("students").document(uid)
                .set(userProfile)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile saved to Firestore!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    saveButton.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
