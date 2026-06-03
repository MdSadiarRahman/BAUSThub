package com.example.bausthub.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.bausthub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private lateinit var etName: EditText
    private lateinit var etBio: EditText
    private lateinit var btnConfirm: Button
    private lateinit var btnDiscard: Button
    private lateinit var ivProfilePic: ImageView
    private lateinit var ivCoverPic: ImageView
    private lateinit var tabProfile: TextView
    private lateinit var tabPassword: TextView
    private lateinit var layoutProfileFields: LinearLayout
    private lateinit var layoutPasswordFields: LinearLayout
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    
    private var profileUri: Uri? = null
    private var coverUri: Uri? = null
    private var profileImageUrl: String? = null
    private var coverImageUrl: String? = null

    private val pickProfileImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            profileUri = it
            ivProfilePic.setImageURI(it)
        }
    }

    private val pickCoverImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coverUri = it
            ivCoverPic.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etSettingsName)
        etBio = findViewById(R.id.etSettingsBio)
        btnConfirm = findViewById(R.id.btnConfirmChanges)
        btnDiscard = findViewById(R.id.btnDiscard)
        ivProfilePic = findViewById(R.id.ivSettingsProfilePic)
        ivCoverPic = findViewById(R.id.ivSettingsCover)
        tabProfile = findViewById(R.id.tabProfile)
        tabPassword = findViewById(R.id.tabPassword)
        layoutProfileFields = findViewById(R.id.layoutProfileFields)
        layoutPasswordFields = findViewById(R.id.layoutPasswordFields)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)

        loadUserData()

        findViewById<View>(R.id.btnChangeProfile).setOnClickListener {
            pickProfileImage.launch("image/*")
        }

        findViewById<View>(R.id.btnChangeCover).setOnClickListener {
            pickCoverImage.launch("image/*")
        }

        tabProfile.setOnClickListener {
            tabProfile.setBackgroundResource(R.drawable.bg_toggle_active)
            tabProfile.setTextColor(ContextCompat.getColor(this, R.color.baust_green))
            tabPassword.setBackground(null)
            tabPassword.setTextColor(ContextCompat.getColor(this, R.color.text_grey))
            
            layoutProfileFields.visibility = View.VISIBLE
            layoutPasswordFields.visibility = View.GONE
            btnConfirm.text = "CONFIRM CHANGES"
        }

        tabPassword.setOnClickListener {
            tabPassword.setBackgroundResource(R.drawable.bg_toggle_active)
            tabPassword.setTextColor(ContextCompat.getColor(this, R.color.baust_green))
            tabProfile.setBackground(null)
            tabProfile.setTextColor(ContextCompat.getColor(this, R.color.text_grey))
            
            layoutProfileFields.visibility = View.GONE
            layoutPasswordFields.visibility = View.VISIBLE
            btnConfirm.text = "UPDATE PASSWORD"
        }

        btnDiscard.setOnClickListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            if (layoutProfileFields.visibility == View.VISIBLE) {
                startUploadProcess()
            } else {
                updatePassword()
            }
        }
    }

    private fun startUploadProcess() {
        val newName = etName.text.toString().trim()
        if (newName.isEmpty()) {
            etName.error = "Name cannot be empty"
            return
        }

        btnConfirm.isEnabled = false
        btnConfirm.text = "Uploading..."

        if (profileUri != null && coverUri != null) {
            uploadImage(profileUri!!, true) {
                uploadImage(coverUri!!, false) {
                    saveUserData()
                }
            }
        } else if (profileUri != null) {
            uploadImage(profileUri!!, true) {
                saveUserData()
            }
        } else if (coverUri != null) {
            uploadImage(coverUri!!, false) {
                saveUserData()
            }
        } else {
            saveUserData()
        }
    }

    private fun uploadImage(uri: Uri, isProfile: Boolean, onComplete: () -> Unit) {
        MediaManager.get().upload(uri)
            .option("unsigned", true)
            .option("upload_preset", "bausthub_preset")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    if (isProfile) profileImageUrl = url else coverImageUrl = url
                    onComplete()
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "CONFIRM CHANGES"
                    Toast.makeText(this@SettingsActivity, "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun updatePassword() {
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmNewPassword.text.toString()

        if (newPassword.length < 6) {
            etNewPassword.error = "Password must be at least 6 characters"
            return
        }

        if (newPassword != confirmPassword) {
            etConfirmNewPassword.error = "Passwords do not match"
            return
        }

        val user = auth.currentUser
        user?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Failed to update password. Please re-login and try again.", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("students").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    etName.setText(document.getString("name"))
                    etBio.setText(document.getString("bio"))
                    profileImageUrl = document.getString("profileImage")
                    coverImageUrl = document.getString("coverImage")
                    
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profileImageUrl).into(ivProfilePic)
                    }
                    if (!coverImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(coverImageUrl).into(ivCoverPic)
                    }
                }
            }
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return
        val newName = etName.text.toString().trim()
        val newBio = etBio.text.toString().trim()

        val updates = hashMapOf<String, Any>(
            "name" to newName,
            "bio" to newBio
        )
        
        profileImageUrl?.let { updates["profileImage"] = it }
        coverImageUrl?.let { updates["coverImage"] = it }

        db.collection("students").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btnConfirm.isEnabled = true
                btnConfirm.text = "CONFIRM CHANGES"
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}
