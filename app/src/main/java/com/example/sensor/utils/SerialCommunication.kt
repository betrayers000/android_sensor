package com.example.sensor.utils

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

    /**
     * Create ByteArray
     */
    fun byteArrayOfInt(vararg ints : Int) = ByteArray(ints.size) {pos -> ints[pos].toByte()}


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
        Log.d(SC_TAG, "Result Value : " + resultString)
        return resultString

    }

    /**
     * Serial Communication Read Function
     * return is ByteArray
     * 2020-09-28
     */
    private fun SCread() : ByteArray?{
//        Log.d(SC_TAG, "READ START")

        val buffer = ByteArray(64)
        try {
            val len = port.read(buffer, 1000)
//            Log.d("readArr", len.toString())
            if (len > 0) {
                return buffer.copyOf(len)
            }
        } catch (e: Exception){

        }
        return null
    }

    /**
     * Serial Communication Write Function
     * require Sensor Command
     * 2020-09-28
     */
    private fun SCWrite(command : ByteArray){
//        Log.d(SC_TAG, "Write Communication Sensor")
        runBlocking {
            val job = launch(Dispatchers.IO) {
                try {

                    port.write(command, 1000)
                } catch (e: Exception){

                }

            }
            job.join()
            delay(800)
        }

    }

    /**
     * TB Sensor init function
     * command is 0xD1
     * 2020-09-28
     */
    fun InitTbSensor() : HashMap<String, Any>{
        Log.d(SC_TAG, "Init TB-Sensor")
        SCWrite(byteArrayOfInt(0xD1))
        val result = HashMap<String, Any>()
        val readArr = ByteArray(9)
        var idx = 0
        var cnt = 0
        while (true) {
            val temp = SCread()
            if (temp != null){
                for (i in 0..temp.size-1) {
                    if (idx > 8){
                        break
                    }
//                    Log.d(SC_TAG, idx.toString() + "readArr Value : " + i + " : " + temp[i].toInt().toString())
                    readArr.set(idx, temp[i])
                    idx += 1
                }
            }
            if (idx > 8){
                break
            }
            if (cnt > 300){
                break
            }
            cnt += 1
        }
        if (readArr[3].equals(0x02.toByte())) {
            result.put("unit", "ppm")
        }else {
            result.put("unit", "ppb")
        }
        result.put("type", readArr[0].toInt())
        result.put("decimal", readArr[7].toInt()/16)
        Log.d("readArr", "return")
        return result
    }

    /**
     * TB Sensor Measure Function
     * 2020-09-28
     */
    @ExperimentalUnsignedTypes
    fun readTbSensor(): String{
//        Log.d(SC_TAG, "Init TB-Sensor")
        SCWrite(byteArrayOfInt(0xFF, 0x01, 0x87, 0x00, 0x00, 0x00, 0x00, 0x00, 0x78))
        val readArr = ByteArray(12)
        var idx = 0
        var cnt = 0
        while (true) {
            val temp = SCread()
            if (temp != null){
                for (i in 0..temp.size-1) {
                    if (idx > 10){
                        break
                    }
//                    Log.d(SC_TAG, idx.toString() + "readArr Value : " + i + " : " + temp[i].toInt().toString())

                    readArr.set(idx, temp[i])
                    idx += 1
                }
            }
            if (idx > 10){
                break
            }
            cnt += 1
            if (cnt > 300){
                break
            }
        }
        val result = (readArr[6].toUByte().toInt() * 256 + readArr[7].toUByte().toInt()).toString()
        return result
    }



    @ExperimentalUnsignedTypes
    fun write(command : ByteArray) : String?{
        Log.d(SC_TAG, "Write & Read START")
        var resultString = ""
        runBlocking {
            Log.d(SC_TAG, "Write & Read START job")
            val job = launch(Dispatchers.IO) {
                try {
                    port.write(command, 1000)
                } catch (e : Exception){

                }

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
                                if (command.size == 1){
                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[0].toString() )
                                    resultString += byteArray[0].toString()
                                } else {
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[0].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[1].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[2].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[3].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[4].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[5].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[6].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[7].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[8].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[9].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[10].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[11].toUByte().toString() )
//                                    Log.d(SC_TAG, "Write & Read START job result : " + byteArray[12].toUByte().toString() )
                                    resultString += (byteArray[6].toUByte().toInt() * 256 + byteArray[7].toUByte().toInt()).toString()
//                                    Log.d(SC_TAG, "Write & Read START job result : " + resultString )
                                }
                                check = false
                            }
                        } catch (e: Exception){
                            println(e)
                            Log.d(SC_TAG, e.toString())
                            Log.d(SC_TAG, "Write & Read START error")
                        }
                    }
                    job.join()
                    delay(300)
                }
            }
            job.join()
        }
        return resultString
    }

}