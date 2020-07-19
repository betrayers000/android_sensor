package com.example.sensor

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.media.RingtoneManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private val READY = "NEED CONNECT"
    private val CONNECT = "CONNECT"
    private val context = this
    lateinit var manager : UsbManager
    var loopChk = true
    val danger = App.prefs.danger
    val sound = App.prefs.sound
    val change = App.prefs.change_switch
    val stay = App.prefs.stay_switch
    var reVal : Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        result_viewer.text = READY
        main_title.text = App.prefs.sensor
        manager = getSystemService(Context.USB_SERVICE) as UsbManager

        result_viewer.movementMethod = ScrollingMovementMethod()

        //임시로 클릭버튼 생성
        connecting_btn.setOnClickListener {
            startActivity(Intent(this, ConnectingActivity::class.java))
        }

        val deviceList : HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { d ->
            val permissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, filter)
            manager.requestPermission(d, permissionIntent)
        }

        main_measure_btn.setOnClickListener {
            val thread = ThreadClass()
            thread.start()
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
                            //call method to set up device communication
//                            val thread = ThreadClass()
//                            thread.start()
                            // 연결 되면 연결 성공 이미지를 넣는다거나 문구를 변경시킨다.
                            result_viewer.text = CONNECT

                        }
                    } else {
                        // 권한 허용이 안되어있는 경우
                        Log.d("device", "permission denied for device $device")
                        result_viewer.text = READY
                    }

                }
            }
            // 연결이 끊겼을때
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.apply {
                    ringOff()
                    // call your method that cleans up and closes communication with the device
                    result_viewer.text = READY
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
                    result_viewer.text = sensorVal
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
                    if (oxygen < danger){
                        main_background.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
                        ringOn()
                    } else if (reVal != null && reVal!! - oxygen > 0.1){
                        ringOn()
                    } else{
                        main_background.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                    }
                    reVal = oxygen

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
        val result = serialCommunication.write(command)
        runOnUiThread {
            // 산소농도 값 넣기
            result_viewer.text = result
        }
//        val port = serialCommunication.port
//        val mListener: SerialInputOutputManager.Listener =
//            object : SerialInputOutputManager.Listener {
//                override fun onRunError(e: java.lang.Exception) {
//                    Log.d("tbSensor", "Runner stopped.")
//                }
//
//                override fun onNewData(data: ByteArray) {
//                    runOnUiThread {
//                        // 산소농도 값 넣기
//                        result_viewer.text = data.toString()
//                    }
//
//                }
//            }
//
//        val serialInputOutputManager = SerialInputOutputManager(port, mListener)
//        Executors.newSingleThreadExecutor().submit(serialInputOutputManager)
//        port.write(command, 1000)
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

}
