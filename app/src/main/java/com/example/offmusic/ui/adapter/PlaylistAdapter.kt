package com.example.offmusic.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.offmusic.data.Entity.Playlist
import com.example.offmusic.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist, onPlaylistClick)
    }

    class PlaylistViewHolder(private val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            playlist: Playlist,
            onPlaylistClick: (Playlist) -> Unit
        ) {
            binding.tvPlaylistName.text = playlist.name
            binding.root.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean = oldItem.playlistId == newItem.playlistId
        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean = oldItem == newItem
    }
}