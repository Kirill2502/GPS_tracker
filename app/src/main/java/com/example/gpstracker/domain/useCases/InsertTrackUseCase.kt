package com.example.gpstracker.domain.useCases

import com.example.gpstracker.domain.models.TrackItemDomain
import com.example.gpstracker.domain.repository.DbRepository

class InsertTrackUseCase(private val repository: DbRepository) {
    suspend operator fun invoke(track: TrackItemDomain) {
        repository.insertTrack(track)
    }
}