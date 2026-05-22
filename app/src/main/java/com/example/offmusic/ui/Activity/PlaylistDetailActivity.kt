package com.example.offmusic.ui.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.offmusic.data.Database.AppDatabase
import com.example.offmusic.data.Repository.MusicRepository
import com.example.offmusic.ViewModel.MusicViewModelFactory
import com.example.offmusic.ui.adapter.SongAdapter
import com.example.offmusic.databinding.ActivityPlaylistDetailBinding
import com.example.offmusic.model.MusicViewModel
import com.example.offmusic.utils.MusicScanner
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistDetailBinding
    private lateinit var viewModel: MusicViewModel
    private lateinit var songAdapter: SongAdapter
    private var playlistId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlaylistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playlistId = intent.getLongExtra("PLAYLIST_ID", -1)
        val playlistName = intent.getStringExtra("PLAYLIST_NAME") ?: "Chi tiết Playlist"
        binding.tvPlaylistDetailTitle.text = playlistName

        val database = AppDatabase.getDatabase(this)
        val scanner = MusicScanner(applicationContext)
        val repository = MusicRepository(database.musicDao(), scanner)
        viewModel = ViewModelProvider(this, MusicViewModelFactory(repository))[MusicViewModel::class.java]

        setupRecyclerView()
        observePlaylistSongs()

        binding.btnBackFromDetail.setOnClickListener { finish() }

        binding.fabAddSongToThisPlaylist.setOnClickListener {
            showSongsSelectionDialog()
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                val currentSongList = songAdapter.currentList
                val paths = ArrayList(currentSongList.map { it.filePath })
                val titles = ArrayList(currentSongList.map { it.title })
                val artists = ArrayList(currentSongList.map { it.artist })
                val ids = ArrayList(currentSongList.map { it.id })
                val clickedPosition = currentSongList.indexOf(song)

                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putStringArrayListExtra("LIST_PATHS", paths)
                    putStringArrayListExtra("LIST_TITLES", titles)
                    putStringArrayListExtra("LIST_ARTISTS", artists)
                    putStringArrayListExtra("LIST_IDS", ids)
                    putExtra("CLICKED_POSITION", clickedPosition)
                }
                startActivity(intent)
            },
            onFavClick = { song -> viewModel.onFavoriteClicked(song) },
            onSongLongClick = {  }
        )

        binding.rvPlaylistSongs.layoutManager = LinearLayoutManager(this)
        binding.rvPlaylistSongs.adapter = songAdapter
    }

    private fun observePlaylistSongs() {
        lifecycleScope.launch {
            viewModel.getSongsOfPlaylist(playlistId).collectLatest { playlistWithSongs ->
                playlistWithSongs?.let {
                    songAdapter.submitList(it.songs)
                }
            }
        }
    }

    private fun showSongsSelectionDialog() {
        // Lấy tất cả kho bài hát offline trong máy ra để người dùng chọn
        val allSongs = viewModel.songListState.value
        if (allSongs.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy bài hát nào trong máy để thêm!", Toast.LENGTH_SHORT).show()
            return
        }

        val songTitles = allSongs.map { "${it.title} - ${it.artist}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Chọn bài hát muốn thêm")
            .setItems(songTitles) { _, which ->
                val selectedSong = allSongs[which]
                viewModel.addSongToPlaylist(selectedSong.id, playlistId)
                Toast.makeText(this, "Đã thêm '${selectedSong.title}' vào playlist!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}