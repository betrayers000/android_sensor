package com.example.sensor

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.*
import java.util.*

class SerialCommunication(
    val manager: UsbManager,
    val index: Int,
    val baudRate: Int,
    val dataBits : Int,
    val stopBits: Int,
    val parity: Int) {
    // manager : UsbManager, index : driverList 에서 선택할 driver 번호

    private val SC_TAG = "Serial Communication"
    lateinit var driverList : List<UsbSerialDriver>
    lateinit var driver :UsbSerialDriver
    lateinit var connection : UsbDeviceConnection
    lateinit var port : UsbSerialPort

    init {
        this.SCFindDevice()
        this.SCOpenDevice()
    }

    fun SCFindDevice(){
        driverList = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (driverList.isEmpty()){
            return
        }
        driver = driverList.get(index)
        connection = manager.openDevice(driver.device)
    }

    fun SCOpenDevice(){
        if (driverList.isEmpty()){
            return
        }
        try {
            port = driver.ports.get(0)
            port.open(connection)
            port.setParameters(baudRate, dataBits, stopBits, parity)
        }catch (e : Exception){
            Log.d(SC_TAG, e.toString())
        }
    }

//    fun SCRead() : String{
//
//        var resultString = ""
//        runBlocking {
//            val job = launch(Dispatchers.Default) {
//                println("launch in runblocking")
//                var cnt = 0
//                var check = true
//                while (check) {
//                    cnt += 1
//                    val job = launch(Dispatchers.IO) {
//                        println("job start")
//                        val buffer = ByteArray(32)
//                        val len = port.read(buffer, 1000)
//                        if (len > 1) {
//                            val byteArray = Arrays.copyOf(buffer, len)
//                            val encode = String(byteArray, Charsets.UTF_8)
//                            resultString += encode
//                            if (encode.contains("\r\n")) {
//                                check = false
//                            }
//                        }
//                    }
//                    job.join()
//                    delay(10)
//                    println("job end")
//                }
//                println("launch finish")
//            }
//            job.join()
//            println("main thread ")
//        }
//        return resultString
//
//    }
fun SCRead() : String?{

    var resultString = ""
    runBlocking {
            var cnt = 0
            var check = true
            while (check) {
                cnt += 1
                val job = launch(Dispatchers.IO) {
                    println("job start")
                    val buffer = ByteArray(64)
                    try {
                        val len = port.read(buffer, 1000)

                        if (len > 1) {
                            val byteArray = buffer.copyOf(len)
                            val encode = String(byteArray, Charsets.UTF_8)
                            resultString += encode
                            if (encode.contains("\r\n")) {
                                check = false
                            }
                        }
                    } catch (e: Exception){

                    }
                }
//                if (cnt > 5){
//                    check= false
//                    resultString += cnt.toString()
//                }
                job.join()
                delay(10)
                println("job end")
            }
            println("launch finish")
        }
    if (resultString.equals("sensor : ")){
        return null
    }
    println("blocking end")
    return resultString

}

}