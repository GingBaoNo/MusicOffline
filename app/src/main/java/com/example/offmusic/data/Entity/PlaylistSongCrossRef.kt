package com.example.offmusic.data.Entity

import androidx.room.Entity

@Entity(tableName = "playlist_song_join",
    primaryKeys = ["playlistId","id"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val id: String
)
