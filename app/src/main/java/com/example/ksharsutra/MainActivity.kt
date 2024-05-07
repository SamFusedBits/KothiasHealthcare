package com.example.ksharsutra

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() , View.OnClickListener {
    lateinit var signup : Button;
    lateinit var login : Button;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signup = findViewById(R.id.signup);
        login = findViewById(R.id.signin);

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.signup -> {
                //signup
            }
            R.id.signin -> {
                //login
            }
        }
    }
}