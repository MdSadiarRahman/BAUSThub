package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.example.bausthub.activities.SearchActivity
import com.example.bausthub.activities.CreatePostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileBio) // আপনার লেআউটে বায়োর জায়গায় ইমেইল দেখাতে পারেন
        
        val btnSignOut = findViewById<TextView>(R.id.btnSignOut)
        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnSignOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadUserData()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("students").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    tvName.text = document.getString("name")
                    val studentId = document.getString("studentId")
                    val dept = document.getString("department")
                    val batch = document.getString("batch")
                    
                    // বায়ো টেক্সটভিউতে অন্য ইনফো দেখাচ্ছি
                    tvEmail.text = "ID: $studentId | Dept: $dept | Batch: $batch"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
