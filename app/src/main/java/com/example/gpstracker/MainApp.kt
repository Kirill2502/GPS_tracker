package com.example.gpstracker

import android.app.Application
import com.example.gpstracker.data.MainDb

class MainApp: Application() {
    val database by lazy { MainDb.getDatabase(this) }
}