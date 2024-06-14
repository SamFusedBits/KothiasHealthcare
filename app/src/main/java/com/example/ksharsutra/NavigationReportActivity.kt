package com.example.ksharsutra

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class NavigationReportActivity : AppCompatActivity() {

    private lateinit var selectFileIcon: ImageView
    private lateinit var browseFileButton: Button
    private lateinit var selectFileText: TextView

    private val PICK_FILE_REQUEST = 1
    private val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_report)

        selectFileIcon = findViewById(R.id.select_file_icon)
        browseFileButton = findViewById(R.id.browse_file_button)
        selectFileText = findViewById(R.id.select_file_text)

        selectFileIcon.setOnClickListener {
            openFilePicker()
        }

        browseFileButton.setOnClickListener {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
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
                openFilePicker()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                uploadFileToFirebase(uri)
            }
        }
    }

    private fun uploadFileToFirebase(fileUri: Uri) {
        val fileName = getFileName(fileUri) ?: UUID.randomUUID().toString()
        val storageReference = FirebaseStorage.getInstance().reference.child("uploads/$fileName")
        val uploadTask = storageReference.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                saveFileMetadataToFirestore(uri.toString(), fileName)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFileMetadataToFirestore(downloadUrl: String, fileName: String) {
        val firestore = FirebaseFirestore.getInstance()
        val fileMetadata = mapOf(
            "name" to fileName,
            "url" to downloadUrl,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("uploads")
            .add(fileMetadata)
            .addOnSuccessListener {
                Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save file metadata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
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
}