package com.example.gpstracker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.data.MainDb
import com.example.gpstracker.data.TrackItemEntity
import com.example.gpstracker.location.LocationModel
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class MainViewModel(db: MainDb): ViewModel() {
    val locationUpdates = MutableLiveData<LocationModel>()
    val trackUpdates = MutableLiveData<TrackItemEntity>()
    val timeData = MutableLiveData<String>()
    val dao = db.getDao()
    val tracks = dao.getAllTracks().asLiveData()
    fun insertTrack(trackItem: TrackItemEntity) = viewModelScope.launch {
        dao.insertTrack(trackItem)
    }
    fun deleteTrack(trackItem: TrackItemEntity) = viewModelScope.launch {
        dao.deleteTrack(trackItem)
    }




    class ViewModelFactory(private val db: MainDb): ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)){
                return MainViewModel(db) as T
            }
            throw IllegalArgumentException("Неизвестный viewModel класс")


        }
    }





}