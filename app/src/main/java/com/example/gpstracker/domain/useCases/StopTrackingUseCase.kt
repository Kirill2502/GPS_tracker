package com.example.gpstracker.domain.useCases

import com.example.gpstracker.domain.controller.TrackController

class StopTrackingUseCase(private val trackController: TrackController) {
    operator fun invoke(){
        trackController.stopTrack()
    }
}