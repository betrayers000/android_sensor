package com.example.sensor

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private val MAX_OPER = "> "
    private val MIN_OPER = "< "
    private val context = this
    lateinit var manager : UsbManager
    var connect = false
    var loopChk = true
    val danger = App.prefs.danger
    val thread = ThreadClass()
    var minVal : Float? = null
    var maxVal : Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // actionbar 색 변경
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.customBlack)))
            actionBar.title = App.prefs.sensor
        }

        main_title.text = App.prefs.sensor
        manager = getSystemService(Context.USB_SERVICE) as UsbManager

        result_viewer.movementMethod = ScrollingMovementMethod()

        // meter set
//        meter.setLabelConverter(object : SpeedometerGauge.LabelConverter {
//            override fun getLabelFor(
//                progress: Double,
//                maxProgress: Double
//            ): String? {
//                return Math.round(progress).toString()
//            }
//        })
//
//        meter.setMaxSpeed(300.0)
//        meter.setMajorTickStep(30.0)
//        meter.setMinorTicks(2)
//        meter.addColoredRange(0.0, 60.0, Color.GREEN)
//        meter.addColoredRange(60.0, 180.0, Color.RED)
//
//
//        main_sound_off_btn.setOnClickListener{
//            meter.setSpeed(180.0, true)
//            meter.setUnitsText("500")
//        }


        val filter = IntentFilter(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter)

        val deviceList : HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { d ->
            val permissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            manager.requestPermission(d, permissionIntent)
        }


//        main_measure_btn.setOnClickListener{
//            if (connect){
//                loopChk = true
//                thread.start()
//            }
//        }
//
//        main_sound_off_btn.setOnClickListener {
//            ringOff()
//        }
        measure_toggle_btn.setOnCheckedChangeListener { buttonView, isChecked ->

            if(!connect){
                measure_toggle_btn.isChecked = false
            } else {
                if(isChecked){
                    loopChk = true
                    thread.start()
                } else {
                    ringOff()
                }
            }
        }

        if (App.prefs.sensor != resources.getStringArray(R.array.sensor_lists)[0]){
            result_viewer_tmp.visibility = View.GONE
        }
        when(App.prefs.sensor){
            resources.getStringArray(R.array.sensor_lists)[0] -> {
                result_viewer_min.text = MIN_OPER + App.prefs.min_o2.toString()
                result_viewer_max.text = MAX_OPER + App.prefs.max_o2.toString()
                minVal = App.prefs.min_o2
                maxVal = App.prefs.max_o2
            }
            resources.getStringArray(R.array.sensor_lists)[1] -> {
                result_viewer_min.text = MIN_OPER + App.prefs.min_co2.toString()
                result_viewer_max.text = MAX_OPER + App.prefs.max_co2.toString()
                minVal = App.prefs.min_co2
                maxVal = App.prefs.max_co2
            }
            resources.getStringArray(R.array.sensor_lists)[2] -> {
                result_viewer_min.text = MIN_OPER + App.prefs.min_co.toString()
                result_viewer_max.text = MAX_OPER + App.prefs.max_co.toString()
                minVal = App.prefs.min_co
                maxVal = App.prefs.max_co

            }
            resources.getStringArray(R.array.sensor_lists)[3] -> {
                result_viewer_min.text = MIN_OPER + App.prefs.min_no2.toString()
                result_viewer_max.text = MAX_OPER + App.prefs.max_no2.toString()
                minVal = App.prefs.min_no2
                maxVal = App.prefs.max_no2

            }
            resources.getStringArray(R.array.sensor_lists)[4] -> {
                result_viewer_min.text = MIN_OPER + App.prefs.min_so2.toString()
                result_viewer_max.text = MAX_OPER + App.prefs.max_so2.toString()
                minVal = App.prefs.min_so2
                maxVal = App.prefs.max_so2

            }
            resources.getStringArray(R.array.sensor_lists)[5] -> {
                result_viewer_min.text = MIN_OPER + App.prefs.min_h2s.toString()
                result_viewer_max.text = MAX_OPER + App.prefs.max_h2s.toString()
                minVal = App.prefs.min_h2s
                maxVal = App.prefs.max_h2s

            }
        }

    }


    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            // 연결됐을 때
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            Log.d("MainActivity", "connect device")
                            ready_layout.visibility = View.GONE
                            connect_layout.visibility = View.VISIBLE
                            connect = true
                        }
                    } else {
                        // 권한 허용이 안되어있는 경우
                        Log.d("device", "permission denied for device $device")
                    }

                }
            }
            // 연결이 끊겼을때
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.apply {
                    Log.d("Main connect", "Disconnect")
                    ringOff()
                    loopChk = false
                    connect = false
                    // call your method that cleans up and closes communication with the device
                    ready_layout.visibility = View.VISIBLE
                    connect_layout.visibility = View.GONE
                    measure_toggle_btn.isChecked = false

                }
            }
        }
    }


    inner class ThreadClass() : Thread(){

        override fun run() {
            when(App.prefs.sensor){
                resources.getStringArray(R.array.sensor_lists)[0] -> {
                    o2Sensor()
                }
                resources.getStringArray(R.array.sensor_lists)[1] -> {
                    co2Sensor()
                }
                resources.getStringArray(R.array.sensor_lists)[2] -> {
                    tbSensor()
                }
                resources.getStringArray(R.array.sensor_lists)[3] -> {
                    tbSensor()
                }
                resources.getStringArray(R.array.sensor_lists)[4] -> {
                    tbSensor()
                }
                resources.getStringArray(R.array.sensor_lists)[5] -> {
                    tbSensor()
                }
                resources.getStringArray(R.array.sensor_lists)[6] -> {
                    tbSensor()
                }
            }

        }
    }


    /**
     * 이산환탄소 센서 측정
     */
    fun co2Sensor(){
        val serialCommunication = SerialCommunication(manager, 0, 9600, 8, 1, 0)
        var msg : String? = ""
        while(loopChk){
            msg = serialCommunication.SCRead() // Z xxxxx

            if (msg != null) {
                val sensorVal = msg.split(" ")[2]
                runOnUiThread {
                    // 산소농도 값 넣기
                    result_viewer.text = sensorVal + " %"
                    if (sensorVal.toFloat() < minVal!!){
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                        ringOn()
                    } else if (sensorVal.toFloat() > maxVal!! ){
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                        ringOn()
                    } else {
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_greenview)
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customGreen))
                    }
                }
            }
        }
    }

    /**
     * 산소 센서 측정
     */
    fun o2Sensor(){
        val serialCommunication = SerialCommunication(manager, 0, 9600, 8, 1, 0)
        var msg : String? = ""
        var cnt = 0
        while(loopChk){
            cnt += 1
            println("port read")
            msg = serialCommunication.SCRead()
//                if (cnt > 40){
//                    loopChk = false
//                }
            if (msg != null) {
                val hashMap = getMap(msg)
                val oxygen = hashMap.get("%")!!.toFloat()
                // 020.55 -> String
//                    val oxygen = msg!!.split("").toString()

                // 온도
                val temp = hashMap.get("T") + " °C"
                runOnUiThread {

                    // 산소농도 값 넣기
                    result_viewer.text = oxygen.toString() + " %"
                    // 산소농도에 따라 배경화면 색이 변함
                    if (oxygen < minVal!!){
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
                        ringOn()
                    } else if (oxygen > maxVal!! ){
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
                        ringOn()
                    } else {
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customGreen))
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_greenview)
                    }

                    // 온도 값 넣기
                    result_viewer_tmp.text = temp.toString()
                }
            }
        }
    }


    /**
     * 산소측정 센서 각 수치별로 나눠주는 함수
     */
    fun getMap(msg : String?) : MutableMap<String, String>{
        val checkList = listOf<String>("O", "P", "e", "%", "T")
        var checkString = "O"
        val hashMap = mutableMapOf<String, String>("O" to "")
        val msgArray = msg!!.split("")
        for (i in 0 until msgArray.size-1) {
            val n = msgArray[i]
            if (n == " "){
                continue
            }
            if (n in checkList){
                checkString = n
                hashMap.put(n, "")
                continue
            }
            val temp = hashMap.get(checkString)
            hashMap.put(checkString, temp + n)
        }
        return hashMap
    }

    /**
     * TB 센서 함수
     */
    fun tbSensor(){
        val command = byteArrayOfInt(0xFF, 0x01, 0x87, 0x00, 0x00, 0x00, 0x00, 0x00, 0x78)
        val serialCommunication = SerialCommunication(manager, 0, 9600, 8, 1, 0)
        while(loopChk){
            val result = serialCommunication.write(command)

            if (result != null) {
                val ppm = result.toFloat()/1000
                runOnUiThread {
                    // 산소농도 값 넣기
                    result_viewer.text = ppm.toString() + " ppm"
                    if (ppm < minVal!!){
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                        ringOn()
                    } else if (ppm > maxVal!! ){
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                        ringOn()
                    } else {
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_greenview)
                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customGreen))
                    }
                }
            }
        }
    }

    /**
     * Create ByteArray
     */
    fun byteArrayOfInt(vararg ints : Int) = ByteArray(ints.size) {pos -> ints[pos].toByte()}

    // 액션바에 설정버튼 추가
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }



    // 설정버튼에 이벤트 추가
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting_btn -> {
                val settingIntent = Intent(this, SettingActivity::class.java)
                startActivity(settingIntent)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
    fun ringOn(){
        App.ringtone.run {
            if(!isPlaying) play()
        }
    }
    fun ringOff(){
        App.ringtone.run {
            if(isPlaying) stop()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "Destroy")
        unregisterReceiver(usbReceiver)
    }

}
