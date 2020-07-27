package com.example.sensor

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(context: Context) {
    // 파일이름과 저장할 키 값을 만들고 prefs인스턴스 초기화
    val prefs_fileName = "prefs"
    val prefs: SharedPreferences = context.getSharedPreferences(prefs_fileName, 0)

    //key list
    val prefs_key_danger = "danger"
    val prefs_key_sound = "sound"
    val prefs_key_change_switch = "change_switch"
    val prefs_key_stay_switch = "stay_switch"
    val prefs_key_sensor = "sensor"
    val prefs_key_min_switch = "min_switch"
    val prefs_key_max_switch = "max_switch"

    val prefs_key_min_o2 = "min_o2"
    val prefs_key_max_o2 = "max_o2"

    val prefs_key_min_co2 = "min_co2"
    val prefs_key_max_co2 = "max_co2"

    val prefs_key_min_co = "min_co"
    val prefs_key_max_co = "max_co"

    val prefs_key_min_no2 = "min_no2"
    val prefs_key_max_no2 = "max_no2"

    val prefs_key_min_so2 = "min_so2"
    val prefs_key_max_so2 = "max_so2"

    val prefs_key_min_h2s = "min_h2s"
    val prefs_key_max_h2s = "max_h2s"

    // method
    var danger: Float
        get() = prefs.getFloat(prefs_key_danger, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_danger, value).apply()

    var sound : String
        get() = prefs.getString(prefs_key_sound, "싸이렌")!!
        set(value) = prefs.edit().putString(prefs_key_sound, value).apply()

    var change_switch: Boolean
        get() = prefs.getBoolean(prefs_key_change_switch, false)
        set(value) = prefs.edit().putBoolean(prefs_key_change_switch, value).apply()

    var stay_switch: Boolean
        get() = prefs.getBoolean(prefs_key_stay_switch,false)
        set(value) = prefs.edit().putBoolean(prefs_key_stay_switch, value).apply()

    var sensor: String
        get() = prefs.getString(prefs_key_sensor, "산소")!!
        set(value) = prefs.edit().putString(prefs_key_sensor, value).apply()

    var min_switch: Boolean
        get() = prefs.getBoolean(prefs_key_min_switch,true)
        set(value) = prefs.edit().putBoolean(prefs_key_min_switch, value).apply()

    var max_switch: Boolean
        get() = prefs.getBoolean(prefs_key_max_switch,true)
        set(value) = prefs.edit().putBoolean(prefs_key_max_switch, value).apply()


    var min_o2: Float
        get() = prefs.getFloat(prefs_key_min_o2, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_min_o2, value).apply()

    var max_o2: Float
        get() = prefs.getFloat(prefs_key_max_o2, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_max_o2, value).apply()


    var min_co2: Float
        get() = prefs.getFloat(prefs_key_min_co2, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_min_co2, value).apply()

    var max_co2: Float
        get() = prefs.getFloat(prefs_key_max_co2, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_max_o2, value).apply()

    var min_so2: Float
        get() = prefs.getFloat(prefs_key_min_so2, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_min_so2, value).apply()

    var max_so2: Float
        get() = prefs.getFloat(prefs_key_max_so2, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_max_so2, value).apply()

    var min_no2: Float
        get() = prefs.getFloat(prefs_key_min_no2, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_min_no2, value).apply()

    var max_no2: Float
        get() = prefs.getFloat(prefs_key_max_no2, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_max_no2, value).apply()

    var min_co: Float
        get() = prefs.getFloat(prefs_key_min_co, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_min_co, value).apply()

    var max_co: Float
        get() = prefs.getFloat(prefs_key_max_co, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_max_co, value).apply()

    var min_h2s: Float
        get() = prefs.getFloat(prefs_key_min_h2s, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_min_h2s, value).apply()

    var max_h2s: Float
        get() = prefs.getFloat(prefs_key_max_h2s, 18.0F)
        set(value) = prefs.edit().putFloat(prefs_key_max_h2s, value).apply()
}