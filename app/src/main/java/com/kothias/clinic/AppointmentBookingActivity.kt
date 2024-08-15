package com.kothias.clinic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentBookingActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvSelectedSchedule: TextView
    private lateinit var bookAppointmentButton: Button

    // Selected schedule from the previous activity
    private var selectedSchedule: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_booking)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        tvSelectedSchedule = findViewById(R.id.tvSelectedSchedule)
        bookAppointmentButton = findViewById(R.id.bookAppointmentButton)

        // Retrieve selected schedule from intent
        selectedSchedule = intent.getStringExtra("selected_schedule") ?: ""

        // Display selected schedule
        tvSelectedSchedule.text = "Selected Schedule: $selectedSchedule"

        // Handle book appointment button click
        bookAppointmentButton.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (validateInputs(name, email, phone)) {
                // Save appointment details to Firestore
                saveAppointment(name, email, phone, selectedSchedule)
            }
        }
    }

    // Validate user inputs
    private fun validateInputs(name: String, email: String, phone: String): Boolean {
        // Validate name
        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return false
        }

        // Validate email or phone number
        if (email.isEmpty() && phone.isEmpty()) {
            etEmail.error = "Email or Phone Number is required"
            etPhone.error = "Email or Phone Number is required"
            etEmail.requestFocus()
            return false
        }

        return true
    }

    // Save appointment details to Firestore
    private fun saveAppointment(name: String, email: String, phone: String, schedule: String) {
        val db = FirebaseFirestore.getInstance()

        // Create a new appointment
        val appointment = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "schedule" to schedule
        )

        // Add a new document with a generated ID
        db.collection("appointments")
            .add(appointment)
            .addOnSuccessListener {
                Toast.makeText(this, "Confirmation email will be sent to you shortly, based on availability.", Toast.LENGTH_SHORT).show()
                // Send confirmation email
                sendConfirmationEmail(email, name)

                startActivity(Intent(this, HomePageActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error booking appointment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Send confirmation email to the user
    private fun sendConfirmationEmail(userEmail: String, userName: String) {
        val subject = "Appointment Confirmation"
        val content = """
        <html>
            <body>
                <p>Dear $userName,</p>
                <p>Thank you for choosing Dr. Kothia's Clinic.</p>
                <p>We have received your appointment request and are currently processing it. You will receive a confirmation email shortly with the details of your appointment once it has been scheduled based on availability.</p>
                <p>If you have any questions or need further assistance, please feel free to contact us.</p>
                <p>Thank you for your patience and understanding.</p>
                <p>Best regards,</p>
                <p><strong>Kothia's Clinic Team</strong></p>
            </body>
        </html>
    """.trimIndent()

        SendinblueHelper.sendEmail(userEmail, subject, content) { success, error ->
            if (!success) {
                Log.d("AppointmentBookingActivity", "Failed to send confirmation email: $error")
            }
        }
    }
}