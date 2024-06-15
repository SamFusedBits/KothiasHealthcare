package com.example.ksharsutra
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ManageReportsActivity : AppCompatActivity() {

    private lateinit var reportsContainer: LinearLayout
    private lateinit var searchEditText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_patient_reports)

        // Initialize views
        reportsContainer = findViewById(R.id.reports_container)
        searchEditText = findViewById(R.id.search_edit_text)

        // Fetch and display user's reports
        fetchAllReports()

        // Navigation to ManageAppointments Page
        val navigation_appointments = findViewById<ImageView>(R.id.navigation_appointments)
        navigation_appointments.setOnClickListener{
            val intent = Intent(this, ManageAppointmentsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchAllReports() {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("uploads")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp descending
            .get()
            .addOnSuccessListener { querySnapshot ->
                reportsContainer.removeAllViews() // Clear previous views

                for (document in querySnapshot.documents) {
                    val fileName = document.getString("name")
                    val patientName = document.getString("patientName")
                    val patientEmail = document.getString("patientEmail")
                    val fileUrl = document.getString("url")

                    if (fileName != null && fileUrl != null) {
                        addReportToUI(fileName, patientName, patientEmail, fileUrl)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch reports: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun onSearchButtonClick(view: android.view.View) {
        val searchText = searchEditText.text.toString().trim()

        if (searchText.isEmpty()) {
            // If search text is empty, fetch all reports
            fetchAllReports()
        } else {
            // Perform search based on patientName or patientEmail
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("uploads")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp descending
                .whereEqualTo("normalizedPatientName", searchText.lowercase()) // Use normalized field for case-insensitive search
                .get()
                .addOnSuccessListener { querySnapshot ->
                    reportsContainer.removeAllViews() // Clear previous views

                    for (document in querySnapshot.documents) {
                        val fileName = document.getString("name")
                        val patientName = document.getString("patientName")
                        val patientEmail = document.getString("patientEmail")
                        val fileUrl = document.getString("url")

                        if (fileName != null && fileUrl != null) {
                            addReportToUI(fileName, patientName, patientEmail, fileUrl)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch reports: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addReportToUI(fileName: String, patientName: String?, patientEmail: String?, fileUrl: String) {
        val reportView = layoutInflater.inflate(R.layout.item_manage_report, null)
        val fileNameTextView = reportView.findViewById<TextView>(R.id.file_name_text_view)
        val usernameTextView = reportView.findViewById<TextView>(R.id.username_text_view)
        val emailTextView = reportView.findViewById<TextView>(R.id.email_text_view)
        val viewButton = reportView.findViewById<Button>(R.id.view_button)

        fileNameTextView.text = "File Name: $fileName"
        usernameTextView.text = "Patient Name: ${patientName ?: "Unknown"}"
        emailTextView.text = "Patient Email: ${patientEmail ?: "Unknown"}"

        // Set click listener for the View button
        viewButton.setOnClickListener {
            // Open file URL in browser or appropriate app
            openFile(fileUrl)
        }

        reportsContainer.addView(reportView)

        // Update Firestore with normalizedPatientName
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { uid ->
            val normalizedPatientName = patientName?.lowercase() ?: ""
            val docRef = firestore.collection("uploads").document()

            docRef.set(
                mapOf(
                    "name" to fileName,
                    "patientName" to patientName,
                    "normalizedPatientName" to normalizedPatientName, // Store normalized name
                    "patientEmail" to patientEmail,
                    "url" to fileUrl,
                    "timestamp" to System.currentTimeMillis(),
                    "userId" to uid
                )
            )
                .addOnSuccessListener {
                    Log.d("ManageReportActivity","Report added successfully")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add report: ${e.message}",Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openFile(fileUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(fileUrl)
        startActivity(intent)
    }
}
