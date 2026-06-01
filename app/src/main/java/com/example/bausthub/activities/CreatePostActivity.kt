package com.example.bausthub.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var imageUri: Uri? = null
    private lateinit var ivSelectedImage: ImageView
    private lateinit var etCaption: EditText
    private lateinit var pbUpload: ProgressBar
    private lateinit var btnShare: LinearLayout
    private lateinit var tvShare: TextView
    private lateinit var ivSendIcon: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            ivSelectedImage.setImageURI(it)
            ivSelectedImage.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        // Initializing Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initializing Cloudinary (Only if not already initialized)
        try {
            val config = hashMapOf(
                "cloud_name" to "YOUR_CLOUD_NAME", // Change this
                "api_key" to "YOUR_API_KEY",       // Change this
                "api_secret" to "YOUR_API_SECRET"  // Change this
            )
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Already initialized or config error
        }

        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnUploadImage = findViewById<ConstraintLayout>(R.id.btnUploadImage)
        ivSelectedImage = findViewById(R.id.ivSelectedImage)
        etCaption = findViewById(R.id.etCaption)
        btnShare = findViewById(R.id.btnShare)
        pbUpload = findViewById(R.id.pbUpload)
        tvShare = findViewById(R.id.tvShare)
        ivSendIcon = findViewById(R.id.ivSendIcon)

        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)
        val btnSearch = findViewById<ImageButton>(R.id.btnNavSearch)
        val btnProfile = findViewById<ImageButton>(R.id.btnNavProfile)
        val btnNotifications = findViewById<ImageButton>(R.id.btnNavNotifications)

        btnClose.setOnClickListener { finish() }

        btnUploadImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnShare.setOnClickListener {
            uploadPost()
        }

        // Navigation logic
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
        btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun uploadPost() {
        val caption = etCaption.text.toString()

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        // Uploading to Cloudinary
        MediaManager.get().upload(imageUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                    if (imageUrl != null) {
                        savePostToFirestore(imageUrl, caption)
                    } else {
                        setLoading(false)
                        Toast.makeText(this@CreatePostActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    setLoading(false)
                    Toast.makeText(this@CreatePostActivity, "Error: ${error?.description}", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun savePostToFirestore(imageUrl: String, caption: String) {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("students").document(uid).get().addOnSuccessListener { userDoc ->
            val authorName = userDoc.getString("name") ?: "BAUSTian"
            
            val post = hashMapOf(
                "userId" to uid,
                "authorName" to authorName,
                "imageUrl" to imageUrl,
                "caption" to caption,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("posts")
                .add(post)
                .addOnSuccessListener {
                    setLoading(false)
                    Toast.makeText(this, "Post shared successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    setLoading(false)
                    Toast.makeText(this, "Failed to save post data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            pbUpload.visibility = View.VISIBLE
            ivSendIcon.visibility = View.GONE
            tvShare.text = "Uploading..."
            btnShare.isEnabled = false
        } else {
            pbUpload.visibility = View.GONE
            ivSendIcon.visibility = View.VISIBLE
            tvShare.text = "Share to BAUSThub"
            btnShare.isEnabled = true
        }
    }
}
