package com.varuna.app.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.varuna.app.R

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        // Splash delay - then route based on auth state
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, 2500)
    }

    private fun checkAuthAndNavigate() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User already logged in → Go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Not logged in → Go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
