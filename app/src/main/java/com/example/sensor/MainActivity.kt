package com.example.sensor

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private val context = this
    lateinit var driverList : List<UsbSerialDriver>
    lateinit var driver :UsbSerialDriver
    lateinit var connection : UsbDeviceConnection
    lateinit var manager : UsbManager
    lateinit var port : UsbSerialPort
    lateinit var buffer : ByteArray
    var loopChk = true
    var tempoper = "+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
    }


    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            //call method to set up device communication
                            val thread = ThreadClass()
                            thread.start()

                        }
                    } else {
                        Log.d("device", "permission denied for device $device")
                    }

                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.apply {
                    // call your method that cleans up and closes communication with the device
                }
            }
        }
    }
    inner class ThreadClass() : Thread(){
        val serialCommunication = SerialCommunication(manager, 0, 9600, 8, 1, 0)

        var msg : String? = ""
        override fun run() {
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
                        if (oxygen < 18){
                            main_background.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
                        }else{
                            main_background.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                        }

                        // 온도 값 넣기
                        result_viewer_tmp.text = temp.toString()
                    }
                }
            }
        }
    }
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
//            if (n == "+"){
//                continue
//            } else if (n == "-"){
//                tempoper = "-"
//                continue
//            }
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


}
