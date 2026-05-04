package com.example.gpstracker.presentation.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.domain.controller.TrackController
import com.example.gpstracker.domain.models.TrackItemDomain
import com.example.gpstracker.domain.repository.ServRepository
import com.example.gpstracker.domain.useCases.CalculateAverageSpeedUseCase
import com.example.gpstracker.domain.useCases.DeleteTrackUseCase
import com.example.gpstracker.domain.useCases.GetTrackByIdUseCase
import com.example.gpstracker.domain.useCases.GetTracksUseCase
import com.example.gpstracker.domain.useCases.InsertTrackUseCase
import com.example.gpstracker.domain.useCases.StartTrackingUseCase
import com.example.gpstracker.domain.useCases.StopTrackingUseCase
import com.example.gpstracker.domain.useCases.UpdateTrackUseCase
import com.example.gpstracker.presentation.models.LocationModel
import com.example.gpstracker.domain.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val insertTrackUseCase: InsertTrackUseCase,
    private val getTrackByIdUseCase: GetTrackByIdUseCase,
    private val getTracksUseCase: GetTracksUseCase,
    private val deleteTrackUseCase: DeleteTrackUseCase,
    private val updateTrackUseCase: UpdateTrackUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val calculateAverageSpeedUseCase: CalculateAverageSpeedUseCase,
    private val servRepository: ServRepository,
    private val trackController: TrackController
) : ViewModel() {

    // -------------------------------
    // 📡  LiveData
    // -------------------------------

    // Данные для UI (используем существующий LocationModel)
    private val _locationData = MutableLiveData<LocationModel>()
    val locationData: LiveData<LocationModel> = _locationData

    // Время трекинга
    private val _trackingTime = MutableLiveData("Время: 00:00:00")
    val trackingTime: LiveData<String> = _trackingTime

    // Состояние трекинга
    private val _isTracking = MutableLiveData(false)
    val isTracking: LiveData<Boolean> = _isTracking

    // Одноразовые события
    private val _events = MutableLiveData<Event>()
    val events: LiveData<Event> = _events

    // -------------------------------
    // 📍 ПОЛУЧИТЬ ТРЕК ПО ID
    // -------------------------------

    suspend fun getTrackById(id: Int): TrackItemDomain? {
        return getTrackByIdUseCase(id)
    }

    // -------------------------------
    // 🧠 внутреннее состояние
    // -------------------------------
    private var timerJob: Job? = null
    private var startTime: Long = 0L
    private var geoPointsList = ArrayList<GeoPoint>()  // ← храним точки

    // -------------------------------
    // 📦 события
    // -------------------------------

    sealed class Event {
        object TrackSaved : Event()
        object TrackingStarted : Event()
        object TrackingStopped : Event()
        data class ShowSaveDialog(
            val time: String,
            val speed: String,
            val distance: String
        ) : Event()
        data class Error(val message: String) : Event()
    }
    val tracks = getTracksUseCase()

    // -------------------------------
    // 🔄 подписка на данные из репозитория
    // -------------------------------

    init {
        viewModelScope.launch {
            servRepository.getLocationUpdates().collect { locationData ->
                Log.d("MyLog", "Received: ${locationData.distance}, ${locationData.velocity}")


                // Обновляем список точек (нужно восстановить из строки)
                geoPointsList = ArrayList(locationData.geoPointsString.toGeoPoints())

                val avgSpeed = calculateAverageSpeedUseCase(
                    distanceMeters = locationData.distance,
                    elapsedMillis = getElapsedTime()
                )

                _locationData.postValue(
                    LocationModel(
                        velocity = locationData.velocity,
                        distance = locationData.distance,
                        averageSpeed = String.format("%.1f", avgSpeed),
                        geoPointsList = geoPointsList
                    )
                )
            }
        }
    }

    // -------------------------------
    // 🔧 конвертация строки в GeoPoint
    // -------------------------------

    private fun String.toGeoPoints(): List<GeoPoint> {
        if (isEmpty()) return emptyList()
        return split("/")
            .filter { it.isNotEmpty() }
            .map {
                val parts = it.split(",")
                GeoPoint(parts[0].toDouble(), parts[1].toDouble())
            }
    }

    // -------------------------------
    // 💾 DB
    // -------------------------------
    

    fun deleteTrack(trackItem: TrackItemDomain) = viewModelScope.launch {
        deleteTrackUseCase(trackItem)
    }


    // -------------------------------
    // ⏱ TIMER
    // -------------------------------

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                _trackingTime.postValue("Время: ${TimeUtils.getTime(elapsed)}")
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // -------------------------------
    // 📍 РАСЧЕТ ВРЕМЕНИ ДЛЯ СРЕДНЕЙ СКОРОСТИ
    // -------------------------------

    fun getElapsedTime(): Long {
        return if (trackController.isTracking) {
            System.currentTimeMillis() - startTime
        } else {
            0L
        }
    }

    // -------------------------------
    // 🚀 START / STOP
    // -------------------------------

    fun onStartStopClicked() {
        if (trackController.isTracking) {  // ← используем trackController, НЕ servRepository
            val currentData = _locationData.value
            if (currentData != null) {
                val elapsed = System.currentTimeMillis() - startTime
                val timeString = TimeUtils.getTime(elapsed)
                _events.value = Event.ShowSaveDialog(
                    time = timeString,
                    speed = currentData.averageSpeed,   // строка, уже отформатирована
                    distance = currentData.distance.toString()    // строка
                )
            } else {
                _events.value = Event.Error("Нет данных о маршруте")
            }
        } else {
            startTracking()
        }
    }

    // Функция для подтверждения сохранения из фрагмента
    fun onSaveDialogConfirmed() {
        saveCurrentTrack()
        stopTracking()
    }

    fun startTracking() {
        if (trackController.isTracking) return

        startTrackingUseCase.invoke()
        startTime = System.currentTimeMillis()
        startTimer()
        _isTracking.value = true
        _events.value = Event.TrackingStarted
    }

    fun stopTracking() {
        if (!trackController.isTracking) return

        stopTrackingUseCase.invoke()
        stopTimer()
        _isTracking.value = false
        _events.value = Event.TrackingStopped
    }

    // -------------------------------
    // 💾 сохранение текущего трека
    // -------------------------------

    fun saveCurrentTrack() {
        val currentData = _locationData.value ?: return

        val track = TrackItemDomain(
            id = null,
            time = TimeUtils.getTime(System.currentTimeMillis() - startTime),
            date = TimeUtils.getDate(),
            distance = currentData.distance.toString(),
            velocity = currentData.averageSpeed,
            geoPoints = geoPointsListToGeoPointsString(currentData.geoPointsList)
        )

        viewModelScope.launch {
            try {
                insertTrackUseCase(track)
                _events.value = Event.TrackSaved
            } catch (e: Exception) {
                _events.value = Event.Error(e.message ?: "Ошибка сохранения")
            }
        }
    }


    private fun geoPointsListToGeoPointsString(list: ArrayList<GeoPoint>): String {
        val sb = StringBuilder()
        list.forEach {
            sb.append("${it.latitude},${it.longitude}/")
        }
        return sb.toString()
    }


    // -------------------------------
    // 📍 состояние трекинга
    // -------------------------------

    fun isTrackingActive(): Boolean = trackController.isTracking

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}