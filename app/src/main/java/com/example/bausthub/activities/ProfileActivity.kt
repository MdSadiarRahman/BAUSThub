package com.example.bausthub.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    
    private lateinit var ivProfilePic: ShapeableImageView
    private lateinit var tvName: TextView
    private lateinit var tvBio: TextView
    private lateinit var pbProfilePic: ProgressBar

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToStorage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvName = findViewById(R.id.tvProfileName)
        tvBio = findViewById(R.id.tvProfileBio)
        pbProfilePic = findViewById(R.id.pbProfilePic)
        
        val btnUpload = findViewById<ImageButton>(R.id.btnUploadPic)
        val btnSignOut = findViewById<TextView>(R.id.btnSignOut)
        val btnEdit = findViewById<Button>(R.id.btnEditProfile)
        val btnHome = findViewById<ImageButton>(R.id.btnNavHome)

        btnUpload.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0, 0)
        }

        btnSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnEdit.setOnClickListener {
            Toast.makeText(this, "Edit profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        loadUserData()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("students").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    tvName.text = document.getString("name") ?: "N/A"
                    val bio = document.getString("bio")
                    if (bio != null) {
                        tvBio.text = bio
                    }
                    
                    val profileImageUrl = document.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.mipmap.ic_launcher)
                            .into(ivProfilePic)
                    }
                }
            }
    }

    private fun uploadImageToStorage(imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_pics/$uid.jpg")

        pbProfilePic.visibility = View.VISIBLE
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageUrlToFirestore(downloadUri.toString())
                }
            }
            .addOnFailureListener { e ->
                pbProfilePic.visibility = View.GONE
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToFirestore(url: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("students").document(uid)
            .update("profileImageUrl", url)
            .addOnSuccessListener {
                pbProfilePic.visibility = View.GONE
                Glide.with(this).load(url).into(ivProfilePic)
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                pbProfilePic.visibility = View.GONE
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
