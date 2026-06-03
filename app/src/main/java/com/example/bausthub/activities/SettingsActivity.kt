package com.example.bausthub.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
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
    private lateinit var tabProfile: TextView
    private lateinit var tabPassword: TextView
    private lateinit var layoutProfileFields: LinearLayout
    private lateinit var layoutPasswordFields: LinearLayout
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText

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
        tabProfile = findViewById(R.id.tabProfile)
        tabPassword = findViewById(R.id.tabPassword)
        layoutProfileFields = findViewById(R.id.layoutProfileFields)
        layoutPasswordFields = findViewById(R.id.layoutPasswordFields)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)

        loadUserData()

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
                saveUserData()
            } else {
                updatePassword()
            }
        }
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
                    val imageUrl = document.getString("profileImage")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(ivProfilePic)
                    }
                }
            }
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return
        val newName = etName.text.toString().trim()
        val newBio = etBio.text.toString().trim()

        if (newName.isEmpty()) {
            etName.error = "Name cannot be empty"
            return
        }

        val updates = hashMapOf<String, Any>(
            "name" to newName,
            "bio" to newBio
        )

        db.collection("students").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}
