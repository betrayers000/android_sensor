package com.example.sensor.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sensor.main.Main2Activity
import com.example.sensor.R

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val mainIntent = Intent(this, Main2Activity::class.java)
        startActivity(mainIntent)
        finish()
    }
}
