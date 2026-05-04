package com.example.gpstracker.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gpstracker.R
import com.example.gpstracker.databinding.TrackItemBinding
import com.example.gpstracker.domain.models.TrackItemDomain

class TrackAdapter(private val listener: Listener): ListAdapter<TrackItemDomain, TrackAdapter.TrackViewHolder>(DIFF_CALLBACK) {

    class TrackViewHolder(private val binding: TrackItemBinding, private val listener: Listener) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
            private lateinit var trackTemp: TrackItemDomain
            init {
                binding.imDelete.setOnClickListener(this)
                binding.item.setOnClickListener(this)
            }

        fun bind(track: TrackItemDomain) {
            trackTemp = track
            binding.tvDate.text = track.date
            binding.tvSpeedIcon.text = "Скорость: ${track.velocity} км/ч"
            binding.tvTimeIcon.text = "${track.time}"
            binding.tvDistanceItem.text = "Дистанция: ${track.distance} км"
        }

        override fun onClick(view: View?) {
            val type = when(view?.id){
                R.id.imDelete-> ClickType.DELETE

                R.id.item-> ClickType.OPEN

                else -> ClickType.OPEN
            }
            listener.onClick(trackTemp, type)
            binding.item.id
        }
    }
    interface Listener{
        fun onClick(track: TrackItemDomain, type: ClickType)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrackViewHolder {
        val binding = TrackItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding,listener)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    enum class ClickType{
        DELETE,
        OPEN
    }
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackItemDomain>() {
            override fun areItemsTheSame(oldItem: TrackItemDomain, newItem: TrackItemDomain): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TrackItemDomain, newItem: TrackItemDomain): Boolean {
                return oldItem == newItem
            }
        }
    }


}