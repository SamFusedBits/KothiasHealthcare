package com.example.ksharsutra

import com.example.ksharsutra.BuildConfig
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var emailOrPhoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var signupTextView: TextView
    private var verificationId: String? = null
    private var isOtpRequestInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        emailOrPhoneEditText = findViewById(R.id.email_or_phone)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)
        forgotPasswordTextView = findViewById(R.id.forgot_password)
        signupTextView = findViewById(R.id.create_account)

        // Check if the user is already logged in
        loginButton.setOnClickListener {
            loginUser()
        }

        // Handle forgot password or send OTP based on input
        forgotPasswordTextView.setOnClickListener {
            val input = emailOrPhoneEditText.text.toString().trim()
            if (input.contains("@")) {
                // Handle email forgot password
                sendPasswordResetEmail(input)
            } else {
                // Handle phone OTP
                sendOtpToPhone(input)
            }
        }

        // Handle signup click
        signupTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Add a text change listener to the emailOrPhoneEditText
        emailOrPhoneEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No-op
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // If the input is a phone number, change the forgotPasswordTextView to "Send OTP"
                // and clear the passwordEditText field
                if (s != null && s.length == 10 && !s.contains("@") && !isOtpRequestInProgress) {
                    forgotPasswordTextView.text = "Send OTP"
                    passwordEditText.hint = "Enter your OTP here"
                    passwordEditText.text.clear() // Clear password field
                } else {
                    // Reset to default if the input is not a phone number
                    forgotPasswordTextView.text = "Forgot your Password?"
                    passwordEditText.hint = "Password"
                }
            }
        })
    }

    // Function to log in a user
    private fun loginUser() {
        val input = emailOrPhoneEditText.text.toString().trim()
        val passwordOrOtp = passwordEditText.text.toString().trim()

        // Validate input
        if (TextUtils.isEmpty(input)) {
            emailOrPhoneEditText.error = "Email or phone number is required."
            return
        }

        if (TextUtils.isEmpty(passwordOrOtp)) {
            passwordEditText.error = "Password or OTP is required."
            return
        }

        if (input.contains("@")) {
            // Email login
            loginWithEmail(input, passwordOrOtp)
        } else {
            // Phone login with OTP
            verifyPhoneWithOtp(input, passwordOrOtp)
        }
    }

    // Function to log in a user with email and password
    private fun loginWithEmail(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    if (user?.isEmailVerified == true) {
                        navigateToAppropriateActivity(user.email)
                    } else {
                        Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show()
                        mAuth.signOut() // Sign out unverified user
                    }
                } else {
                    Toast.makeText(this, "Please enter valid credentials.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to send a password reset email
    private fun sendPasswordResetEmail(email: String) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to send OTP to phone number
    private fun sendOtpToPhone(phone: String) {
        var formattedPhone = phone
        if (!phone.startsWith("+91")) {
            formattedPhone = "+91$phone"
        }

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    isOtpRequestInProgress = false
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isOtpRequestInProgress = false
                    Toast.makeText(this@LoginActivity, "Phone verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    isOtpRequestInProgress = true
                    this@LoginActivity.verificationId = verificationId
                    Toast.makeText(this@LoginActivity, "OTP sent to your phone.", Toast.LENGTH_SHORT).show()
                    // Update UI for OTP entry
                    passwordEditText.hint = "Enter your OTP here"
                    passwordEditText.text.clear() // Clear password field
                    forgotPasswordTextView.text = "Resend OTP"
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Toast.makeText(this, "Sending OTP to your phone...", Toast.LENGTH_SHORT).show()
    }

    // Function to verify phone number with OTP
    private fun verifyPhoneWithOtp(phone: String, otp: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Query Firestore to check if a document with the user's phone number exists
        firestore.collection("users")
            .whereEqualTo("phoneNumber", phone)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // No document with the user's phone number exists, prompt the user to sign up
                    Toast.makeText(this, "Please sign up using your phone number first.", Toast.LENGTH_SHORT).show()
                } else {
                    // Document with the user's phone number exists, proceed with login
                    val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                    signInWithPhoneAuthCredential(credential)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to verify phone number: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to sign in with phone auth credential
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToAppropriateActivity(mAuth.currentUser?.phoneNumber)
                } else {
                    Toast.makeText(this, "Invalid OTP.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to navigate to the appropriate activity based on the user's role
    private fun navigateToAppropriateActivity(userIdentifier: String?) {
        when (userIdentifier) {
            BuildConfig.DOCTOR_EMAIL -> {
                Log.d("LoginActivity", "Doctor login successful")
                startActivity(Intent(this, ManageReportsActivity::class.java))
            }
            BuildConfig.STAFF_EMAIL -> {
                Log.d("LoginActivity", "Staff login successful")
                startActivity(Intent(this, ManageAppointmentsActivity::class.java))
            }
            else -> {
                Log.d("LoginActivity", "User login successful")
                startActivity(Intent(this, HomePageActivity::class.java))
            }
        }
    }
}