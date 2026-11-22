package com.leandroleu96.songlister.ui.detail

import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity

sealed interface SongDetailUiState {
    data object Loading : SongDetailUiState
    data object NotFound : SongDetailUiState
    data class UltraStarLoaded(val song: UltraStarSongEntity) : SongDetailUiState
    data class YargLoaded(val song: YargSongEntity) : SongDetailUiState
}
