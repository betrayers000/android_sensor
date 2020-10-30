package com.example.sensor.utils

import com.example.sensor.data.SensorData

interface ReadStrategy {
    fun read(arr: ByteArray) {}
}


/**
 * TBSensor Read Strategy
 * TBSenor 측정값을 return
 * arr[6] * 256 + arr[7]
 * arr convert to UByte
 */
class TBSensorStrategy : ReadStrategy{
    override fun read(arr:ByteArray) {
        val result = (arr[6].toUByte().toInt() * 256 + arr[7].toUByte().toInt())
        SensorData.measureValue = result.toFloat()
    }
}


/**
 * TBSensor init strategy
 * Unit,
 */
class SensorInit: ReadStrategy{
    override fun read(arr: ByteArray){
        if (arr[3].equals(0x02.toByte())) {
            SensorData.unit = "ppm"
        }else {
            SensorData.unit = "ppb"
        }
        SensorData.type = arr[0].toInt()
        SensorData.decimal = arr[7].toInt()/16
    }
}

class OxygenSensorStrategy: ReadStrategy{
    override fun read(arr: ByteArray) {
        super.read(arr)

    }
}