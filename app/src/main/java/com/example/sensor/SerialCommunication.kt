package com.example.sensor

import android.content.Context
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.charset.Charset
import java.util.concurrent.Executors

class SerialCommunication(
    val manager: UsbManager,
    val index: Int,
    val baudRate: Int,
    val dataBits : Int,
    val stopBits: Int,
    val parity: Int
) {
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

    fun SCRead() : String?{
        Log.d(SC_TAG, "READ START")

        var resultString = ""
        runBlocking {
                var cnt = 0
                var check = true
                while (check) {
                    cnt += 1
                    val job = launch(Dispatchers.IO) {
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
                    job.join()
                    delay(300)
                }
            }
        if (resultString.equals("sensor : ")){
            return null
        }
        return resultString

    }


    fun write(command : ByteArray) : String?{
        Log.d(SC_TAG, "Write & Read START")
        var resultString = ""
        runBlocking {
            Log.d(SC_TAG, "Write & Read START job")
            val job = launch(Dispatchers.IO) {
                port.write(command, 1000)
                var cnt = 0
                var check = true
                while (check) {
                    cnt += 1
                    val job = launch(Dispatchers.IO) {
                        val buffer = ByteArray(64)
                        try {
                            val len = port.read(buffer, 1000)

                            if (len > 1) {
                                val byteArray = buffer.copyOf(len)
//                                val encode = String(byteArray, Charsets.US_ASCII)
//                                resultString += encode
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[0].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[1].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[2].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[3].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[4].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[5].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[6].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[7].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[8].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[9].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[10].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[11].toString() )
                                Log.d(SC_TAG, "Write & Read START job result : " + byteArray[12].toString() )
                                resultString += (byteArray[6] * 256 + byteArray[7]).toString()
                                Log.d(SC_TAG, "Write & Read START job result : " + resultString )
                                check = false
                            }
                        } catch (e: Exception){
                            Log.d(SC_TAG, "Write & Read START error")
                        }
                    }
                    job.join()
                    delay(300)
                }
            }
            job.join()
            delay(300)
        }
        return resultString
    }

}