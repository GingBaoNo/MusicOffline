package com.example.offmusic.ui.adapter

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.offmusic.data.Entity.Song
import com.example.offmusic.databinding.ItemSongBinding

class SongAdapter(
    private val onSongClick: (Song) -> Unit,
    private val onFavClick: (Song) -> Unit,
    private val onSongLongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, onSongClick, onFavClick, onSongLongClick)
    }

    class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            song: Song,
            onSongClick: (Song) -> Unit,
            onFavClick: (Song) -> Unit,
            onSongLongClick: (Song) -> Unit
        ) {
            binding.tvSongTitle.text = song.title
            binding.tvArtist.text = song.artist

            if (song.isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.btn_star_big_on)
            } else {
                binding.btnFavorite.setImageResource(R.drawable.btn_star_big_off)
            }

            binding.root.setOnClickListener {
                onSongClick(song)
            }

            binding.btnFavorite.setOnClickListener {
                onFavClick(song)
            }

            binding.root.setOnLongClickListener {
                onSongLongClick(song)
                true
            }
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem == newItem
    }
}