package com.example.offmusic.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offmusic.data.Entity.Playlist
import com.example.offmusic.data.Entity.PlaylistSongCrossRef
import com.example.offmusic.data.Entity.PlaylistWithSongs
import com.example.offmusic.data.Entity.Song
import com.example.offmusic.data.Repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    // Chuyển đổi Flow thành StateFlow để UI dễ dàng quan sát (Observe)
    val songListState: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun scanMusic() {
        viewModelScope.launch {
            repository.refreshMusicLibrary()
        }
    }

    // Đưa danh sách Playlist thành StateFlow để UI lắng nghe continuous cập nhật
    val playlistState: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Hàm hỗ trợ tạo nhanh Playlist mới thẳng từ hộp thoại nhập tên
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createNewPlaylist(name)
        }
    }

    fun getSongsOfPlaylist(playlistId: Long): Flow<PlaylistWithSongs?> {
        return repository.getSongsOfPlaylist(playlistId)
    }

    fun onFavoriteClicked(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, !song.isFavorite)
        }
    }

    fun addSongToPlaylist(songId: String, playlistId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val crossRef = PlaylistSongCrossRef(playlistId = playlistId, id = songId)
            repository.addSongToPlaylist(crossRef)
        }
    }
}