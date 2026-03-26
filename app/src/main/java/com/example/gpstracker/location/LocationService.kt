package com.example.gpstracker.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.gpstracker.MainActivity
import com.example.gpstracker.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.greenrobot.eventbus.EventBus
import org.osmdroid.util.GeoPoint


class LocationService: Service() {
    private lateinit var locProvider: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var distance = 0f
    private lateinit var geoPointList: ArrayList<GeoPoint>

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
        geoPointList = ArrayList()



    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        locProvider.removeLocationUpdates(locationCallback)

    }

    private fun startNotification(){
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val nChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
           val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChannel)
        }
        val nIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            10,
            nIntent,
            FLAG_MUTABLE)
        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Приложение работает в фоновом режиме.")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(99,notification)

    }
    //ФУНКЦИИ ДЛЯ ДОБАВЛЕНИЯ СЛУШАТЕЛЯ ОБНОВЛЕНИЯ МЕСТОПОЛОЖЕНИЯ
    private fun initLocation(){
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext)


        // Создаём callback для обработки обновлений
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val currentLocation = locationResult.lastLocation//последнее известное местоположение смартфона
                if (lastLocation!=null && currentLocation!=null){
                   if (currentLocation.speed > 0.2){
                       distance += (currentLocation).let { lastLocation?.distanceTo(it) }!!
                       geoPointList.add(GeoPoint(currentLocation.latitude,currentLocation.longitude))
                   }

                    val locModel = LocationModel(
                        currentLocation.speed,
                        distance,
                        geoPointList
                        )
                    sendLocData(locModel)
                }
                lastLocation = currentLocation//последнее местоположение полученное в этой функции,от него замеряем до новой точки

            }
        }
    }
    //ФУНКЦИЯ ДЛЯ ОТПРАВКИ СКОРОСТИ,ДИСТАНЦИИ И ГЕО ТОЧЕК НА MAIN FRAGMENT
    private fun sendLocData(locModel: LocationModel){
        EventBus.getDefault().post(LocationModelEvent(locModel))

    }

    private fun requestLocationUpdates(){
        val updateInterval = PreferenceManager.getDefaultSharedPreferences(
            this
        ).getString("update_time_key","3000")?.toLong()?: 3000
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
    companion object{
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
        var startTime = 0L

    }

}