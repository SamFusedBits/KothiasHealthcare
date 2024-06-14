package com.example.ksharsutra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HealthAdviceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_healthadvice)


        val learnmore_piles = findViewById<Button>(R.id.learnmore_piles)
        val learnmore_fissure = findViewById<Button>(R.id.learnmore_fissure)
        val learnmore_pilonidalsinus = findViewById<Button>(R.id.learnmore_pilonidalsinus)
        val learnmore_fistula = findViewById<Button>(R.id.learnmore_fistula)
        val back_arrow = findViewById<ImageView>(R.id.back_arrow)

        learnmore_piles.setOnClickListener {
            val intent = Intent(this, PrecationsTreatmentsActivity::class.java)
            intent.putExtra("disease", "piles")
            startActivity(intent)
        }
        learnmore_fissure.setOnClickListener {
            val intent = Intent(this, PrecationsTreatmentsActivity::class.java)
            intent.putExtra("disease", "fissure")
            startActivity(intent)
        }
        learnmore_pilonidalsinus.setOnClickListener {
            val intent = Intent(this, PrecationsTreatmentsActivity::class.java)
            intent.putExtra("disease", "pilonidalsinus")
            startActivity(intent)
        }
        learnmore_fistula.setOnClickListener {
            val intent = Intent(this, PrecationsTreatmentsActivity::class.java)
            intent.putExtra("disease", "fistula")
            startActivity(intent)
        }
        back_arrow.setOnClickListener {
            finish()
        }
    }
}