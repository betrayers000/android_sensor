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

class SerialCommunication2(
    val manager: UsbManager,
    val index: Int,
    val baudRate: Int,
    val dataBits : Int,
    val stopBits: Int,
    val parity: Int
) {

    // manager : UsbManager, index : driverList 에서 선택할 driver 번호

    private val SC_TAG = "Serial Communication"
    private lateinit var driverList : List<UsbSerialDriver>
    private lateinit var driver :UsbSerialDriver
    private lateinit var connection : UsbDeviceConnection
    private lateinit var port : UsbSerialPort
    private lateinit var readStrategy : ReadStrategy

    init {
        this.SCFindDevice()
        this.SCOpenDevice()
    }

    /**
     * Create ByteArray
     */
    fun byteArrayOfInt(vararg ints : Int) = ByteArray(ints.size) {pos -> ints[pos].toByte()}



    private fun SCFindDevice(){
        driverList = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (driverList.isEmpty()){
            return
        }
        driver = driverList.get(index)
        connection = manager.openDevice(driver.device)
    }

    private fun SCOpenDevice(){
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

    fun Read(size : Int){
        val resultArr = SCread(size)
        readStrategy.read(resultArr)
    }

    fun write(command: ByteArray, size: Int){
        port.write(command, 1000)
        val resultArr = SCread(size)
        readStrategy.read(resultArr)

    }

    fun setReadStrategy(_readStrategy : ReadStrategy){
        readStrategy = _readStrategy
    }



    /**
     * Serial Communication Read Function
     * return is ByteArray
     * 2020-09-28
     */
    private fun ReadSensor() : ByteArray?{
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
     * Read function with ReadSensor()
     * return sum of bytearray returned ReadSensor
     * 2020-10-30
     */
    private fun SCread(size : Int) : ByteArray{
        val readArr = ByteArray(size)
        var idx = 0
        while (true) {
            val temp = ReadSensor()
            if (temp != null){
                for (i in 0..temp.size-1) {
                    if (idx > size){
                        break
                    }
                    readArr.set(idx, temp[i])
                    idx += 1
                }
            }
            if (idx > size){
                break
            }
        }
        return readArr
    }

}