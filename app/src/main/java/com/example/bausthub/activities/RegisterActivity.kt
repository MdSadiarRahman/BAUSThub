package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bausthub.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameInput = findViewById<EditText>(R.id.etRegisterName)
        val emailInput = findViewById<EditText>(R.id.etRegisterEmail)
        val passwordInput = findViewById<EditText>(R.id.etRegisterPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)
        val goLoginText = findViewById<TextView>(R.id.tvGoLogin)
        val progressBar = findViewById<ProgressBar>(R.id.pbRegister)

        registerButton.setOnClickListener {
            // Register logic removed
        }

        goLoginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
