package com.example.ksharsutra

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class signUp : AppCompatActivity() {
    lateinit var signup : Button;
    lateinit var login : Button;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signup = findViewById(R.id.signup);
        login = findViewById(R.id.signin);

    }


}