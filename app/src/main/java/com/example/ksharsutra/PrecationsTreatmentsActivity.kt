package com.example.ksharsutra

import android.os.Bundle
import android.text.Html
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

        when (disease) {
            "piles" -> {
                title.text = "Piles"
                val styledPrecationsText = getString(R.string.piles_precautions)
                precautions_list.text = Html.fromHtml(styledPrecationsText, Html.FROM_HTML_MODE_LEGACY)
                val styledTreatmentText = getString(R.string.piles_treatment)
                treatment_list.text = Html.fromHtml(styledTreatmentText, Html.FROM_HTML_MODE_LEGACY)
            }
            "fissure" -> {
                title.text = "Fissure"
                val styledPrecationsText = getString(R.string.fissure_precautions)
                precautions_list.text = Html.fromHtml(styledPrecationsText, Html.FROM_HTML_MODE_LEGACY)
                val styledTreatmentText = getString(R.string.fissure_treatment)
                treatment_list.text = Html.fromHtml(styledTreatmentText, Html.FROM_HTML_MODE_LEGACY)
            }
            "pilonidalsinus" -> {
                title.text = "Pilonidal Sinus"
                val styledPrecationsText = getString(R.string.pilonidalsinus_precautions)
                precautions_list.text = Html.fromHtml(styledPrecationsText, Html.FROM_HTML_MODE_LEGACY)
                val styledTreatmentText = getString(R.string.pilonidalsinus_treatment)
                treatment_list.text = Html.fromHtml(styledTreatmentText, Html.FROM_HTML_MODE_LEGACY)
            }
            "fistula" -> {
                title.text = "Fistula"
                val styledPrecationsText = getString(R.string.fistula_precautions)
                precautions_list.text = Html.fromHtml(styledPrecationsText, Html.FROM_HTML_MODE_LEGACY)
                val styledTreatmentText = getString(R.string.fistula_treatment)
                treatment_list.text = Html.fromHtml(styledTreatmentText, Html.FROM_HTML_MODE_LEGACY)
            }
        }
        back_arrow.setOnClickListener {
            finish()
        }

    }
}