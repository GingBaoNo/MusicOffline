package com.example.offmusic.ui.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.offmusic.ui.Activity.MainActivity
import com.example.offmusic.ui.Activity.PlayerActivity
import com.example.offmusic.data.Entity.Song
import com.example.offmusic.ui.adapter.SongAdapter
import com.example.offmusic.databinding.FragmentSongsBinding
import com.example.offmusic.model.MusicViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongsFragment : Fragment() {

    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MusicViewModel
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]

        setupRecyclerView()
        observeSongs()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onSongClick = { song ->
                val fullMusicList = viewModel.songListState.value

                val paths = ArrayList(fullMusicList.map { it.filePath })
                val titles = ArrayList(fullMusicList.map { it.title })
                val artists = ArrayList(fullMusicList.map { it.artist })

                val ids = ArrayList(fullMusicList.map { it.id.toString() })

                val clickedPosition = fullMusicList.indexOfFirst { it.id == song.id }

                val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                    putStringArrayListExtra("LIST_PATHS", paths)
                    putStringArrayListExtra("LIST_TITLES", titles)
                    putStringArrayListExtra("LIST_ARTISTS", artists)
                    putStringArrayListExtra("LIST_IDS", ids)
                    putExtra("CLICKED_POSITION", if (clickedPosition != -1) clickedPosition else 0)
                }
                startActivity(intent)
            },
            onFavClick = { song ->
                viewModel.onFavoriteClicked(song)
            },
            onSongLongClick = { selectedSong ->
                (activity as? MainActivity)?.showAddToPlaylistDialog(selectedSong)
            }
        )

        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.songListState.collectLatest { list ->
                songAdapter.submitList(list)
            }
        }
    }

    fun filterList(filteredList: List<Song>) {
        if (::songAdapter.isInitialized) {
            songAdapter.submitList(filteredList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}