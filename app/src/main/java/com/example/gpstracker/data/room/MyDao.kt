package com.example.gpstracker.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gpstracker.data.room.TrackItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyDao {
    // Вставка (можно использовать suspend для корутин)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertTrack(trackItem: TrackItemEntity): Long

    // Обновление
    @Update
    suspend fun updateTrack(trackItem: TrackItemEntity)

    // Удаление
    @Delete
    suspend fun deleteTrack(trackItem: TrackItemEntity)

    // Получение всех записей
    @Query("SELECT * FROM tracks")
    fun getAllTracks(): Flow<List<TrackItemEntity>>

}