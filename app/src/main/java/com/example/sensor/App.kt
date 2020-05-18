package com.example.sensor

import android.app.Application
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

class App:Application() {
    companion object {
        lateinit var prefs:SharedPreference
        lateinit var uriRingtone : Uri
        lateinit var ringtone : Ringtone
    }

    override fun onCreate() {
        prefs = SharedPreference(applicationContext)
        val sound = prefs.sound
        var uriRingtone : Uri? = null
        when(sound){
            "싸이렌" -> {
                uriRingtone = Uri.parse("android.resource://" + packageName + "/" + R.raw.police)
            }
            "시끄러운소리" -> {
                uriRingtone = Uri.parse("android.resource://" + packageName + "/" + R.raw.loud)
            }
            "경고음" -> {
                uriRingtone = Uri.parse("android.resource://" + packageName + "/" + R.raw.alert)
            }
            else -> {
                uriRingtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        }
        ringtone = RingtoneManager.getRingtone(this, uriRingtone)
        super.onCreate()
    }
}