package com.example.ksharsutra

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import com.google.firebase.firestore.Query

class NavigationReportActivity : AppCompatActivity() {

    private lateinit var selectFileIcon: ImageView
    private lateinit var browseFileButton: Button
    private lateinit var selectFileText: TextView
    private lateinit var filesContainer: LinearLayout
    private lateinit var progressBar: ProgressBar

    private val PICK_FILE_REQUEST = 1
    private val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 100

    // Cache for storing file metadata
    private val fileCache = mutableListOf<FileMetadata>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_report)

        // Initialize views
        selectFileIcon = findViewById(R.id.select_file_icon)
        browseFileButton = findViewById(R.id.browse_file_button)
        selectFileText = findViewById(R.id.select_file_text)
        filesContainer = findViewById(R.id.files_container)
        progressBar = findViewById(R.id.progress_bar)

        val navigation_profile = findViewById<ImageView>(R.id.navigation_profile)
        val navigation_home = findViewById<ImageView>(R.id.navigation_home)
        val navigation_appointment = findViewById<ImageView>(R.id.navigation_appointment)

        navigation_profile.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            startActivity(intent)
        }

        navigation_home.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }

        navigation_appointment.setOnClickListener {
            val intent = Intent(this, ManageAppointmentsActivity::class.java)
            startActivity(intent)
        }

        // Set listeners
        selectFileIcon.setOnClickListener {
            Log.d(TAG, "Select file icon clicked")
            openFilePicker()
        }

        browseFileButton.setOnClickListener {
            Log.d(TAG, "Browse file button clicked")
            openFilePicker()
        }

        // Fetch and display last 3 uploaded files from fileCache
        displayLast3UploadedFiles()

        // Optional: Fetch files from Firestore initially if needed
        fetchUserFiles()

    }

    private fun fetchUserFiles() {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { uid ->
            firestore.collection("uploads")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    fileCache.clear()  // Clear the cache before adding new data
                    for (document in querySnapshot.documents) {
                        val fileName = document.getString("name")
                        val fileUrl = document.getString("url")
                        val timestamp = document.getLong("timestamp") ?: 0

                        if (fileName != null && fileUrl != null) {
                            val fileMetadata = FileMetadata(fileName, fileUrl, timestamp)
                            fileCache.add(fileMetadata)
                        }
                    }

                    // Display last 3 uploaded files
                    displayLast3UploadedFiles()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch user files: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun displayLast3UploadedFiles() {
        // Clear existing views in filesContainer
        filesContainer.removeAllViews()

        // Sort the cache by timestamp in descending order
        val sortedCache = fileCache.sortedByDescending { it.timestamp }

        // Display the last 3 files from sortedCache
        val last3Files = sortedCache.take(3)
        last3Files.forEach { fileMetadata ->
            addFileToUI(fileMetadata.name, fileMetadata.url, fileMetadata.timestamp)
        }
    }

    private fun openFilePicker() {
        Log.d(TAG, "Opening file picker")
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, PICK_FILE_REQUEST)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                Log.d(TAG, "Permission granted")
                openFilePicker()
            } else {
                Log.d(TAG, "Permission denied")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                Log.d(TAG, "File selected: $uri")
                // Prompt user for additional details before uploading file
                fetchUserDetailsAndUpload(uri)
            }
        }else{
            Log.d(TAG, "No file selected")
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserDetailsAndUpload(fileUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val name = it.displayName ?: "Unknown"
            val email = it.email ?: "Unknown"
            val phoneNumber = it.phoneNumber ?: "Unknown"

            uploadFileToFirebase(fileUri, name, email, phoneNumber)
        }
    }

    private fun uploadFileToFirebase(fileUri: Uri, name: String, email: String, phoneNumber: String) {
        Log.d(TAG, "Uploading file to Firebase Storage")
        val fileName = getFileName(fileUri) ?: UUID.randomUUID().toString()
        val storageReference = FirebaseStorage.getInstance().reference.child("uploads/$fileName")
        val uploadTask = storageReference.putFile(fileUri)

        // Show progress bar
        progressBar.visibility = ProgressBar.VISIBLE
        progressBar.progress = 0

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            progressBar.progress = progress
        }.addOnSuccessListener { taskSnapshot ->
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                val timestamp = System.currentTimeMillis()

                // Show file immediately in UI
                addFileToUI(fileName, downloadUrl, timestamp)

                // Add to cache
                fileCache.add(FileMetadata(fileName, downloadUrl, timestamp))

                // Save metadata to Firestore in background
                saveFileMetadataToFirestore(fileName, downloadUrl, name, email, phoneNumber)

                // Hide progress bar
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to retrieve file URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                // Hide progress bar if retrieving the URL fails
                progressBar.visibility = ProgressBar.GONE
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            // Hide progress bar
            progressBar.visibility = ProgressBar.GONE
        }
    }

    private fun saveFileMetadataToFirestore(fileName: String, downloadUrl: String, name: String, email: String, phoneNumber: String) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { uid ->
            val fileMetadata = hashMapOf(
                "name" to fileName,
                "url" to downloadUrl,
                "timestamp" to System.currentTimeMillis(),
                "userId" to uid,
                "patientName" to name,
                "patientEmail" to email,
                "patientPhoneNumber" to phoneNumber
            )

            firestore.collection("uploads")
                .add(fileMetadata)
                .addOnSuccessListener {
                    progressBar.visibility = ProgressBar.GONE
                    Log.d(TAG, "File metadata saved to Firestore")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload file: ${it.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = ProgressBar.GONE
                }
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            progressBar.visibility = ProgressBar.GONE
        }
    }

    private fun addFileToUI(fileName: String, fileUrl: String, timestamp: Long) {
        val fileView = layoutInflater.inflate(R.layout.item_uploaded_file, null)
        val fileNameTextView = fileView.findViewById<TextView>(R.id.file_name_text_view)
        val viewFileButton = fileView.findViewById<Button>(R.id.view_file_button)

        fileNameTextView.text = fileName
        viewFileButton.setOnClickListener {
            openFileInBrowser(fileUrl)
        }

        // Check if filesContainer already has 3 child views
        if (filesContainer.childCount >= 3) {
            filesContainer.removeViewAt(0) // Remove the oldest file view if more than 3
        }

        filesContainer.addView(fileView)
    }

    private fun openFileInBrowser(fileUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(fileUrl)
        startActivity(intent)
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            result?.let {
                val cut = it.lastIndexOf('/')
                if (cut != -1) {
                    result = it.substring(cut + 1)
                }
            }
        }
        return result
    }

    data class FileMetadata(val name: String, val url: String, val timestamp: Long)
}