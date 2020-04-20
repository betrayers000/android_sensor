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
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    lateinit var driverList : List<UsbSerialDriver>
    lateinit var driver :UsbSerialDriver
    lateinit var connection : UsbDeviceConnection
    lateinit var manager : UsbManager
    lateinit var port : UsbSerialPort
    var len = -1
    val buffer  = ByteArray(30)
    val error = "len is zero"
    var check = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val result : StringBuilder = StringBuilder()
        manager = getSystemService(Context.USB_SERVICE) as UsbManager

        val deviceList : HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { d ->
            val permissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, filter)
            manager.requestPermission(d, permissionIntent)
            result.append(d.toString())
            result_viewer.text = d.toString()
        }
        result_btn.setOnClickListener {
            findDevice(manager)
            openDevice()
            GlobalScope.launch(Dispatchers.Default) {
                while(check){
                    readPort()
                    GlobalScope.launch(Dispatchers.Main) {
                        if (len > 0) {
                            list_viewer.text = len.toString()
                            result_viewer2.text = String(buffer)
                            result_viewer.text = HexDump.Buff2String(buffer)
                            len = 0
                        }
                    }
                }
            }

        }

        cancel_btn.setOnClickListener {

            check = false
        }

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
            port.setParameters(19200, 8, 1, 0)
        }catch (e : Exception){
            result_viewer.text = e.toString()
        }
    }

    fun readPort() {
        if (driverList.isEmpty()){
            return
        }
        len = port.read(buffer, 1000)
    }

    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            //call method to set up device communication
                            result_viewer.text = device.toString()
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

}
