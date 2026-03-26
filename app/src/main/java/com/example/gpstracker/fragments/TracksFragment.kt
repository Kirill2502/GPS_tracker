package com.example.gpstracker.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpstracker.MainApp
import com.example.gpstracker.MainViewModel
import com.example.gpstracker.R
import com.example.gpstracker.data.TrackItemEntity
import com.example.gpstracker.databinding.FragmentMainBinding
import com.example.gpstracker.databinding.TracksBinding
import com.example.gpstracker.data.TrackAdapter
import com.example.gpstracker.utils.DialogManager
import com.example.gpstracker.utils.openFragment
import kotlin.getValue


class TracksFragment : Fragment(), TrackAdapter.Listener {
    lateinit var binding: TracksBinding
    private lateinit var adapter: TrackAdapter
    private val viewModel: MainViewModel by activityViewModels{
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TracksBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TrackAdapter(this@TracksFragment)
        binding.rcView.layoutManager = LinearLayoutManager(requireContext())
        binding.rcView.adapter = adapter

        getTracks()

    }
    private fun getTracks(){
        viewModel.tracks.observe(viewLifecycleOwner){
            adapter.submitList(it)
            binding.tvEmpty.visibility = if (it.isEmpty()) View.VISIBLE
            else View.GONE
        }
    }

    override fun onClick(track: TrackItemEntity, type: TrackAdapter.ClickType) {
        when(type){
            TrackAdapter.ClickType.DELETE->{
                DialogManager.showDeleteTrackDialog(requireContext(),object: DialogManager.Listener{
                    override fun onClick() {
                        viewModel.deleteTrack(track)
                    }

                } )
            }
            TrackAdapter.ClickType.OPEN -> {
                viewModel.trackUpdates.value = track
                openViewTrackFrag(track)
            }
        }


    }
    private fun openViewTrackFrag(track: TrackItemEntity){
        openFragment(ViewTrackFragment.newInstance())
    }


    companion object {

        @JvmStatic
        fun newInstance() = TracksFragment()

    }
}