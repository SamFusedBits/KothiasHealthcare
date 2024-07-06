package com.example.ksharsutra

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ManageReportsActivity : AppCompatActivity() {

    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var progressBar: ProgressBar
    private val reports = mutableListOf<Report>()

    // Pagination variables
    private var lastVisibleDocument: DocumentSnapshot? = null
    private var isLoadingReports = false
    private val PAGE_SIZE = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_patient_reports)

        // Initialize views
        reportsRecyclerView = findViewById(R.id.reports_recycler_view)
        searchEditText = findViewById(R.id.search_edit_text)
        progressBar = findViewById(R.id.progress_bar)
        val searchButton: Button = findViewById(R.id.search_button)

        // Setup RecyclerView
        reportAdapter = ReportAdapter(reports) { fileUrl -> openFile(fileUrl) }
        reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        reportsRecyclerView.adapter = reportAdapter

        // Fetch and display user's reports
        fetchAllReports()

        // Navigation to ManageAppointments Page
        val navigationAppointments = findViewById<ImageView>(R.id.navigation_appointments)
        navigationAppointments.setOnClickListener {
            val intent = Intent(this, ManageAppointmentsActivity::class.java)
            startActivity(intent)
        }

        // Set up scroll listener for pagination
        reportsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingReports && totalItemCount <= (lastVisibleItem + PAGE_SIZE / 2)) {
                    fetchMoreReports()
                }
            }
        })

        // Set up search button click listener
        searchButton.setOnClickListener {
            onSearchButtonClick(it)
        }
    }

    private fun fetchAllReports() {
        val firestore = FirebaseFirestore.getInstance()
        isLoadingReports = true
        reports.clear()
        lastVisibleDocument = null

        // Show the ProgressBar
        progressBar.visibility = View.VISIBLE

        var query = firestore.collection("uploads")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

        lastVisibleDocument?.let {
            query = query.startAfter(it)
        }

        query.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val fileName = document.getString("name")
                    val patientName = document.getString("patientName")
                    val patientEmail = document.getString("patientEmail")
                    val fileUrl = document.getString("url")

                    if (fileName != null && fileUrl != null) {
                        val report = Report(fileName, patientName, patientEmail, fileUrl)
                        reports.add(report)
                    }
                }

                if (querySnapshot.size() > 0) {
                    lastVisibleDocument = querySnapshot.documents[querySnapshot.size() - 1]
                }

                reportAdapter.notifyDataSetChanged()
                isLoadingReports = false
                // Hide the ProgressBar
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch reports: ${exception.message}", Toast.LENGTH_SHORT).show()
                isLoadingReports = false
                // Hide the ProgressBar
                progressBar.visibility = View.GONE
            }
    }

    private fun fetchMoreReports() {
        val firestore = FirebaseFirestore.getInstance()
        isLoadingReports = true

        // Show the ProgressBar
        progressBar.visibility = View.VISIBLE

        var query = firestore.collection("uploads")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

        lastVisibleDocument?.let {
            query = query.startAfter(it)
        }

        query.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val fileName = document.getString("name")
                    val patientName = document.getString("patientName")
                    val patientEmail = document.getString("patientEmail")
                    val fileUrl = document.getString("url")

                    if (fileName != null && fileUrl != null) {
                        val report = Report(fileName, patientName, patientEmail, fileUrl)
                        reports.add(report)
                    }
                }

                if (querySnapshot.size() > 0) {
                    lastVisibleDocument = querySnapshot.documents[querySnapshot.size() - 1]
                }

                reportAdapter.notifyDataSetChanged()
                isLoadingReports = false
                // Hide the ProgressBar
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch reports: ${exception.message}", Toast.LENGTH_SHORT).show()
                isLoadingReports = false
                // Hide the ProgressBar
                progressBar.visibility = View.GONE
            }
    }

    fun onSearchButtonClick(view: View) {
        val searchText = searchEditText.text.toString().trim()

        if (searchText.isEmpty()) {
            fetchAllReports()
        } else {
            val firestore = FirebaseFirestore.getInstance()

            // Show the ProgressBar
            progressBar.visibility = View.VISIBLE

            firestore.collection("uploads")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("normalizedPatientName", searchText.lowercase())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    reports.clear()
                    for (document in querySnapshot.documents) {
                        val fileName = document.getString("name")
                        val patientName = document.getString("patientName")
                        val patientEmail = document.getString("patientEmail")
                        val fileUrl = document.getString("url")

                        if (fileName != null && fileUrl != null) {
                            val report = Report(fileName, patientName, patientEmail, fileUrl)
                            reports.add(report)
                        }
                    }

                    reportAdapter.notifyDataSetChanged()
                    // Hide the ProgressBar
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to search reports: ${exception.message}", Toast.LENGTH_SHORT).show()
                    // Hide the ProgressBar
                    progressBar.visibility = View.GONE
                }
        }
    }

    private fun openFile(fileUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
        startActivity(intent)
    }
}
