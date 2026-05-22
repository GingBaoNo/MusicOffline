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
import com.example.offmusic.ui.Activity.PlaylistDetailActivity
import com.example.offmusic.ui.adapter.PlaylistAdapter
import com.example.offmusic.databinding.FragmentPlaylistsBinding
import com.example.offmusic.model.MusicViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    // Sử dụng backing property an toàn cho ViewBinding trong Fragment
    private val binding get() = _binding!!

    private lateinit var viewModel: MusicViewModel
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]

        setupRecyclerView()
        observePlaylists()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter { selectedPlaylist ->
            val intent = Intent(requireContext(), PlaylistDetailActivity::class.java).apply {
                putExtra("PLAYLIST_ID", selectedPlaylist.playlistId)
                putExtra("PLAYLIST_NAME", selectedPlaylist.name)
            }
            startActivity(intent)
        }

        binding.rvPlaylists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
            setHasFixedSize(true)
        }
    }

    private fun observePlaylists() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistState.collectLatest { playlists ->
                playlistAdapter.submitList(playlists)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Bắt buộc giải phóng binding tại đây để tránh hiện tượng Memory Leak khi đổi Tab
        _binding = null
    }
}