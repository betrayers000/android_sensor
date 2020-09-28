package com.example.sensor.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.sensor.App.Companion.prefs
import com.example.sensor.R
import com.example.sensor.setting.SettingActivity
import com.suke.widget.SwitchButton
import kotlinx.android.synthetic.main.activity_sensor_detail.*

class SensorDetailActivity : AppCompatActivity() {

    lateinit var sensorCategory : String

    private fun setSensor(minVal : Float, maxVal : Float){
        when(sensorCategory){
            resources.getString(R.string.o2) -> {
                prefs.max_o2 = maxVal
                prefs.min_o2 = minVal
                prefs.max_switch_o2 = sensor_max_switch.isChecked
                prefs.min_switch_o2 = sensor_min_switch.isChecked
            }
            resources.getString(R.string.co2) -> {
                prefs.max_co2 = maxVal
                prefs.min_co2 = minVal
                prefs.max_switch_co2 = sensor_max_switch.isChecked
                prefs.min_switch_co2 = sensor_min_switch.isChecked
            }
            resources.getString(R.string.co) -> {
                prefs.max_co = maxVal
                prefs.min_co = minVal
                prefs.max_switch_co = sensor_max_switch.isChecked
                prefs.min_switch_co = sensor_min_switch.isChecked
            }
            resources.getString(R.string.no2) -> {
                prefs.max_no2 = maxVal
                prefs.min_no2 = minVal
                prefs.max_switch_no2 = sensor_max_switch.isChecked
                prefs.min_switch_no2 = sensor_min_switch.isChecked
            }
            resources.getString(R.string.so2) -> {
                prefs.max_so2 = maxVal
                prefs.min_so2 = minVal
                prefs.max_switch_so2 = sensor_max_switch.isChecked
                prefs.min_switch_so2 = sensor_min_switch.isChecked
            }
            resources.getString(R.string.h2s) -> {
                prefs.max_h2s = maxVal
                prefs.min_h2s = minVal
                prefs.max_switch_h2s = sensor_max_switch.isChecked
                prefs.min_switch_h2s = sensor_min_switch.isChecked
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_detail)

        sensorCategory = intent.getStringExtra("sensor")!!

//        // 만든 array가져오기
        val items = resources.getStringArray(R.array.density)
//
        //어댑터 연결하기
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        max_spinner.adapter = myAdapter
        max_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sensor_max_msg.text = myAdapter.getItem(position) + "% 초과시 알람이 울립니다."
            }
        }

        min_spinner.adapter = myAdapter
        min_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sensor_min_msg.text = myAdapter.getItem(position) + "% 미만시 알람이 울립니다."
            }
        }

        when(sensorCategory){
            resources.getString(R.string.o2) -> {
                max_spinner.setSelection(items.indexOf(prefs.max_o2.toString()))
                min_spinner.setSelection(items.indexOf(prefs.min_o2.toString()))
                setBaseSwitch(prefs.min_switch_o2, prefs.max_switch_o2, prefs.max_o2, prefs.min_o2)
            }
            resources.getString(R.string.co2) -> {
                max_spinner.setSelection(items.indexOf(prefs.max_co2.toString()))
                min_spinner.setSelection(items.indexOf(prefs.min_co2.toString()))
                setBaseSwitch(prefs.min_switch_co2, prefs.max_switch_co2, prefs.max_co2, prefs.min_co2)
            }
            resources.getString(R.string.co) -> {
                max_spinner.setSelection(items.indexOf(prefs.max_co.toString()))
                min_spinner.setSelection(items.indexOf(prefs.min_co.toString()))
                setBaseSwitch(prefs.min_switch_co, prefs.max_switch_co, prefs.max_co, prefs.min_co)
            }
            resources.getString(R.string.no2) -> {
                max_spinner.setSelection(items.indexOf(prefs.max_no2.toString()))
                min_spinner.setSelection(items.indexOf(prefs.min_no2.toString()))
                setBaseSwitch(prefs.min_switch_no2, prefs.max_switch_no2, prefs.max_no2, prefs.min_no2)
            }
            resources.getString(R.string.so2) -> {
                max_spinner.setSelection(items.indexOf(prefs.max_so2.toString()))
                min_spinner.setSelection(items.indexOf(prefs.min_so2.toString()))
                setBaseSwitch(prefs.min_switch_so2, prefs.max_switch_so2, prefs.max_so2, prefs.min_so2)
            }
            resources.getString(R.string.h2s) -> {
                max_spinner.setSelection(items.indexOf(prefs.max_h2s.toString()))
                min_spinner.setSelection(items.indexOf(prefs.min_h2s.toString()))
                setBaseSwitch(prefs.min_switch_h2s, prefs.max_switch_h2s, prefs.max_h2s, prefs.min_h2s)
            }
        }

        // 실시간으로 변동주기
        sensor_min_switch.setOnCheckedChangeListener(SwitchButton.OnCheckedChangeListener { view, isChecked ->
            if (isChecked){
                min_spinner.visibility = View.VISIBLE
                sensor_min_msg.visibility = View.VISIBLE
            } else {
                min_spinner.visibility = View.GONE
                sensor_min_msg.visibility = View.GONE
            }
        })

        sensor_max_switch.setOnCheckedChangeListener(SwitchButton.OnCheckedChangeListener { view, isChecked ->
            if (isChecked){
                max_spinner.visibility = View.VISIBLE
                sensor_max_msg.visibility = View.VISIBLE
            } else {
                max_spinner.visibility = View.GONE
                sensor_max_msg.visibility = View.GONE
            }
        })


    }

    // switch 기본설정
    private fun setBaseSwitch(min : Boolean, max: Boolean, minVal: Float, maxVal: Float){
        sensor_min_switch.isChecked = min
        sensor_max_msg.text = maxVal.toString() + "% 초과시 알람이 울립니다."
        sensor_min_msg.text = minVal.toString() + "% 미만시 알람이 울립니다."
        if (sensor_min_switch.isChecked){
            min_spinner.visibility = View.VISIBLE
            sensor_min_msg.visibility = View.VISIBLE
        }else{
            min_spinner.visibility = View.GONE
            sensor_min_msg.visibility = View.GONE
        }

        sensor_max_switch.isChecked = max
        if (sensor_max_switch.isChecked){
            max_spinner.visibility = View.VISIBLE
            sensor_max_msg.visibility = View.VISIBLE
        }else{
            max_spinner.visibility = View.GONE
            sensor_max_msg.visibility = View.GONE
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
                val confirmIntent = Intent(this, SettingActivity::class.java)
                setSensor(min_spinner.selectedItem.toString().toFloat(), max_spinner.selectedItem.toString().toFloat())
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
