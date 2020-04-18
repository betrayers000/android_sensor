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


class MainActivity : AppCompatActivity() {

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var result : StringBuilder = StringBuilder()
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager

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
            connectDevice(manager)
//            openDevice(manager)
        }




    }
    fun connectDevice(manager : UsbManager){
        val driverList : List<UsbSerialDriver> = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (driverList.isEmpty()){
            return
        }
        lateinit var buffer : ByteArray
        val driver :UsbSerialDriver = driverList.get(0)
        val device = driver.device
        list_viewer.text = device.interfaceCount.toString()
        device.getInterface(0).also { intf ->
            intf.getEndpoint(0).also { endpoint ->
                manager.openDevice(device).apply {
                    claimInterface(intf, true)
                    bulkTransfer(endpoint, buffer, buffer.size, 1000)
                }
            }
        }
    }


    fun openDevice(manager : UsbManager){
        val driverList : List<UsbSerialDriver> = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (driverList.isEmpty()){
            return
        }
        var builder = StringBuilder()
        driverList.forEach { driver ->
            builder.append(driver.device.deviceName)
        }
        list_viewer.text = builder
        val driver :UsbSerialDriver = driverList.get(0)
        val connection : UsbDeviceConnection = manager.openDevice(driver.device)

        val port : UsbSerialPort = driver.ports.get(0)
        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        var buffer  = ByteArray(30)
        val len = port.read(buffer, 1000)
        result_viewer.text = buffer.size.toString()
        result_viewer2.text = HexDump.dumpHexString(buffer)
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
