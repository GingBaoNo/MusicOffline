package com.example.offmusic.ui.Activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.offmusic.data.Database.AppDatabase
import com.example.offmusic.data.Entity.Song
import com.example.offmusic.data.Entity.Playlist
import com.example.offmusic.ui.Fragment.PlaylistsFragment
import com.example.offmusic.ui.Fragment.SongsFragment
import com.example.offmusic.R
import com.example.offmusic.data.Repository.MusicRepository
import com.example.offmusic.ViewModel.MusicViewModelFactory
import com.example.offmusic.databinding.ActivityMainBinding
import com.example.offmusic.model.MusicViewModel
import com.example.offmusic.utils.MusicScanner
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MusicViewModel

    private val songsFragment = SongsFragment()
    private val playlistsFragment = PlaylistsFragment()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.scanMusic()
        } else {
            Toast.makeText(this, "Ứng dụng cần quyền bộ nhớ để quét nhạc!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Database và ViewModel dùng chung cho toàn bộ Fragment con
        val database = AppDatabase.getDatabase(this)
        val scanner = MusicScanner(applicationContext)
        val repository = MusicRepository(database.musicDao(), scanner)
        viewModel = ViewModelProvider(this, MusicViewModelFactory(repository))[MusicViewModel::class.java]

        replaceFragment(songsFragment)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_songs -> {
                    replaceFragment(songsFragment)
                    true
                }
                R.id.nav_playlists -> {
                    replaceFragment(playlistsFragment)
                    true
                }
                else -> false
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                val filteredList = viewModel.songListState.value.filter {
                    it.title.lowercase().contains(query) || it.artist.lowercase().contains(query)
                }
                songsFragment.filterList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        checkStoragePermission()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun checkStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.scanMusic()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    fun showAddToPlaylistDialog(song: Song) {
        val currentPlaylists = viewModel.playlistState.value
        val playlistNames = currentPlaylists.map { it.name }.toMutableList()
        playlistNames.add("➕ Tạo Playlist mới...")

        AlertDialog.Builder(this)
            .setTitle("Thêm vào danh sách phát")
            .setItems(playlistNames.toTypedArray()) { _, which ->
                if (which == playlistNames.size - 1) {
                    showCreatePlaylistDialog(song)
                } else {
                    val chosenPlaylist = currentPlaylists[which]
                    viewModel.addSongToPlaylist(song.id, chosenPlaylist.playlistId)
                    Toast.makeText(this, "Đã thêm vào Playlist [${chosenPlaylist.name}]", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showCreatePlaylistDialog(song: Song) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tạo Playlist mới")

        val input = EditText(this).apply {
            hint = "Nhập tên Playlist..."
            setSingleLine()
        }

        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            leftMargin = 50
            rightMargin = 50
        }
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Tạo & Thêm") { dialog, _ ->
            val playlistName = input.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                lifecycleScope.launch {
                    val newPlaylist = Playlist(name = playlistName)
                    val database = AppDatabase.getDatabase(applicationContext)
                    val newId = database.musicDao().createPlaylist(newPlaylist)

                    viewModel.addSongToPlaylist(song.id, newId)
                    Toast.makeText(applicationContext, "Đã tạo playlist '$playlistName' thành công!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Tên không được để trống!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}