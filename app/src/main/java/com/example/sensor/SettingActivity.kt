package com.example.sensor

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.include_options.*

class SettingActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // 위험농도표시 스피너(드롭다운)사용

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

        // 알림소리설정 스피너 사용
        val items2 = resources.getStringArray(R.array.sound_lists)
        val soundAdatper = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items2)
        sound_spinner.adapter = soundAdatper
        sound_spinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {        }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {}

        }
    }

    // 앱바에 확인버튼 추가
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu2, menu)
        return true
    }




}