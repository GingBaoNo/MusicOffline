package com.example.offmusic.data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val playlistId: Long=0,
    val name : String,
    val createAt :Long =System.currentTimeMillis()
)
