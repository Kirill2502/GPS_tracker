package com.example.gpstracker.data.processor

import android.location.Location
import android.util.Log
import com.example.gpstracker.data.dataSource.LocationDataSource
import com.example.gpstracker.data.mappers.toGeoPointsString
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProcessor @Inject constructor(
    private val locationDataSource: LocationDataSource
) {
    private var totalDistance = 0f
    private val geoPointsList = mutableListOf<GeoPoint>()

    fun process(newLocation: Location, previousLocation: Location?) {
        Log.d("LocationProcessor", "new speed=${newLocation.speed}, prev=$previousLocation")
        if (previousLocation != null && newLocation.speed >0.2f ) {
            val dist = previousLocation.distanceTo(newLocation)
            Log.d("LocationProcessor", "distanceTo = $dist")
            totalDistance += dist
            Log.d("LocationProcessor", "totalDistance = $totalDistance")
            geoPointsList.add(
                GeoPoint(newLocation.latitude, newLocation.longitude)
            )
        }else {
            Log.d("LocationProcessor", "previousLocation is null, first point")
            // Возможно, нужно добавить первую точку даже без перемещения
            geoPointsList.add(GeoPoint(newLocation.latitude, newLocation.longitude))
        }

        locationDataSource.emitLocation(
            velocity = newLocation.speed,
            distance = totalDistance,
            averageSpeed = 0f,
            geoPointsString = geoPointsList.toGeoPointsString()  // ← используем маппер
        )
    }
}