package com.example.ksharsutra

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrecationsTreatmentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_precationstreatments)

        val back_arrow = findViewById<ImageView>(R.id.back_arrow)

        val title = findViewById<TextView>(R.id.title)
        val precautions_list = findViewById<TextView>(R.id.precautions_list)
        val treatment_list = findViewById<TextView>(R.id.treatment_list)

        val disease = intent.getStringExtra("disease")

        if(disease == "piles") {
            title.text = "Piles"
            precautions_list.text = getString(R.string.piles_precautions)
            treatment_list.text = getString(R.string.piles_treatment)
        } else if(disease == "fissure") {
            title.text = "Fissure"
            precautions_list.text = getString(R.string.fissure_precautions)
            treatment_list.text = getString(R.string.fissure_treatment)
        } else if(disease == "pilonidalsinus") {
            title.text = "Pilonidal Sinus"
            precautions_list.text = getString(R.string.pilonidalsinus_precautions)
            treatment_list.text = getString(R.string.piles_treatment)
        } else if(disease == "fistula") {
            title.text = "Fistula"
            precautions_list.text = getString(R.string.fistula_precautions)
            treatment_list.text = getString(R.string.fistula_treatment)
        }
        back_arrow.setOnClickListener {
            finish()
        }

    }
}