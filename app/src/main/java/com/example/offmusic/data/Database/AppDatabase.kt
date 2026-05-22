package com.example.offmusic.data.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.offmusic.data.DAO.MusicDao
import com.example.offmusic.data.Entity.Playlist
import com.example.offmusic.data.Entity.PlaylistSongCrossRef
import com.example.offmusic.data.Entity.Song

@Database(
    entities = [Song::class, Playlist::class, PlaylistSongCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "music_database"
                )

                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}