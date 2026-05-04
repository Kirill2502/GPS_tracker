package com.example.gpstracker.domain.useCases

import com.example.gpstracker.domain.models.TrackItemDomain
import com.example.gpstracker.domain.repository.DbRepository
import kotlinx.coroutines.flow.Flow

class GetTracksUseCase(private val repository: DbRepository) {
    operator fun invoke(): Flow<List<TrackItemDomain>>{
        return repository.getAllTracks()
    }
}