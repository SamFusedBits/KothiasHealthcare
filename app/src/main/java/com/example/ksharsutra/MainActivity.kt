package com.example.ksharsutra

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            // User is logged in, navigate to the appropriate activity based on email
            navigateBasedOnEmail(currentUser)
            return  // Exit onCreate to prevent initializing the login buttons
        }

        val signInButton = findViewById<Button>(R.id.login)
        val signUpButton = findViewById<Button>(R.id.signup)

        // Set on click listeners for sign in and sign up buttons
        signInButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set on click listener for Google sign-in button
        val googleLoginLayout = findViewById<LinearLayout>(R.id.google_login)
        googleLoginLayout.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener() {
                signInWithGoogle()
            }
        }
    }

    // Start the Google sign-in flow
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Handle the result of the Google sign-in flow
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.signInIntent
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign-In failed
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Authenticate with Firebase using the Google ID token
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    if (user != null) {
                        // Log the photo URL
                        Log.d("MainActivity", "Google Profile Photo URL: ${user.photoUrl}")

                        saveGoogleUserToFirestore(user)
                        // Set profile image in UserDetailsActivity
                        val intent = Intent(this, UserDetailsActivity::class.java)
                        intent.putExtra("profileImageUrl", user.photoUrl.toString()) // Add the profile image URL to the intent
                        Log.d("MainActivity", "Google Profile Photo URL: ${user.photoUrl}")
                    } else {
                        Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Save Google user details to Firestore
    private fun saveGoogleUserToFirestore(user: FirebaseUser) {
        val userId = user.uid
        val username = user.displayName ?: "Unknown"
        val email = user.email ?: "Unknown"
        val photoUrl = user.photoUrl?.toString() ?: ""

        // Create a HashMap to store the user details
        val userMap = hashMapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "photoUrl" to photoUrl
        )

        // Save user details to Firestore
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "User details saved", Toast.LENGTH_SHORT).show()
                navigateBasedOnEmail(user)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Navigate to the appropriate activity based on user email
    private fun navigateBasedOnEmail(user: FirebaseUser) {
        val email = user.email ?: return

        when (email) {
            BuildConfig.DOCTOR_EMAIL -> {
                startActivity(Intent(this, ManageReportsActivity::class.java))
            }
            BuildConfig.STAFF_EMAIL -> {
                startActivity(Intent(this, ManageAppointmentsActivity::class.java))
            }
            else -> {
                startActivity(Intent(this, HomePageActivity::class.java))
            }
        }
        finish()
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}