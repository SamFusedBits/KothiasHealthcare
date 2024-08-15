package com.kothias.clinic

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ContactUSActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge to edge display for the activity layout (Notch display)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contactus)

        val email = findViewById<TextView>(R.id.email)
        val phone = findViewById<TextView>(R.id.phoneno)
        val address = findViewById<TextView>(R.id.address)
        val appointment = findViewById<Button>(R.id.book_an_appointment)
        val back = findViewById<ImageView>(R.id.back_arrow)

        val titleTextView = findViewById<TextView>(R.id.title)

        // Add onclick listeners
        email.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${getString(R.string.emailID)}")
            }
            startActivity(emailIntent)
        }

        phone.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${getString(R.string.phoneno)}")
            }
            startActivity(dialIntent)
        }

        address.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=${Uri.encode(getString(R.string.address))}")
            }
            startActivity(mapIntent)
        }
        appointment.setOnClickListener {
            val intent = Intent(this, AppointmentActivity::class.java)
            startActivity(intent)
        }
        back.setOnClickListener {
            finish()
        }
    }
}