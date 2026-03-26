package com.example.gpstracker.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.example.gpstracker.MainApp
import com.example.gpstracker.MainViewModel
import com.example.gpstracker.databinding.ViewTrackBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


class ViewTrackFragment : Fragment() {
    lateinit var binding: ViewTrackBinding
    private val viewModel: MainViewModel by activityViewModels{
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)

    }
    private var startPoint: GeoPoint? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        settingsOsm()
        binding = ViewTrackBinding.inflate(inflater,container,false)
        return binding.root


    }
    private fun settingsOsm(){
        Configuration.getInstance().load(
            requireContext(),
            // Важно: сохраните настройки кеша в своем файле preferences
            requireContext().getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )

        Configuration.getInstance().userAgentValue = BuildConfig.BUILD_TYPE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTrack()
        binding.fMyPosition.setOnClickListener {
            if (startPoint!=null){
                binding.mapView1.controller.animateTo(startPoint)

            }
        }

    }
    @SuppressLint("UseKtx")
    private fun getTrack() = with(binding){
        viewModel.trackUpdates.observe(viewLifecycleOwner){
            tvDateVT.text = "Дата: ${it.date}"
            tvTimeVT.text = " ${it.time}"
            tvSpeedVT.text = "Средняя скорость: ${it.velocity} км/ч"
            tvDistanceVT.text = "Дистанция: : ${it.distance} км"
            val polyline = getPolyline(it.geoPoints)
            mapView1.overlays.add(polyline)
            mapView1.setMultiTouchControls(true)
            goToStartPosition(polyline.actualPoints[0])
            polyline.outlinePaint.color = Color.parseColor(
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("color_key","#14347C")
            )
            addStartMarker(polyline.actualPoints[0])
            addEndMarker(polyline.actualPoints.last())
            startPoint = polyline.actualPoints[0]


        }
    }
    //ФУНКЦИЯ ДЛЯ ДОБАВЛЕНИЯ ЗУМА НА СТАРТОВУЮ ТОЧКУ МАРШРУТА
    private fun goToStartPosition(startPosition: GeoPoint){
        binding.mapView1.controller.zoomTo(18.0)
        binding.mapView1.controller.animateTo(startPosition)
    }
    //ФУНКЦИЯ ДЛЯ ПОЛУЧЕНИЯ ТОЧЕК ИЗ СТРОКИ И ДОБАВЛЕНИЯ ИХ В МАРШРУТ
    private fun getPolyline(geoPoints: String): Polyline{
        val polyline = Polyline()
        val list = geoPoints.split("/")
        list.forEach{
            if (it.isEmpty()) return@forEach
            val points = it.split(",")
            polyline.addPoint(GeoPoint(points[0].toDouble(),points[1].toDouble()))

        }
        return polyline
    }
    //ФУНКЦИЯ ДЛЯ ДОБАВЛЕНИЯ СТАРТОВОГО МАРКЕРА НА КАРТЕ
    private fun addStartMarker(startPoint: GeoPoint) =with(binding) {
        val marker = Marker(mapView1)
        marker.position = startPoint
        marker.title = "Начало маршрута"
        marker.snippet = "Точка старта"
        marker.isDraggable = false // нельзя перемещать

        // Можно задать иконку (по умолчанию — стандартная)
        // marker.icon = ContextCompat.getDrawable(this, R.drawable.ic_start_marker)

        mapView1.overlays.add(marker)
        mapView1.invalidate() // Перерисовываем карту
    }
    private fun addEndMarker(endPoint: GeoPoint){
        binding.apply {
        val marker = Marker(mapView1)
        marker.position = endPoint
        marker.title = "Конец маршрута"
        marker.snippet = "Финальная точка"
        marker.isDraggable = false

        // Можно задать другую иконку для конца
        // marker.icon = ContextCompat.getDrawable(this, R.drawable.ic_end_marker)
        mapView1.overlays.add(marker)
        mapView1.invalidate()
        }
    }


    override fun onResume() {
        super.onResume()
        binding.mapView1.onResume()
    }
    override fun onPause() {
        super.onPause()
        binding.mapView1.onPause()
    }


    companion object {

        @JvmStatic
        fun newInstance() = ViewTrackFragment()

    }
}