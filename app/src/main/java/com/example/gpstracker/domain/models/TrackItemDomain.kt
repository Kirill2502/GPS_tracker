package com.example.gpstracker.domain.models

data class TrackItemDomain(
    val id: Int?,
    val time: String,
    val date: String,
    val distance: String,
    val velocity: String,
    val geoPoints: String
)
