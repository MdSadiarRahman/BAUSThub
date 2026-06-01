package com.example.bausthub.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.bausthub.MainActivity
import com.example.bausthub.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed(
            {
                // ইউজার লগইন করা আছে কিনা চেক করা হচ্ছে
                if (auth.currentUser != null) {
                    // লগইন করা থাকলে সরাসরি MainActivity-তে যাবে
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // লগইন করা না থাকলে LoginActivity-তে যাবে
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            },
            1800,
        )
    }
}
