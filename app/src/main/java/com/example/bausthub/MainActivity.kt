package com.example.bausthub

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.activities.ProfileActivity
import com.example.bausthub.activities.SearchActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val navSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnAdd = findViewById<ImageButton>(R.id.btnAdd)

        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }

        navSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            overridePendingTransition(0, 0)
        }
        
        btnAdd.setOnClickListener {
            // Add post logic here
        }
    }
}
