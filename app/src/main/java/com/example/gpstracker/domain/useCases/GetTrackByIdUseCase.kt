package com.example.gpstracker.domain.useCases

import com.example.gpstracker.domain.models.TrackItemDomain
import com.example.gpstracker.domain.repository.DbRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GetTrackByIdUseCase(private val repository: DbRepository) {

    suspend operator fun invoke(id: Int): TrackItemDomain? {
        return repository.getAllTracks()
            .map { tracks -> tracks.find { it.id == id } }
            .first()
    }
}
