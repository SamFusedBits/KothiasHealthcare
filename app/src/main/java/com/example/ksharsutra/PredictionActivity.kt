package com.example.ksharsutra

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PredictionActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        val back_arrow = findViewById<ImageView>(R.id.back_arrow)

        back_arrow.setOnClickListener {
            finish()
        }
    }
}