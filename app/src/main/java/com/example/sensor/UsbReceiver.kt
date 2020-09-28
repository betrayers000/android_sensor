package com.example.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class UsbReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!App.resumed) {
            val applicationContext = context!!.applicationContext
            val startIntent = Intent(applicationContext, Main2Activity::class.java)
            startIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext.startActivity(startIntent)
        }
        Log.d("device", "call receiver")
        Log.d("device", intent!!.action.toString())
        // 연결됐을 때
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
            synchronized(this) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                Log.d("device", device!!.productId.toString())
                Log.d("device", device.vendorId.toString())
                Log.d("device", device.toString())

                device?.apply {
                    Log.d("device", "connect")
                    val usbIntent = Intent("usbevent")
                    usbIntent.putExtra("msg", "connect")
                    LocalBroadcastManager.getInstance(context!!).sendBroadcast(usbIntent)

                }

            }
        }
        // 연결이 끊겼을때
        if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            device?.apply {
                val usbIntent = Intent("usbevent")
                usbIntent.putExtra("msg", "disconnect")
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(usbIntent)
            }
        }
    }
}