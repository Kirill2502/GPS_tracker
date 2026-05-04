package com.example.gpstracker.data.controllers

import com.example.gpstracker.data.system.LocationServiceController
import com.example.gpstracker.domain.controller.TrackController
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackControllerImplement @Inject constructor(
    val  locationServiceController: LocationServiceController,

): TrackController {
    private var _isTracking  = false
    override val isTracking: Boolean get() =  _isTracking
    override fun startTrack() {
        locationServiceController.start()
        _isTracking = true
    }

    override fun stopTrack() {
        locationServiceController.stop()
        _isTracking = false
    }
}