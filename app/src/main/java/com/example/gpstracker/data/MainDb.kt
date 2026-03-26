package com.example.gpstracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [TrackItemEntity::class],
    version = 1,
    exportSchema = false // Не сохранять схему в assets
)
abstract class MainDb: RoomDatabase() {
    companion object{
        @Volatile
        var INSTANCE: MainDb? = null
        fun getDatabase(context: Context): MainDb{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MainDb::class.java,
                    "GpsTracker.db"
                ).build()
                INSTANCE = instance
                return instance

            }
        }
    }
    abstract fun getDao(): MyDao
}