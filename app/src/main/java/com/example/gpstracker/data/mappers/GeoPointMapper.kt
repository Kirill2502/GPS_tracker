package com.example.gpstracker.data.mappers

import org.osmdroid.util.GeoPoint

fun String.toGeoPoints(): List<GeoPoint>{
    return this.split("/")
        .filter { it.isNotEmpty() }
        .map {
            val p = it.split(",")
            GeoPoint(p[0].toDouble(), p[1].toDouble())
        }
}
fun List<GeoPoint>.toGeoPointsString(): String {
    val sb = StringBuilder()
    forEach {
        sb.append("${it.latitude},${it.longitude}/")
    }
    return sb.toString()
}