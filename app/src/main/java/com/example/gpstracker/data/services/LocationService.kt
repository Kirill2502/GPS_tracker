package com.example.gpstracker.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.gpstracker.R
import com.example.gpstracker.data.processor.LocationProcessor
import com.example.gpstracker.presentation.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {
    @Inject
    lateinit var locationProcessor: LocationProcessor
    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        requestLocationUpdates()

        isRunning = true
        return START_STICKY//если у пользователя будет мало памяти сервис убьется,но при появлении памяти он будет перезапущен
    }

    override fun onCreate() {
        super.onCreate()
        initLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        locProvider.removeLocationUpdates(locationCallback)

    }

    private fun startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChannel)
        }
        val nIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            10,
            nIntent,
            PendingIntent.FLAG_MUTABLE
        )
        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Приложение работает в фоновом режиме.")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(99, notification)

    }

    //ФУНКЦИИ ДЛЯ ДОБАВЛЕНИЯ СЛУШАТЕЛЯ ОБНОВЛЕНИЯ МЕСТОПОЛОЖЕНИЯ
    private fun initLocation() {
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)


        // Создаём callback для обработки обновлений
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLocation = locationResult.lastLocation ?: return
                val previousLocation = lastLocation
                lastLocation = currentLocation
                Log.d("LocationDebug", "Service received location: speed=${currentLocation.speed}, lat=${currentLocation.latitude}")

                // ✅ Только передаём сырые данные в процессор
                locationProcessor.process(currentLocation, previousLocation)
            }
        }
    }


    private fun requestLocationUpdates() {
        val updateInterval = PreferenceManager.getDefaultSharedPreferences(
            this
        ).getString("update_time_key", "3000")?.toLong() ?: 3000
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            updateInterval// интервал в мс (5 сек)
        ).setMinUpdateIntervalMillis(updateInterval)//минимальный интервал
            .setWaitForAccurateLocation(false)
            .setMaxUpdateDelayMillis(updateInterval)
            .build()
        try {
            locProvider.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            println("Ошибка доступа к местоположению: ${e.message}")
        }
    }

    companion object {
        const val CHANNEL_ID = "channel_1"

        var startTime = 0L

    }

}