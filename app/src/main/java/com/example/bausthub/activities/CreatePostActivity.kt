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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        try {
            val config = hashMapOf(
                "cloud_name" to "dvjgbhfog",
                "api_key" to "448829959477278",
                "api_secret" to "uEx0_-X-2dzKH67PjiHGwBjCZhM"
            )
            MediaManager.init(this, config)
        } catch (e: Exception) {
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

        btnShare.setOnClickListener {
            uploadPost()
        }

        btnUploadImage.setOnClickListener {
            pickImage.launch("image/*")
        }

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
        val caption = etCaption.text.toString().trim()

        if (caption.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Please write something or select an image", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .option("unsigned", true)
                .option("upload_preset", "bausthub_preset")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        if (imageUrl != null) {
                            savePostToFirestore(imageUrl, caption)
                        } else {
                            setLoading(false)
                            Toast.makeText(this@CreatePostActivity, "Upload failed: URL not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        setLoading(false)
                        Toast.makeText(this@CreatePostActivity, "Error: ${error?.description}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            savePostToFirestore("", caption)
        }
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
                .addOnSuccessListener { postDoc ->
                    db.collection("students").document(uid)
                        .update("postsCount", com.google.firebase.firestore.FieldValue.increment(1))

                    sendNotificationsToFollowers(uid, authorName, postDoc.id)

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

    private fun sendNotificationsToFollowers(uid: String, authorName: String, postId: String) {
        db.collection("students").document(uid).collection("followers")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val followerId = doc.id
                    val notification = hashMapOf(
                        "type" to "post",
                        "fromId" to uid,
                        "fromName" to authorName,
                        "timestamp" to System.currentTimeMillis(),
                        "message" to "uploaded a new post.",
                        "isRead" to false,
                        "postId" to postId
                    )
                    db.collection("students").document(followerId).collection("notifications").add(notification)
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
