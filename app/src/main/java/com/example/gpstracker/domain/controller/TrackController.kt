package com.example.gpstracker.domain.controller

interface TrackController {
     fun startTrack()
     fun stopTrack()
     val isTracking: Boolean
}