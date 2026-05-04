package com.example.gpstracker.di.module

import android.content.Context
import com.example.gpstracker.data.controllers.TrackControllerImplement
import com.example.gpstracker.data.dataSource.LocationDataSource
import com.example.gpstracker.data.repository.ServRepositoryImplement
import com.example.gpstracker.data.system.LocationServiceController
import com.example.gpstracker.domain.controller.TrackController
import com.example.gpstracker.domain.repository.ServRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideLocationServiceController( @ApplicationContext context: Context): LocationServiceController{
        return LocationServiceController(context)
    }
    @Provides
    @Singleton
    fun provideTrackController(locationServiceController: LocationServiceController): TrackController{
        return TrackControllerImplement(locationServiceController)
    }
    @Provides
    @Singleton
    fun provideServRepository(locationDataSource: LocationDataSource): ServRepository{
        return ServRepositoryImplement(locationDataSource)
    }
    @Provides
    @Singleton
    fun provideLocationDataSource(): LocationDataSource {
        return LocationDataSource()
    }


}