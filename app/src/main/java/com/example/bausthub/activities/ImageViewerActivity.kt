package com.example.bausthub.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bausthub.R

class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val imageUrl = intent.getStringExtra("imageUrl")
        val authorName = intent.getStringExtra("authorName")
        val caption = intent.getStringExtra("caption")

        val ivFullImage = findViewById<ImageView>(R.id.ivFullImage)
        val tvAuthorName = findViewById<TextView>(R.id.tvAuthorName)
        val tvCaption = findViewById<TextView>(R.id.tvCaption)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        tvAuthorName.text = authorName
        tvCaption.text = caption

        Glide.with(this)
            .load(imageUrl)
            .into(ivFullImage)

        btnClose.setOnClickListener {
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, android.R.anim.fade_out)
    }
}
