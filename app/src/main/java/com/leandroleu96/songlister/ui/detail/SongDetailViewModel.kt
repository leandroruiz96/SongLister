package com.leandroleu96.songlister.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SongDetailViewModel(
    private val gameType: GameType,
    private val songId: Long,
    private val repository: SongRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SongDetailUiState>(SongDetailUiState.Loading)
    val uiState: StateFlow<SongDetailUiState> = _uiState.asStateFlow()

    init {
        loadSong()
    }

    private fun loadSong() {
        viewModelScope.launch {
            _uiState.value = when (gameType) {
                GameType.ULTRASTAR -> {
                    val song = repository.getUltraStarSong(songId)
                    if (song != null) SongDetailUiState.UltraStarLoaded(song) else SongDetailUiState.NotFound
                }

                GameType.YARG -> {
                    val song = repository.getYargSong(songId)
                    if (song != null) SongDetailUiState.YargLoaded(song) else SongDetailUiState.NotFound
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            gameType: GameType,
            songId: Long,
            repository: SongRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SongDetailViewModel(
                    gameType = gameType,
                    songId = songId,
                    repository = repository,
                ) as T
            }
        }
    }
}
