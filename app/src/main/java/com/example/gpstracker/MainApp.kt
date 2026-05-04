package com.example.gpstracker

import android.app.Application
import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.gpstracker.data.room.MainDb
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig

@HiltAndroidApp
class MainApp: Application() {
    override fun onCreate() {
        super.onCreate()
        settingsOsm()
    }
    fun settingsOsm(){
        Configuration.getInstance().load(
            applicationContext,
            // Важно: сохраните настройки кеша в своем файле preferences
            applicationContext.getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )

        Configuration.getInstance().userAgentValue = BuildConfig.BUILD_TYPE
    }
}