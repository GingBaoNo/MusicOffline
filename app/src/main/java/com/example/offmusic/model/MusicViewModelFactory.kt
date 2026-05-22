package com.example.offmusic.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.offmusic.data.Repository.MusicRepository
import com.example.offmusic.model.MusicViewModel

class MusicViewModelFactory(private val repository: MusicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}