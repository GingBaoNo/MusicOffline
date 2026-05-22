package com.example.offmusic.data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long = 0L,
    val filePath: String,
    val isFavorite : Boolean = false,
    val dateAdd : Long = System.currentTimeMillis()
)
