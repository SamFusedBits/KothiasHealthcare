package com.example.ksharsutra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        val doctorText = findViewById<TextView>(R.id.hello_doctor_text)

        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.search_logo, null)
        val scaledDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap((drawable as BitmapDrawable).bitmap, 40, 40, true))

        val search_bar = findViewById<EditText>(R.id.search_bar)
        search_bar.setCompoundDrawablesWithIntrinsicBounds(scaledDrawable, null, null, null)

        val download_button1 = findViewById<Button>(R.id.download_button1)
        val download_button2 = findViewById<Button>(R.id.download_button2)
        val download_button3 = findViewById<Button>(R.id.download_button3)

        doctorText.text = "${doctorText.text} $username!!!"

        download_button1.setOnClickListener {
            Toast.makeText(this, "Downloading Patient 1 Data", Toast.LENGTH_SHORT).show()
        }
        download_button2.setOnClickListener {
            Toast.makeText(this, "Downloading Patient 2 Data", Toast.LENGTH_SHORT).show()
        }
        download_button3.setOnClickListener {
            Toast.makeText(this, "Downloading Patient 3 Data", Toast.LENGTH_SHORT).show()
        }

        // Add TextWatcher to search_bar
        search_bar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterPatients(s.toString())
            }
        })
    }

    private fun filterPatients(query: String) {
        val patient1 = findViewById<CardView>(R.id.patient1)
        val patient2 = findViewById<CardView>(R.id.patient2)
        val patient3 = findViewById<CardView>(R.id.patient3)

        val name1 = findViewById<TextView>(R.id.name1).text.toString()
        val name2 = findViewById<TextView>(R.id.name2).text.toString()
        val name3 = findViewById<TextView>(R.id.name3).text.toString()

        if (name1.contains(query, ignoreCase = true)) {
            patient1.visibility = View.VISIBLE
        } else {
            patient1.visibility = View.GONE
        }

        if (name2.contains(query, ignoreCase = true)) {
            patient2.visibility = View.VISIBLE
        } else {
            patient2.visibility = View.GONE
        }

        if (name3.contains(query, ignoreCase = true)) {
            patient3.visibility = View.VISIBLE
        } else {
            patient3.visibility = View.GONE
        }
    }
}
