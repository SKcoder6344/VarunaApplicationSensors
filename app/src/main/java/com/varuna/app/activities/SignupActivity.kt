package com.varuna.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varuna.app.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val village = binding.etVillage.text.toString().trim()
            val role = if (binding.rbAdmin.isChecked) "admin" else "user"

            if (validateInputs(name, email, password, confirmPassword, village)) {
                performSignup(name, email, password, village, role)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(
        name: String, email: String, password: String,
        confirmPassword: String, village: String
    ): Boolean {
        if (name.isEmpty()) { binding.etName.error = "Name required"; return false }
        if (email.isEmpty()) { binding.etEmail.error = "Email required"; return false }
        if (village.isEmpty()) { binding.etVillage.error = "Village/Location required"; return false }
        if (password.isEmpty()) { binding.etPassword.error = "Password required"; return false }
        if (password.length < 6) { binding.etPassword.error = "Minimum 6 characters"; return false }
        if (password != confirmPassword) { binding.etConfirmPassword.error = "Passwords do not match"; return false }
        return true
    }

    private fun performSignup(name: String, email: String, password: String, village: String, role: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignup.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "village" to village,
                        "role" to role,
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }
                        .addOnFailureListener {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSignup.isEnabled = true
                            Toast.makeText(this, "Error saving user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignup.isEnabled = true
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
