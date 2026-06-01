package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.MainActivity
import com.example.bausthub.R

class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)

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

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
}
