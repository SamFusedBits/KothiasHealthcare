package com.example.ksharsutra

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.ksharsutra.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class User(
    var username: String = "",
    val email: String = "",
    var phone: String = "",
    var profileImageUrl: String = ""
)

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var phoneTextView: TextView
    private lateinit var phoneEditText: EditText
    private lateinit var emailTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var saveProfileButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: User? = null
    private var isEditMode = false
    private lateinit var profileImageUri: Uri // To store selected image URI
    private lateinit var profileImageView: ImageView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        profileImageView = findViewById(R.id.profile_image)  // Initialization

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        usernameTextView = findViewById(R.id.username_textview)
        usernameEditText = findViewById(R.id.username_edittext)
        phoneTextView = findViewById(R.id.phone_textview)
        phoneEditText = findViewById(R.id.phone_edittext)
        emailTextView = findViewById(R.id.email_textview)
        editProfileButton = findViewById(R.id.edit_profile_button)
        saveProfileButton = findViewById(R.id.save_profile_button)
        logoutButton = findViewById(R.id.logout_button)

        // Fetch and display user details
        fetchUserDetails()

        // Load cached profile image
        loadCachedProfileImage()

        // Handle edit profile button click
        editProfileButton.setOnClickListener {
            toggleEditMode()
        }

        // Handle save profile button click
        saveProfileButton.setOnClickListener {
            saveProfileChanges()
        }

        // Handle profile image click to open gallery
        profileImageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }

        logoutButton.setOnClickListener { // Temporary implementation
            startActivity(Intent(this, ManageAppointmentsActivity::class.java))
            finish()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                profileImageUri = it
                profileImageView.setImageURI(selectedImageUri)
                // Optionally, you can upload the image to Firebase Storage here
                uploadProfileImage(profileImageUri)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val user = mAuth.currentUser
        user?.let {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("profile_images/${user.uid}.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL from Firebase Storage
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Update Firestore with the new image URL
                        updateProfileImageUrl(uri.toString())
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to retrieve download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val user = mAuth.currentUser
        user?.let {
            // Update Firestore with new image URL
            val userRef = firestore.collection("users").document(user.uid)
            userRef.update("profileImageUrl", imageUrl)
                .addOnSuccessListener {
                    // Update local cached user object if necessary
                    currentUser?.profileImageUrl = imageUrl
                    Toast.makeText(this, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1011
        )
    }

    private fun fetchUserDetails() {
        val user = mAuth.currentUser
        user?.let {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        currentUser = document.toObject(User::class.java)
                        currentUser?.let {
                            // Set other fields (username, email, phone)
                            usernameTextView.text = it.username
                            usernameEditText.setText(it.username)
                            emailTextView.text = it.email
                            phoneTextView.text = it.phone
                            phoneEditText.setText(it.phone)

                            // Load and cache profile image URL
                            loadProfileImage(it.profileImageUrl)

                            // Cache profile image URL in SharedPreferences
                            saveProfileImageUrl(it.profileImageUrl)
                        }
                    } else {
                        Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileImageUrl(imageUrl: String) {
        val sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("profileImageUrl", imageUrl)
        editor.apply()
    }

    private fun loadProfileImage(imageUrl: String?) {
        imageUrl?.let {
            // Load image using Glide into profileImageView
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.toolbar_user_profile_logo) // Optional placeholder
                .error(R.drawable.toolbar_user_profile_logo) // Optional error image
                .into(profileImageView)
        }
    }

    private fun loadCachedProfileImage() {
        val sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val profileImageUrl = sharedPreferences.getString("profileImageUrl", null)
        profileImageUrl?.let {
            loadProfileImage(it)
        }
    }

    private fun toggleEditMode() {
        if (!isEditMode) {
            // Switch to edit mode
            usernameTextView.visibility = View.GONE
            usernameEditText.visibility = View.VISIBLE
            phoneTextView.visibility = View.GONE
            phoneEditText.visibility = View.VISIBLE
            editProfileButton.visibility = View.GONE
            saveProfileButton.visibility = View.VISIBLE

            isEditMode = true
        }
    }

    private fun saveProfileChanges() {
        val user = mAuth.currentUser
        user?.let {
            val newUsername = usernameEditText.text.toString().trim()
            val newPhone = phoneEditText.text.toString().trim()

            // Update Firestore with new data
            val userRef = firestore.collection("users").document(user.uid)
            userRef.update(mapOf(
                "username" to newUsername,
                "phone" to newPhone
            ))
                .addOnSuccessListener {
                    // Update currentUser with new data
                    currentUser?.username = newUsername
                    currentUser?.phone = newPhone

                    // Switch back to view mode
                    usernameTextView.text = newUsername
                    usernameTextView.visibility = View.VISIBLE
                    usernameEditText.visibility = View.GONE
                    phoneTextView.text = newPhone
                    phoneTextView.visibility = View.VISIBLE
                    phoneEditText.visibility = View.GONE
                    emailTextView.visibility = View.VISIBLE
                    editProfileButton.visibility = View.VISIBLE
                    saveProfileButton.visibility = View.GONE

                    isEditMode = false

                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update profile: $exception", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
