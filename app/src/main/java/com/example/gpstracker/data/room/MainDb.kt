package com.example.gpstracker.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gpstracker.data.room.MyDao
import com.example.gpstracker.data.room.TrackItemEntity

@Database(
    entities = [TrackItemEntity::class],
    version = 1,
    exportSchema = false // Не сохранять схему в assets
)
abstract class MainDb: RoomDatabase() {

    abstract fun getDao(): MyDao
}