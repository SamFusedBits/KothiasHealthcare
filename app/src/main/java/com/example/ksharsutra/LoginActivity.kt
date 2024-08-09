package com.example.ksharsutra

import com.example.ksharsutra.BuildConfig
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var signupTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)
        forgotPasswordTextView = findViewById(R.id.forgot_password)
        signupTextView = findViewById(R.id.create_account)

        // Check if the user is already logged in
        loginButton.setOnClickListener {
            loginUser()
        }

        // Handle forgot password click
        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            // Validate email
            if (TextUtils.isEmpty(email)) {
                emailEditText.error = "Email is required."
                return@setOnClickListener
            }

            // Send password reset email
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Handle signup click
        signupTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    // Login user with email and password
    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate email and password
        if (TextUtils.isEmpty(email)) {
            emailEditText.error = "Email is required."
            return
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.error = "Password is required."
            return
        }

        // Sign in user with email and password
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Check if the user's email is verified
                    val user = mAuth.currentUser
                    if (user?.isEmailVerified == true) {
                        // Check the user's email and navigate to the appropriate activity
                        when (user.email) {
                            BuildConfig.DOCTOR_EMAIL -> {
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, ManageReportsActivity::class.java))
                            }
                            BuildConfig.STAFF_EMAIL -> {
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, ManageAppointmentsActivity::class.java))
                            }
                            else -> {
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomePageActivity::class.java))
                            }
                        }
                        finish()
                    } else {
                        Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show()
                        mAuth.signOut() // Sign out unverified user
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}