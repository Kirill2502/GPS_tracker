package com.example.gpstracker.domain.repository

import com.example.gpstracker.domain.models.TrackItemDomain
import kotlinx.coroutines.flow.Flow

interface DbRepository {
    suspend fun insertTrack(track: TrackItemDomain)

    suspend fun updateTrack(track: TrackItemDomain)

    suspend fun deleteTrack(track: TrackItemDomain)

    fun getAllTracks(): Flow<List<TrackItemDomain>>

}