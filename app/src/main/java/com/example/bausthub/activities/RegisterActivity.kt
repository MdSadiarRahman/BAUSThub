package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val nameInput = findViewById<EditText>(R.id.etRegisterName)
        val emailInput = findViewById<EditText>(R.id.etRegisterEmail)
        val passwordInput = findViewById<EditText>(R.id.etRegisterPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)
        val goLoginText = findViewById<TextView>(R.id.tvGoLogin)
        val progressBar = findViewById<ProgressBar>(R.id.pbRegister)

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isEmpty()) {
                nameInput.error = "Enter your name"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailInput.error = "Enter your email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Enter your password"
                return@setOnClickListener
            }

            if (password.length < 6) {
                passwordInput.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            registerButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Log.d("RegisterActivity", "createUserWithEmail:success")
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, ProfileSetupActivity::class.java)
                        intent.putExtra("name", name)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        registerButton.visibility = View.VISIBLE
                        val e = task.exception
                        Log.w("RegisterActivity", "createUserWithEmail:failure", e)
                        
                        val errorMessage = when (e) {
                            is FirebaseAuthException -> {
                                when (e.errorCode) {
                                    "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
                                    "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                                    "ERROR_WEAK_PASSWORD" -> "The password is too weak."
                                    else -> e.message
                                }
                            }
                            else -> e?.message ?: "Unknown error occurred"
                        }
                        
                        Toast.makeText(this, "Failed: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
        }

        goLoginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
