package com.example.gpstracker.domain.repository

import com.example.gpstracker.domain.models.LocationDataDomain
import kotlinx.coroutines.flow.Flow

interface ServRepository {

    fun getLocationUpdates(): Flow<LocationDataDomain>

    //val isTracking: Boolean
}