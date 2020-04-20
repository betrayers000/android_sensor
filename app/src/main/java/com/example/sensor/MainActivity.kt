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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    lateinit var driverList : List<UsbSerialDriver>
    lateinit var driver :UsbSerialDriver
    lateinit var connection : UsbDeviceConnection
    lateinit var manager : UsbManager
    lateinit var port : UsbSerialPort
    var check = true
    var check_cnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val result : StringBuilder = StringBuilder()
        manager = getSystemService(Context.USB_SERVICE) as UsbManager


        val deviceList : HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { d ->
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, filter)
            manager.requestPermission(d, permissionIntent)
            result.append(d.toString())
            result_viewer.text = d.toString()
        }
        result_btn.setOnClickListener {
            findDevice(manager)
            check_cnt = 0
            GlobalScope.launch {
                    openDevice()
                    delay(1000)
            }
        }




    }

    fun findDevice(manager: UsbManager){
        driverList = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (driverList.isEmpty()){
            check = false
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

        port = driver.ports.get(0)
        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        val buffer  = ByteArray(16)
        val len = port.read(buffer, 1000)
        result_viewer.text = buffer.size.toString()
        if (len > 0){
            result_viewer2.text = HexDump.dumpHexString(buffer)
        } else {
            result_viewer2.text = "len is zero" + check_cnt.toString()
            if (check_cnt > 10){
                check = false
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
                            result_viewer.text = device.deviceName
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
