package com.example.gpstracker.di.module

import com.example.gpstracker.domain.controller.TrackController
import com.example.gpstracker.domain.repository.DbRepository
import com.example.gpstracker.domain.repository.ServRepository
import com.example.gpstracker.domain.useCases.CalculateAverageSpeedUseCase
import com.example.gpstracker.domain.useCases.DeleteTrackUseCase
import com.example.gpstracker.domain.useCases.GetTrackByIdUseCase
import com.example.gpstracker.domain.useCases.GetTracksUseCase
import com.example.gpstracker.domain.useCases.InsertTrackUseCase
import com.example.gpstracker.domain.useCases.StartTrackingUseCase
import com.example.gpstracker.domain.useCases.StopTrackingUseCase
import com.example.gpstracker.domain.useCases.UpdateTrackUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideInsertTrackUseCase(repository: DbRepository): InsertTrackUseCase {
        return InsertTrackUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteTrackUseCase(repository: DbRepository): DeleteTrackUseCase {
        return DeleteTrackUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateTrackUseCase(repository: DbRepository): UpdateTrackUseCase {
        return UpdateTrackUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetTracksUseCase(repository: DbRepository): GetTracksUseCase {
        return GetTracksUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetTrackByIdUseCase(repository: DbRepository): GetTrackByIdUseCase {
        return GetTrackByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideStartTrackingUseCase(trackController: TrackController): StartTrackingUseCase {
        return StartTrackingUseCase(trackController)
    }

    @Provides
    @Singleton
    fun provideStopTrackingUseCase(trackController: TrackController): StopTrackingUseCase {
        return StopTrackingUseCase(trackController)
    }

    @Provides
    @Singleton
    fun provideCalculateAverageSpeedUseCase(): CalculateAverageSpeedUseCase {
        return CalculateAverageSpeedUseCase()
    }
    
}