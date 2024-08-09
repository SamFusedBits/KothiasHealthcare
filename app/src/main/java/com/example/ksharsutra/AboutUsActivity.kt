package com.example.ksharsutra

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AboutUsActivity : AppCompatActivity(){
    private lateinit var videoContainer: FrameLayout
    private lateinit var videoView: VideoView
    private lateinit var title: TextView
    private val firestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        // Initialize the views
        videoContainer = findViewById(R.id.video_container)
        videoView = findViewById(R.id.video_view)
        title = findViewById(R.id.title)
        val backArrow: ImageView = findViewById(R.id.back_arrow)

        // Load the latest video URL from Firebase
        loadVideoFromFirebase()

        val reachUsButton: Button = findViewById(R.id.reach_us_button)
        reachUsButton.setOnClickListener {
            // Open the map with the address
            val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=${Uri.encode(getString(R.string.address))}")
            }
            startActivity(mapIntent)
        }

        backArrow.setOnClickListener {
            finish()
        }
    }

    // Load the latest video URL from Firebase
    private fun loadVideoFromFirebase() {
        firestore.collection("videos")
            // Order the videos by timestamp in descending order
            .orderBy("timestamp",Query.Direction.DESCENDING)
            .limit(1)
            .get()
            // Get the latest video URL
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    // Get the latest video URL
                    val videoUrl = result.documents[0].getString("url") ?: ""
                    if (videoUrl.isNotEmpty()) {
                        // Show the video container and video view
                        videoContainer.visibility = View.VISIBLE
                        videoView.visibility = View.VISIBLE
                        videoView.setVideoURI(Uri.parse(videoUrl))
                        videoView.start()
                    } else {
                        // Hide the video container and video view
                        videoContainer.visibility = View.GONE
                        videoView.visibility = View.GONE
                    }
                } else {
                    // Hide the video container and video view
                    videoContainer.visibility = View.GONE
                    videoView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AboutUsActivity", "Error getting video URL", exception)
                videoContainer.visibility = View.GONE
                videoView.visibility = View.GONE
            }
    }
}