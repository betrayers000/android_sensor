package com.example.sensor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SensorDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_detail)

        // 만든 array가져오기
        val items = resources.getStringArray(R.array.density)

        //어댑터 연결하기
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        danger_spinner.adapter = myAdapter
        danger_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {}
        }

        // 기존에 저장한 값으로 불러오기(sharedPreference에서 값가져오기)
        danger_spinner.setSelection(items.indexOf(prefs.danger.toString()))
    }
}
