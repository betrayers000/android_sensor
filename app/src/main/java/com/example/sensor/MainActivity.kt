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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = getSystemService(Context.USB_SERVICE) as UsbManager

        result_viewer.movementMethod = ScrollingMovementMethod()

        val deviceList : HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { d ->
            val permissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, filter)
            manager.requestPermission(d, permissionIntent)

        }
    }

    fun connect(){
        findDevice(manager)
        openDevice()
    }

    fun setstatus(){
        var resultString = ""
        runBlocking {
            val job = launch(Dispatchers.Default) {
                println("launch in runblocking")
                var cnt = 0
                var check = true
                while (check) {
                    cnt += 1
                    val job = launch(Dispatchers.IO) {
                        println("job start")
                        val len = readPort()
                        if (len > 1) {
                            val byteArray = Arrays.copyOf(buffer, len)
                            val encode = String(byteArray, Charsets.UTF_8)
                            resultString += encode
                            if (encode.contains("\r\n")) {
//                                display("finish point ")
                                check = false
                            }
                        }
                    }
                    job.join()
                    delay(10)
                    println("job end")
                }

                if (resultString != ""){
//                    display("full : " + resultString)
//                    display(resultString.split("").toString())
                }
                println("launch finish")
            }
            job.join()
            println("main thread ")
        }
        println("after blocking")
    }

    fun findDevice(manager: UsbManager){
        driverList = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (driverList.isEmpty()){
            return
        }
        driver = driverList.get(0)
        connection = manager.openDevice(driver.device)
    }


    fun openDevice(){
        if (driverList.isEmpty()){
            return
        }
        try {
            port = driver.ports.get(0)
            port.open(connection)
            port.setParameters(9600, 8, 1, 0)
        }catch (e : Exception){
            result_viewer.text = e.toString()
        }
    }

    fun readPort() : Int{
        buffer = ByteArray(32)
        if (driverList.isEmpty()){
            return 0
        }
        return port.read(buffer,1000)
    }

    fun getSensorParmas(){
        val serialCommunication = SerialCommunication(manager, 0, 9600, 8, 1, 0)
        var cnt = 0
        while(loopChk){
            cnt += 1
            println("port read")
            var msg : String? = ""
            msg = serialCommunication.SCRead()
            if (msg != null){
//                display(msg + cnt.toString())
                val thread = ThreadClass()
                thread.start()
            }
            if (cnt > 40){
                loopChk = false
            }
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
                    val oxygen = getOxgen(msg)
                    // 020.55 -> String
//                    val oxygen = msg!!.split("").toString()

                    // 온도
                    val temp = 21
                    runOnUiThread {

                        // 산소농도 값 넣기
                        result_viewer.text = oxygen
                        // 산소농도에 따라 배경화면 색이 변함
                        if (oxygen.toFloat() < 18){
                            main_background.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDanger))
                        }else{
                            main_background.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        }

                        // 온도 값 넣기
                        result_viewer_tmp.text = temp.toString()
                    }
                }
            }
        }
    }
    fun getOxgen(msg : String?) : String{
        var oxygen = ""
        var arrChk = false
        val msgArray = msg!!.split("")
        for (i in 0 until msgArray.size-1){
            if (msgArray[i] == "%"){
                arrChk = true
                continue
            }
            if (msgArray[i] == "e"){
                arrChk = false
                break
            }
            if (arrChk) {
                oxygen += msgArray[i]
            }
        }
        return oxygen
    }

}
