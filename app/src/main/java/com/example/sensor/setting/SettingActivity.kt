package com.example.sensor.setting

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sensor.App.Companion.prefs
import com.example.sensor.R
import com.example.sensor.main.Main2Activity
import kotlinx.android.synthetic.main.activity_setting2.*
import kotlinx.android.synthetic.main.include_options.sound_spinner

class SettingActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        prefs.danger.get()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting2)

        // 위험농도표시 스피너(드롭다운)사용

        // 만든 array가져오기
        val items = resources.getStringArray(R.array.density)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        //어댑터 연결하기
//        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
//        danger_spinner.adapter = myAdapter
//        danger_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {}
//        }

        // 기존에 저장한 값으로 불러오기(sharedPreference에서 값가져오기)
//        danger_spinner.setSelection(items.indexOf(prefs.danger.toString()))

        // 알림소리설정 스피너 사용
        val items2 = resources.getStringArray(R.array.sound_lists)
        val soundAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items2)
        sound_spinner.adapter = soundAdapter
        sound_spinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {        }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {}

        }
        // 기존에 설정한 값으로 불러오기
        sound_spinner.setSelection(items2.indexOf(prefs.sound))


        // 센서종류 스피너 사용
        val items3 = resources.getStringArray(R.array.sensor_lists)
        val sensorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items3)
        sensor_spinner.adapter = sensorAdapter
        sensor_spinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {}
        }
        // 기존에 설정한 값으로 불러오기
        sensor_spinner.setSelection(items3.indexOf(prefs.sensor))


//        sensor_btn1.setOnClickListener {
//            val intent = Intent(this, SensorDetailActivity::class.java)
//            intent.putExtra("sensor", resources.getString(R.string.o2))
//            startActivity(intent)
//        }
//
//        sensor_btn2.setOnClickListener {
//            val intent = Intent(this, SensorDetailActivity::class.java)
//            intent.putExtra("sensor", resources.getString(R.string.co2))
//            startActivity(intent)
//        }
//
//        sensor_btn3.setOnClickListener {
//            val intent = Intent(this, SensorDetailActivity::class.java)
//            intent.putExtra("sensor", resources.getString(R.string.co))
//            startActivity(intent)
//        }
//
//        sensor_btn4.setOnClickListener {
//            val intent = Intent(this, SensorDetailActivity::class.java)
//            intent.putExtra("sensor", resources.getString(R.string.no2))
//            startActivity(intent)
//        }
//
//        sensor_btn5.setOnClickListener {
//            val intent = Intent(this, SensorDetailActivity::class.java)
//            intent.putExtra("sensor", resources.getString(R.string.so2))
//            startActivity(intent)
//        }
//
//        sensor_btn6.setOnClickListener {
//            val intent = Intent(this, SensorDetailActivity::class.java)
//            intent.putExtra("sensor", resources.getString(R.string.h2s))
//            startActivity(intent)
//        }
    }


    // 앱바에 확인버튼 추가
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu2, menu)
        return true
    }

    // 확인버튼에 이벤트 넣기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.confirm_btn ->{
                val confirmIntent = Intent(this, Main2Activity::class.java)

                // 액티비티가 전환되기전 sharedPreference에 값저장하기
//                val danger_data = danger_spinner.selectedItem.toString()
//                prefs.danger = danger_data.toFloat()
                prefs.sound = sound_spinner.selectedItem.toString()
                prefs.sensor = sensor_spinner.selectedItem.toString()
//                prefs.change_switch = change_switch.isChecked

                startActivity(confirmIntent)
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }




}