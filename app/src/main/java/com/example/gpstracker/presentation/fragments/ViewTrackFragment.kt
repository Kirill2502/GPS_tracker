package com.example.gpstracker.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.gpstracker.data.mappers.toGeoPoints
import com.example.gpstracker.databinding.ViewTrackBinding
import com.example.gpstracker.domain.models.TrackItemDomain
import com.example.gpstracker.presentation.map.MapRender
import com.example.gpstracker.presentation.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

@AndroidEntryPoint
class ViewTrackFragment : Fragment() {

    private lateinit var binding: ViewTrackBinding
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var mapRender: MapRender
    private var startPoint: GeoPoint? = null
    private var trackId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackId = arguments?.getInt(ARG_TRACK_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapRender = MapRender(binding.mapView1)
        mapRender.initOSM(requireContext())

        loadTrack()

        binding.fMyPosition.setOnClickListener {
            startPoint?.let {
                mapRender.moveCamera(it)
            }
        }
    }

    private fun loadTrack() {
        trackId?.let { id ->
            viewLifecycleOwner.lifecycleScope.launch {
                val track = viewModel.getTrackById(id)
                if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)){
                    track?.let { displayTrack(it) }
                }

            }
        }
    }

    private fun displayTrack(track: TrackItemDomain) {
        // UI текст
        binding.tvDateVT.text = "Дата: ${track.date}"
        binding.tvTimeVT.text = track.time
        binding.tvSpeedVT.text = "Средняя скорость: ${track.velocity} км/ч"
        binding.tvDistanceVT.text = "Дистанция: ${track.distance} км"

        // Карта — вся логика отрисовки внутри MapRender
        val points = track.geoPoints.toGeoPoints()
        startPoint = mapRender.showRoute(requireContext(), points)
    }





    override fun onDestroyView() {
        if (::binding.isInitialized && this::mapRender.isInitialized) {
            mapRender.detach()
                //binding.mapView1.onDetach()
        }
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TRACK_ID = "track_id"

        @JvmStatic
        fun newInstance(trackId: Int?) = ViewTrackFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_TRACK_ID, trackId ?: -1)
            }
        }
    }
}