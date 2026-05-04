package com.example.gpstracker.data.system

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.gpstracker.data.services.LocationService

class LocationServiceController(private val context: Context) {

    fun start() {
        val intent = Intent(context, LocationService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stop() {
        context.stopService(
            Intent(context, LocationService::class.java)
        )
    }
}
