package com.example.gpstracker.data.repository

import com.example.gpstracker.data.dataSource.LocationDataSource
import com.example.gpstracker.domain.models.LocationDataDomain
import com.example.gpstracker.domain.repository.ServRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServRepositoryImplement @Inject constructor(
    private val locationDataSource: LocationDataSource

    ): ServRepository {


    override fun getLocationUpdates(): Flow<LocationDataDomain> = locationDataSource.locationFlow



}