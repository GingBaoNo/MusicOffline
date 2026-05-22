package com.example.offmusic.data.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.offmusic.data.Entity.Playlist
import com.example.offmusic.data.Entity.PlaylistSongCrossRef
import com.example.offmusic.data.Entity.PlaylistWithSongs
import com.example.offmusic.data.Entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewSongs(songs: List<Song>): List<Long>

    @Query("UPDATE songs SET title = :title, artist = :artist, album = :album, filePath = :path WHERE id = :id")
    suspend fun updateSongFileInfo(id: String, title: String, artist: String, album: String, path: String)

    @Delete
    suspend fun deleteSongs(songs: List<Song>)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsDirect(): List<Song>


    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("UPDATE songs SET isFavorite = :isFav WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: String, isFav: Boolean)

    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Delete
    suspend fun removeSongFromPlaylist(crossRef: PlaylistSongCrossRef)

    @Transaction
    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    fun getSongsFromPlaylist(playlistId: Long): Flow<PlaylistWithSongs>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(playlistSongCrossRef: PlaylistSongCrossRef)
}