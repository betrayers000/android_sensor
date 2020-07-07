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
}