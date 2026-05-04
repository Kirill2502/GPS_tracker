package com.example.gpstracker.di.module

import android.content.Context
import androidx.room.Room
import com.example.gpstracker.data.repository.DbRepositoryImplement
import com.example.gpstracker.data.room.MainDb
import com.example.gpstracker.data.room.MyDao
import com.example.gpstracker.domain.repository.DbRepository
import com.example.gpstracker.domain.useCases.GetTrackByIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MainDb {
        return Room.databaseBuilder(
            context,
            MainDb::class.java,
            "GpsTracker.db"
        ).build()
    }

    @Provides
    fun provideDao(db: MainDb): MyDao {
        return db.getDao()
    }
    @Provides
    @Singleton
    fun provideRepository(dao: MyDao): DbRepository{
        return DbRepositoryImplement(dao)
    }

}