package com.example.ksharsutra

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

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

    private fun signUpUser() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val dob = dob.text.toString().trim()

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

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    Toast.makeText(this, "Registration succeeded.", Toast.LENGTH_SHORT).show()
                    // Navigate to main activity or perform additional action
                    startActivity(Intent(this, HomePageActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}