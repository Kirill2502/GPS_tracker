package com.example.gpstracker.data.mappers

import com.example.gpstracker.data.room.TrackItemEntity
import com.example.gpstracker.domain.models.TrackItemDomain

fun TrackItemEntity.toDomain(): TrackItemDomain {
    return TrackItemDomain(
        id = id,
        time = time,
        date = date,
        distance = distance,
        velocity = velocity,
        geoPoints = geoPoints
    )
}