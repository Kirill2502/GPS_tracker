package com.example.gpstracker.presentation.models

import org.osmdroid.util.GeoPoint
import java.io.Serializable
import java.util.ArrayList

data class LocationModel(
    val velocity: Float = 0.0f,
    val distance: Float = 0.0f,
    val averageSpeed: String = "0",
    val geoPointsList: ArrayList<GeoPoint>
): Serializable