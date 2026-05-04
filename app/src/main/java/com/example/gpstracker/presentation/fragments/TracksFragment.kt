package com.example.gpstracker.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpstracker.databinding.TracksBinding
import com.example.gpstracker.domain.models.TrackItemDomain
import com.example.gpstracker.presentation.adapters.TrackAdapter
import com.example.gpstracker.presentation.utils.DialogManager
import com.example.gpstracker.presentation.utils.openFragment
import com.example.gpstracker.presentation.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TracksFragment : Fragment(), TrackAdapter.Listener {

    lateinit var binding: TracksBinding
    private lateinit var adapter: TrackAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TrackAdapter(this@TracksFragment)
        binding.rcView.layoutManager = LinearLayoutManager(requireContext())
        binding.rcView.adapter = adapter

        getTracks()
    }

    private fun getTracks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tracks.collect { tracks ->
                    adapter.submitList(tracks)
                    binding.tvEmpty.visibility = if (tracks.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onClick(track: TrackItemDomain, type: TrackAdapter.ClickType) {
        when (type) {
            TrackAdapter.ClickType.DELETE -> {
                DialogManager.showDeleteTrackDialog(requireContext(),
                    object : DialogManager.Listener {
                        override fun onClick() {
                            viewModel.deleteTrack(track)
                        }
                    })
            }
            TrackAdapter.ClickType.OPEN -> {
                // Открываем фрагмент с деталями трека, передаем id трека
                openViewTrackFrag(track.id)
            }
        }
    }

    private fun openViewTrackFrag(trackId: Int?) {
        // Передаем id трека через Bundle
        val fragment = ViewTrackFragment.newInstance(trackId)
        openFragment(fragment)
    }

    companion object {
        @JvmStatic
        fun newInstance() = TracksFragment()
    }
}