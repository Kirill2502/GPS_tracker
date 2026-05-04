package com.example.gpstracker.data.repository

import com.example.gpstracker.data.mappers.toDomain
import com.example.gpstracker.data.room.MyDao
import com.example.gpstracker.data.mappers.toEntity
import com.example.gpstracker.domain.models.TrackItemDomain
import com.example.gpstracker.domain.repository.DbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DbRepositoryImplement(val mainDao: MyDao): DbRepository {
    override suspend fun insertTrack(track: TrackItemDomain) {
        mainDao.insertTrack(track.toEntity())
    }

    override suspend fun updateTrack(track: TrackItemDomain) {
        mainDao.updateTrack(track.toEntity())
    }

    override suspend fun deleteTrack(track: TrackItemDomain) {
        mainDao.deleteTrack(track.toEntity())
    }

    override fun getAllTracks(): Flow<List<TrackItemDomain>> {
        return mainDao.getAllTracks().map {list->
            list.map { it.toDomain() }
        }
    }

}