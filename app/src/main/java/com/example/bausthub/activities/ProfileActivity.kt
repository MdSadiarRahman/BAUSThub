package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.google.android.material.imageview.ShapeableImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfilePic: ShapeableImageView
    private lateinit var tvName: TextView
    private lateinit var tvBio: TextView
    private lateinit var pbProfilePic: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvName = findViewById(R.id.tvProfileName)
        tvBio = findViewById(R.id.tvProfileBio)
        pbProfilePic = findViewById(R.id.pbProfilePic)
        
        val btnUpload = findViewById<ImageButton>(R.id.btnUploadPic)
        val btnSignOut = findViewById<TextView>(R.id.btnSignOut)
        val btnEdit = findViewById<Button>(R.id.btnEditProfile)
        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)

        btnUpload.setOnClickListener {
            // Upload logic removed
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnSignOut.setOnClickListener {
            // Sign out logic removed
        }

        btnEdit.setOnClickListener {
            Toast.makeText(this, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
