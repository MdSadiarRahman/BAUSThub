package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.example.bausthub.activities.NotificationsActivity

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val btnNotifications = findViewById<ImageButton>(R.id.btnNavNotifications)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }
}
