package com.example.sensor

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.sensor.App.Companion.prefs
import kotlinx.android.synthetic.main.activity_setting2.*
import kotlinx.android.synthetic.main.include_options.*
import kotlinx.android.synthetic.main.include_options.sound_spinner

class SettingActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        prefs.danger.get()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting2)

        // 위험농도표시 스피너(드롭다운)사용

        // 만든 array가져오기
        val items = resources.getStringArray(R.array.density)

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

        // 디테일페이지시험
        sensor_btn_detail.setOnClickListener {
            startActivity(Intent(this, SensorDetailActivity::class.java))
        }
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
                val confirmIntent = Intent(this, MainActivity::class.java)

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