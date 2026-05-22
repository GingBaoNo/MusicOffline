package com.example.offmusic.data.Repository

import com.example.offmusic.data.DAO.MusicDao
import com.example.offmusic.data.Entity.Playlist
import com.example.offmusic.data.Entity.PlaylistSongCrossRef
import com.example.offmusic.data.Entity.PlaylistWithSongs
import com.example.offmusic.data.Entity.Song
import com.example.offmusic.utils.MusicScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(
    private val musicDao: MusicDao,
    private val musicScanner: MusicScanner
) {
    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = musicDao.getFavoriteSongs()

    suspend fun refreshMusicLibrary() = withContext(Dispatchers.IO) {
        val songsOnDevice = musicScanner.scanStorageForMp3()

        musicDao.insertNewSongs(songsOnDevice)

        songsOnDevice.forEach { song ->
            musicDao.updateSongFileInfo(song.id, song.title, song.artist, song.album, song.filePath)
        }

        val songsInRoom = musicDao.getAllSongsDirect()
        val deletedSongs = songsInRoom.filter { roomSong ->
            songsOnDevice.none { deviceSong -> deviceSong.id == roomSong.id }
        }
        if (deletedSongs.isNotEmpty()) {
            musicDao.deleteSongs(deletedSongs)
        }
    }

    suspend fun toggleFavorite(songId: String, isFavorite: Boolean) {
        musicDao.updateFavoriteStatus(songId, isFavorite)
    }
    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    suspend fun addSongToPlaylist(songId: String, playlistId: Long) {
        val crossRef = PlaylistSongCrossRef(playlistId = playlistId, id = songId)
        musicDao.addSongToPlaylist(crossRef)
    }

    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef) {
        musicDao.addSongToPlaylist(crossRef)
    }

    suspend fun createNewPlaylist(name: String) {
        val newPlaylist = Playlist(name = name)
        musicDao.createPlaylist(newPlaylist)
    }
    fun getSongsOfPlaylist(playlistId: Long): Flow<PlaylistWithSongs?> {
        return musicDao.getSongsFromPlaylist(playlistId)
    }

}