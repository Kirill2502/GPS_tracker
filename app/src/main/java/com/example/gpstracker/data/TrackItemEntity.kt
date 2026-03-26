package com.example.gpstracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Tracks")
data class TrackItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    @ColumnInfo(name = "time")
    val time: String,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "distance")
    val distance: String,
    @ColumnInfo(name = "velocity")
    val velocity: String,
    @ColumnInfo(name = "geoPoints")
    val geoPoints: String
    )
