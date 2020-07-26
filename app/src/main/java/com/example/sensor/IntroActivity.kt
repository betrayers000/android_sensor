package com.example.sensor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }
}
