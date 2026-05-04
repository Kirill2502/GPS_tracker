package com.example.gpstracker.domain.models

data class LocationDataDomain(
    val velocity: Float = 0f,      // скорость
    val distance: Float = 0f,      // дистанция
    val averageSpeed: Float = 0f, //средняя скорость
    val geoPointsString: String = "" // точки маршрута (как текст)
)