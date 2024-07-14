package com.example.ksharsutra

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.text.TextWatcher
import android.webkit.MimeTypeMap
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
        reportAdapter = ReportAdapter(reports) { fileUrl, mimeType -> openFile(Uri.parse(fileUrl), mimeType) }
        reportsRecyclerView.adapter = reportAdapter
        reportsRecyclerView.layoutManager = LinearLayoutManager(this)

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

        // Add TextWatcher to searchEditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                if (searchText.isEmpty()) {
                    fetchAllReports()
                } else {
                    searchReports(searchText)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
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

                // Log fetched reports
                Log.d("ManageReportsActivity", "Fetched ${reports.size} reports")
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch reports: ${exception.message}", Toast.LENGTH_SHORT).show()
                isLoadingReports = false
                // Hide the ProgressBar
                progressBar.visibility = View.GONE

                // Log the failure message for debugging
                Log.e("ManageReportsActivity", "Failed to fetch reports: ${exception.message}", exception)
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
                Log.d("ManageReportsActivity", "Successfully fetched documents: ${querySnapshot.size()}")
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val fileName = document.getString("name")
                        val patientName = document.getString("patientName")
                        val patientEmail = document.getString("patientEmail")
                        val fileUrl = document.getString("url")

                        if (fileName != null && fileUrl != null) {
                            val report = Report(fileName, patientName, patientEmail, fileUrl)
                            reports.add(report)
                            Log.d("ManageReportsActivity", "Report added: $fileName")
                        }
                    }

                    if (querySnapshot.size() > 0) {
                        lastVisibleDocument = querySnapshot.documents[querySnapshot.size() - 1]
                    }

                    reportAdapter.notifyDataSetChanged()
                } else {
                    Log.d("ManageReportsActivity", "No documents found.")
                }

                isLoadingReports = false
                // Hide the ProgressBar
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch reports: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("ManageReportsActivity", "Failed to fetch reports: ${exception.message}", exception)
                isLoadingReports = false
                // Hide the ProgressBar
                progressBar.visibility = View.GONE
            }
    }

    private fun searchReports(searchText: String) {
        val firestore = FirebaseFirestore.getInstance()
        isLoadingReports = true
        reports.clear()
        lastVisibleDocument = null

        // Show the ProgressBar
        progressBar.visibility = View.VISIBLE

        firestore.collection("uploads")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                reports.clear()
                for (document in querySnapshot.documents) {
                    val fileName = document.getString("name")
                    val patientName = document.getString("patientName")
                    val patientEmail = document.getString("patientEmail")
                    val fileUrl = document.getString("url")

                    if (fileName != null && patientName != null && fileUrl != null) {
                        // Check if the patientName contains the searchText (case insensitive)
                        if (patientName.contains(searchText, ignoreCase = true)) {
                            val report = Report(fileName, patientName, patientEmail, fileUrl)
                            reports.add(report)
                        }
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

    private fun openFileInGoogleDrive(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setPackage("com.google.android.apps.docs")

        try {
            val chooserIntent = Intent.createChooser(intent, "Open with")
            startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            // Google Drive app not found, fall back to default handling
            openFileInDefaultChooser(uri, mimeType)
        }
    }

    private fun openFileInDefaultChooser(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            val chooserIntent = Intent.createChooser(intent, "Open with")
            startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            // No app found to handle the file, open in browser
            openFileInBrowser(uri)
        }
    }

    private fun openFileInBrowser(uri: Uri) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        browserIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            // No browser found, show a toast or handle the situation accordingly
            Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFile(uri: Uri, mimeType: String?) {
        if (mimeType != null) {
            if (mimeType == "application/pdf") {
                openFileInGoogleDrive(uri, mimeType)
            } else {
                openFileInDefaultChooser(uri, mimeType)
            }
        } else {
            // Unsupported file type, open in browser
            openFileInBrowser(uri)
        }
    }

    fun onSearchButtonClick(view: View) {
        val searchText = searchEditText.text.toString().trim()
        if (searchText.isEmpty()) {
            fetchAllReports()
        } else {
            searchReports(searchText)
        }
    }
}