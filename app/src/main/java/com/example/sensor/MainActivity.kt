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
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    lateinit var driverList : List<UsbSerialDriver>
    lateinit var driver :UsbSerialDriver
    lateinit var connection : UsbDeviceConnection
    lateinit var manager : UsbManager
    lateinit var port : UsbSerialPort
    lateinit var buffer : ByteArray
    val error = "len is zero"
    var check = true

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
        result_btn.setOnClickListener {
            connect()
            setstatus()
        }

        cancel_btn.setOnClickListener {

            list_viewer.text  = "Ready"
            result_viewer.text = "Ready"
            result_viewer2.text = "Ready"
            port.close()
        }

    }

    fun connect(){
        findDevice(manager)
        openDevice()
    }

    fun setstatus(){
        // O
        var ppO2Val = arrayListOf<String>()
        // T
        var temperatureVal = arrayListOf<String>()
        // P
        var pressureVal = arrayListOf<String>()
        // %
        var O2Val = arrayListOf<String>()
        // e
        var statVal = arrayListOf<String>()
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
                                display("finish point ")
                                check = false
                            }
                        }
                    }
                    job.join()
                    delay(10)
                    println("job end")
//                    if (cnt > 200){
//                        break
//                    }
//                    println(cnt.toString())
                }

                if (resultString != ""){
                    display("full : " + resultString)
                    display(resultString.split("").toString())
                }
                println("launch finish")
            }
            job.join()
            println("main thread ")
        }
        println("after blocking")
//        list_viewer.text = "last String : " + resultString
//        result_viewer.text = "complete String : " + ppO2Val + temperatureVal + pressureVal + O2Val + statVal
//        result_viewer2.text = "stat value : " + statVal
    }

    fun findDevice(manager: UsbManager){
        driverList = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (driverList.isEmpty()){
            return
        }
        val builder = StringBuilder()
        driverList.forEach { driver ->
            builder.append(driver.device.deviceName)
        }
        list_viewer.text = builder
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

    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            //call method to set up device communication
                            connect()
                            for (i in 0..10){

                                setstatus()
                                display("now count : " + i.toString())
                            }
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
    fun display(msg : String){
        GlobalScope.launch(Dispatchers.Main){
            result_viewer.append(msg + "\n")
        }
    }

}
