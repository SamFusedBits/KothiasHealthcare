package com.example.ksharsutra

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var dob: EditText
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mAuth = FirebaseAuth.getInstance()
        usernameEditText = findViewById(R.id.username)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirm_password)
        dob = findViewById(R.id.dob)
        signupButton = findViewById(R.id.signup)

        signupButton.setOnClickListener {
            signUpUser()
        }
    }

    // Function to sign up a new user
    private fun signUpUser() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val dob = dob.text.toString().trim()

        // Validate the input fields
        if (TextUtils.isEmpty(username)) {
            usernameEditText.error = "Username is required."
            return
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.error = "Email is required."
            return
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.error = "Password is required."
            return
        }

        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match."
            return
        }

        // Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Send email verification to the user
                    val user = mAuth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            // Check if the verification email was sent successfully
                            if (verificationTask.isSuccessful) {
                                // Save user details to Firestore
                                user?.let {
                                    val userId = it.uid
                                    val userMap = hashMapOf(
                                        "username" to username,
                                        "email" to email,
                                        "dob" to dob
                                    )
                                    // Save user details to Firestore
                                    FirebaseFirestore.getInstance().collection("users").document(userId)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Registration succeeded. Please check your email for verification.", Toast.LENGTH_SHORT).show()
                                            // Sign out user to prevent unverified access
                                            mAuth.signOut()
                                            // Navigate to login screen
                                            startActivity(Intent(this, LoginActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to save user details: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Failed to send verification email: ${verificationTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}