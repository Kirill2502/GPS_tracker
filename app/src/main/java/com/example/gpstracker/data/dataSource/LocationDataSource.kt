package com.example.gpstracker.data.dataSource

import com.example.gpstracker.domain.models.LocationDataDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDataSource @Inject constructor() {

    private val _locationFlow = MutableStateFlow(
        LocationDataDomain(0f, 0f, 0f, "")
    )

    val locationFlow: Flow<LocationDataDomain> = _locationFlow.asStateFlow()

     fun emitLocation(velocity: Float, distance: Float, averageSpeed: Float, geoPointsString: String) {
        _locationFlow.value = LocationDataDomain(velocity, distance, averageSpeed, geoPointsString)
    }
}