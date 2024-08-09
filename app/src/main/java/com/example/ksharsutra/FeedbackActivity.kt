package com.example.ksharsutra

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var ratingBar: RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)

        // Set padding based on system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        nameEditText = findViewById(R.id.edit_text_name)
        emailEditText = findViewById(R.id.edit_text_email)
        phoneEditText = findViewById(R.id.edit_text_phone)
        messageEditText = findViewById(R.id.edit_text_message)
        ratingBar = findViewById(R.id.ratingBar)
        val submitButton = findViewById<Button>(R.id.submit)

        // Handle back arrow click to finish activity
        backArrow.setOnClickListener {
            finish()
        }

        // Handle submit button click
        submitButton.setOnClickListener {
            submitFeedback()
        }
    }

    // Submit feedback to Firestore
    private fun submitFeedback() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val message = messageEditText.text.toString().trim()
        val rating = ratingBar.rating.toInt()

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || message.isEmpty()) {
            // Handle empty fields or show error message
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a HashMap to store the feedback data
        val feedbackData = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "message" to message,
            "rating" to rating
        )

        // Access Firestore instance and add feedback to "user_feedback" collection
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("patient_feedback")
            .add(feedbackData)
            .addOnSuccessListener { documentReference ->
                // Handle successful feedback submission
                Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to submit feedback: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}