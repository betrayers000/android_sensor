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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.anastr.speedviewlib.components.Section
import com.github.anastr.speedviewlib.components.Style
import com.github.anastr.speedviewlib.components.indicators.Indicator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.measure_toggle_btn
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class Main2Activity : AppCompatActivity() {

    lateinit var mainFragment: MainFragment
    lateinit var subFragment: SubFragment
    lateinit var errorFragment: ErrorFragment
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
    var measureMax : Float? = null
    var unit : String = "ppm"
    private var defaultTime : Long = 1597932005417
    val filter = IntentFilter(ACTION_USB_PERMISSION)

    @ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // actionbar 색 변경
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.customBlack)))
            actionBar.title = App.prefs.sensor
        }


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
        if (App.prefs.sensor != resources.getStringArray(R.array.sensor_lists)[0]){
            subFragment.visibilityResultTemp(true)
        }
        when(App.prefs.sensor){
            resources.getStringArray(R.array.sensor_lists)[0] -> {
                minVal = App.prefs.min_o2
                maxVal = App.prefs.max_o2
                measureMax = 25.0f
                unit = "%"
            }
            resources.getStringArray(R.array.sensor_lists)[1] -> {
                minVal = App.prefs.min_co2
                maxVal = 30000f
                measureMax = 200000f
                unit = "ppm"
                subFragment.setCo2Viewer()
            }
            resources.getStringArray(R.array.sensor_lists)[2] -> {
                minVal = App.prefs.min_co
                maxVal = App.prefs.max_co
                measureMax = 1000.0f
                unit = "ppb"

            }
            resources.getStringArray(R.array.sensor_lists)[3] -> {
                minVal = App.prefs.min_no2
                maxVal = App.prefs.max_no2
                measureMax = 100.0f
                unit = "ppb"

            }
            resources.getStringArray(R.array.sensor_lists)[4] -> {
                minVal = App.prefs.min_so2
                maxVal = App.prefs.max_so2
                measureMax = 100.0f
                unit = "ppb"
            }
            resources.getStringArray(R.array.sensor_lists)[5] -> {
                minVal = App.prefs.min_h2s
                maxVal = App.prefs.max_h2s
                measureMax = 100.0f
                unit = "ppb"
            }
            resources.getStringArray(R.array.sensor_lists)[6] -> {
                minVal = App.prefs.min_hcho
                maxVal = App.prefs.max_hcho
                measureMax = 1000.0f
                unit = "ppb"
            }
        }
        val minV = minVal!!/measureMax!!
        val maxV = maxVal!!/measureMax!!
        subFragment.setResultViewer(unit, measureMax!!, minV, maxV)

    }

    fun onFragmentChange(fragmentNum : Int){
        if (fragmentNum == 1){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, mainFragment).commitAllowingStateLoss()
        } else if (fragmentNum == 2){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, subFragment).commitAllowingStateLoss()
        } else if (fragmentNum == 3){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, errorFragment).commitAllowingStateLoss()
        }
    }

    fun onFragmentMainText(msg : String){
        mainFragment.setText(msg)
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
        val serialCommunication = SerialCommunication(manager, 0, 9600, 8, 1, 0)
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
        val serialCommunication = SerialCommunication(manager, 0, 9600, 8, 1, 0)
        var msg : String? = ""
        while(loopChk){
            msg = serialCommunication.SCRead() // Z xxxxx

            if (msg != null) {
                val sensorVal = msg.split(" ")[2]
                runOnUiThread {
                    // 산소농도 값 넣기
//                    result_viewer.text = sensorVal + " %"

                    try{
                        val co2val = sensorVal.toFloat() * 10
                        subFragment.setResult(co2val)
                        subFragment.setResultTemp((co2val/10000).toString() + " %")
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
//                    result_viewer.text = oxygen.toString() + " %"
                    subFragment.setResult(oxygen)
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

                    // 온도 값 넣기
//                    result_viewer_tmp.text = temp.toString()
                    subFragment.setResultTemp(temp)
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
        val command = byteArrayOfInt(0xFF, 0x01, 0x87, 0x00, 0x00, 0x00, 0x00, 0x00, 0x78)
        val serialCommunication = SerialCommunication(manager, 0, 9600, 8, 1, 0)
        while(loopChk){
            val result = serialCommunication.write(command)
            Log.d("MainAcitvity", result.toString())
            if (result != null) {
                val ppb = result.toFloat()
                Log.d("MainActivity", ppb.toString())
                runOnUiThread {
                    // 산소농도 값 넣기
//                    result_viewer.text = ppm.toString() + " ppm"
//                    result_viewer.speedTo(ppb)
                    subFragment.setResult(ppb)
                    if (ppb < minVal!!){
//                        connect_layout.background = resources.getDrawable(R.drawable.rectangled_redview)
//                        connect_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.customRed))
                        ringOn()
                    } else if (ppb > maxVal!! ){
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
    }

    fun ringOn(){
        Log.d("MainActivity", "Ring on")
        Log.d("MainActivity", defaultTime.toString())
        Log.d("MainActivity", System.currentTimeMillis().toString())
        if (System.currentTimeMillis() > defaultTime){
            App.ringtone.run {
                if(!isPlaying) play()
            }
        }
    }
    fun ringOff(){
        Log.d("MainActivity", "Ring off")
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
                            Log.d("MainActivity", "connect device")
                            Log.d("MainActivity", tbSensorCheck(getSensorType()).toString())
                            if (tbSensorCheck(getSensorType())){
                                onFragmentChange(2)
                                connect = true
                            } else {
                                onFragmentMainText(resources.getString(R.string.needCorrectText))
                            }
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
    }


}
