package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.MainActivity
import com.example.bausthub.R

class CreatePostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnShare = findViewById<LinearLayout>(R.id.btnShare)
        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val btnAddActive = findViewById<ImageButton>(R.id.btnAddActive)

        btnClose.setOnClickListener {
            finish()
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            finish()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
        
        btnAddActive.setOnClickListener {
            // Already on Create Post screen
        }

        btnShare.setOnClickListener {
            // Logic to share post
            finish()
        }
    }
}
