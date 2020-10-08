package com.example.sensor.main

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.example.sensor.*
import com.example.sensor.utils.SerialCommunication
import com.example.sensor.setting.SettingActivity
import kotlinx.android.synthetic.main.activity_main2.*
import java.lang.Exception
import kotlin.collections.HashMap
import kotlin.math.roundToInt
import kotlin.time.toDuration

class Main2Activity : AppCompatActivity() {

    // Fragment List
    lateinit var mainFragment: MainFragment
    lateinit var subFragment: SubFragment
    lateinit var errorFragment: ErrorFragment

    // Host Mode
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    // Usb Connect
    lateinit var manager : UsbManager
    var connect = false
    var loopChk = true
    lateinit var scManager: SerialCommunication
    private val SENSOR_MESSAGE = "sensorMessage"

    // Measure Thread
    val thread = ThreadClass()

    // Connected Sensor Setting
    var minVal : Float? = null
    var maxVal : Float? = null
    var measureMax : Float? = null
    var unit : String = "ppm"
    var type : Int  = 0
    var decimal : Int = 0

    // Alarm Time
    private var defaultTime : Long = 1597932005417

    // Intent Filter
    val filter = IntentFilter(ACTION_USB_PERMISSION)

    @ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // actionbar 색 변경
//        val actionBar = supportActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
//        if (actionBar != null) {
//            actionBar.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.contentBodyColor)))
//            actionBar.title = App.prefs.sensor
//        }


        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)

        mainFragment = MainFragment()
        subFragment = SubFragment()
        errorFragment = ErrorFragment()

        onFragmentChange(1)

//        setSensorParameter()

        measure_toggle_btn.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d("Main", "click toggle")
            if(!connect){
                measure_toggle_btn.isChecked = false
            } else {
                if(isChecked){
                    loopChk = true
                    thread.start()
                } else {
                    loopChk = false
                    ringOff()
                }
            }
        }

    }

    fun initManager(){
        manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList : HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { d ->
            val permissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            manager.requestPermission(d, permissionIntent)
        }
    }

    /**
     * 결과창 각 센서에 맞게 셋팅하는 과정
     */
    fun setSensorParameter(){
        when(App.prefs.sensor){
            resources.getStringArray(R.array.sensor_lists)[0] -> {
                minVal = App.prefs.min_o2
                maxVal = App.prefs.max_o2
                measureMax = 25.0f
            }
            resources.getStringArray(R.array.sensor_lists)[1] -> {
                minVal = App.prefs.min_co2
                maxVal = 30000f
                measureMax = 200000f
            }
            resources.getStringArray(R.array.sensor_lists)[2] -> {
                minVal = App.prefs.min_co
                maxVal = App.prefs.max_co
                measureMax = 1000.0f
            }
            resources.getStringArray(R.array.sensor_lists)[3] -> {
                minVal = App.prefs.min_no2
                maxVal = App.prefs.max_no2
                measureMax = 100.0f
            }
            resources.getStringArray(R.array.sensor_lists)[4] -> {
                minVal = App.prefs.min_so2
                maxVal = App.prefs.max_so2
                measureMax = 100.0f
            }
            resources.getStringArray(R.array.sensor_lists)[5] -> {
                minVal = App.prefs.min_h2s
                maxVal = App.prefs.max_h2s
                measureMax = 100.0f
            }
            resources.getStringArray(R.array.sensor_lists)[6] -> {
                minVal = App.prefs.min_hcho
                maxVal = App.prefs.max_hcho
                measureMax = 1000.0f
            }
        }
        val minV = minVal!!/measureMax!!
        val maxV = maxVal!!/measureMax!!
        subFragment.setResultViewer(unit, measureMax!!, minV, maxV)

    }

    fun onFragmentChange(fragmentNum : Int){
        Log.d("MainActivity", unit)
        Log.d("MainActivity", type.toString())
        Log.d("MainActivity", decimal.toString())
        if (fragmentNum == 1){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, mainFragment).commitAllowingStateLoss()
        } else if (fragmentNum == 2){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, subFragment).commitAllowingStateLoss()
        } else if (fragmentNum == 3){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, errorFragment).commitAllowingStateLoss()
        }
    }

    /**
     * Create ByteArray
     */
    fun byteArrayOfInt(vararg ints : Int) = ByteArray(ints.size) {pos -> ints[pos].toByte()}

    /**
     * TB sensor check
     */
    @ExperimentalUnsignedTypes
    fun tbSensorCheck(type : String): Boolean{
        val command = byteArrayOfInt(0xD1)
        val serialCommunication =
            SerialCommunication(
                manager,
                0,
                9600,
                8,
                1,
                0
            )
        val result = serialCommunication.write(command)
        if (!result.equals(type)){
            return false
        }
        return true

    }

    /**
     * sensor type getter
     */
    fun getSensorType() : String {
        when(App.prefs.sensor){
            resources.getStringArray(R.array.sensor_lists)[0] -> {
                return resources.getStringArray(R.array.sensor_type)[0]
            }
            resources.getStringArray(R.array.sensor_lists)[1] -> {
                return resources.getStringArray(R.array.sensor_type)[1]
            }
            resources.getStringArray(R.array.sensor_lists)[2] -> {
                return resources.getStringArray(R.array.sensor_type)[2]
            }
            resources.getStringArray(R.array.sensor_lists)[3] -> {
                return resources.getStringArray(R.array.sensor_type)[3]
            }
            resources.getStringArray(R.array.sensor_lists)[4] -> {
                return resources.getStringArray(R.array.sensor_type)[4]
            }
            resources.getStringArray(R.array.sensor_lists)[5] -> {
                return resources.getStringArray(R.array.sensor_type)[5]
            }
            resources.getStringArray(R.array.sensor_lists)[6] -> {
                return resources.getStringArray(R.array.sensor_type)[6]
            }
        }
        return ""
    }


    // 액션바에 설정버튼 추가
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    /**
     * 설정버튼 이벤트
     */
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


    inner class ThreadClass() : Thread(){

        @ExperimentalUnsignedTypes
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
        var msg : String? = ""
        while(loopChk){
            msg = scManager.SCRead() // Z xxxxx

            if (msg != null) {
                val sensorVal = msg.split(" ")[2]
                runOnUiThread {
                    // 산소농도 값 넣기
//                    result_viewer.text = sensorVal + " %"

                    try{
                        val co2val = sensorVal.toFloat() * 10
                        subFragment.setResult(co2val.toString())
//                        subFragment.setResultSub((co2val/10000).toString() + " %")
                        subFragment.setUnit(unit)

//                        result_viewer.speedTo(co2val)
//                        result_viewer_tmp.text = (co2val/10000).toString() + " %"
                        Log.d("MainActivity", maxVal.toString())
                        if (sensorVal.toFloat() < minVal!!){
                            //                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
//                            connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                            ringOn()
                        } else if (sensorVal.toFloat() > maxVal!! ){
                            //                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
//                            connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                            ringOn()
                        } else {
                            //                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_greenview)
//                            connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customGreen))
                        }
                    } catch (e : Exception){

                    }
                }
            }
        }
    }

    /**
     * 산소 센서 측정
     */
    fun o2Sensor(){
        var msg : String? = ""
        var cnt = 0
        while(loopChk){
            cnt += 1
            println("port read")
            msg = scManager.SCRead()
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
//                    result_viewer.text = oxygen.toString() + " %"
                    subFragment.setResult(oxygen.toString())
                    subFragment.setUnit(unit)
                    subFragment.setResultSub(temp)

                    // 산소농도에 따라 배경화면 색이 변함
                    if (oxygen < minVal!!){
//                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
                        ringOn()
                    } else if (oxygen > maxVal!! ){
//                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
                        ringOn()
                    } else {
//                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customGreen))
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_greenview)
                    }

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
    @ExperimentalUnsignedTypes
    fun tbSensor(){
        while(loopChk){
            val res = scManager.readTbSensor()
            val measureVal = res.toFloat()
            val result = measureVal/(Math.pow(10.toDouble(), decimal.toDouble()))
            Log.d("MainActivty", res + " and " + measureVal + " and " + result + " decimal : " + decimal)
            runOnUiThread {
                // 산소농도 값 넣기
//                    result_viewer.text = ppm.toString() + " ppm"
//                    result_viewer.speedTo(ppb)
                try {
                    subFragment.setResult(result.toString())
//                    var percent : Double = 0.0
//                    if (unit == "ppm"){
//                        percent = result/10000
//                    } else {
//                        percent = result/10000000
//                    }
//                    subFragment.setResultSub(((percent*1000).roundToInt()/1000f).toString() + "%")
                    subFragment.setUnit(unit)
                } catch (e : Exception){

                }
                if (result < minVal!!){
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
//                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                    ringOn()
                } else if (result > maxVal!! ){
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
//                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                    ringOn()
                } else {
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_greenview)
//                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customGreen))
                }
            }


        }

    }

    fun ringOn(){
        if (System.currentTimeMillis() > defaultTime){
            App.ringtone.run {
                if(!isPlaying) play()
            }
        }
    }
    fun ringOff(){
        defaultTime = System.currentTimeMillis() + 600000
        App.ringtone.run {
            if(isPlaying) stop()
        }
    }

    override fun onResume() {
        super.onResume()
        App.resumed = true

        registerReceiver(usbReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        App.resumed = false
        unregisterReceiver(usbReceiver)
    }

    /**
     *  usb 연결 / 연결해제시 실행되는 작업
     */
    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            // 연결됐을 때
            Log.d("MAinActivity", intent.action.toString())
            if (ACTION_USB_PERMISSION == intent.action || UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                synchronized(this) {
                    Log.d("MainActivity", "connect")
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            scManager = SerialCommunication(
                                manager,
                                0,
                                9600,
                                8,
                                1,
                                0)
                            val pendingResult = goAsync()
                            val asyncTask = Task(pendingResult, intent)
                            asyncTask.execute()
                            connect = true

                        }
                    } else {
                        // 권한 허용이 안되어있는 경우
                        Log.d("MainActivity", "permission denied for device $device")
                        initManager()
                    }

                }
            }
            // 연결이 끊겼을때
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                ringOff()
                Log.d("Main connect", "Disconnect")
                onFragmentChange(1)
                connect = false
                loopChk = false
                measure_toggle_btn.isChecked = false
//                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
//                device?.apply {
//                    ringOff()
//                    Log.d("Main connect", "Disconnect")
//                    onFragmentChange(1)
//                    connect = false
//                    loopChk = false
//                    measure_toggle_btn.isChecked = false
//                }
            }
        }
        private inner class Task(
            private val pedndingResult: PendingResult,
            private val intent : Intent
        ): AsyncTask<String, Int, String>(){
            override fun doInBackground(vararg params: String?): String {
                val result = scManager.InitTbSensor()
                try {

                    unit = result.get("unit").toString()
                    type = result.get("type").toString().toInt()
                    decimal = result.get("decimal").toString().toInt()
                } catch (e: Exception){
                    onFragmentChange(3)
                    println(e)
                }

                if (type.toString().equals(getSensorType())){
                    onFragmentChange(2)
                }
                return toString().also{
                    log-> Log.d("MainActivty", log)
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
            }
        }
    }

}
