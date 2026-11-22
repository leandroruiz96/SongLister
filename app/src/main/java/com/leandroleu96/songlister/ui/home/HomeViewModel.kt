package com.leandroleu96.songlister.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.repository.GameSyncResult
import com.leandroleu96.songlister.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SongRepository,
    autoSyncOnInit: Boolean = true,
) : ViewModel() {

    private val isSyncingFlow = MutableStateFlow(false)
    private val syncResultsFlow = MutableStateFlow<Map<GameType, GameSyncResult>>(emptyMap())

    val uiState: StateFlow<HomeUiState> = combine(
        isSyncingFlow,
        repository.observeSongCount(GameType.ULTRASTAR),
        repository.observeSongCount(GameType.YARG),
        repository.observeSyncMetadata(),
        syncResultsFlow,
    ) { isSyncing, ultraStarCount, yargCount, metadataMap, syncResults ->
        val ultraStarMeta = metadataMap[GameType.ULTRASTAR]
        val yargMeta = metadataMap[GameType.YARG]
        val ultraStarResult = syncResults[GameType.ULTRASTAR]
        val yargResult = syncResults[GameType.YARG]

        HomeUiState(
            isSyncing = isSyncing,
            ultraStar = GameCardUiState(
                gameType = GameType.ULTRASTAR,
                songCount = ultraStarCount,
                lastSyncedAtMillis = ultraStarMeta?.lastSyncedAtMillis,
                syncStatus = ultraStarResult?.status,
                errorMessage = ultraStarResult?.errorMessage,
            ),
            yarg = GameCardUiState(
                gameType = GameType.YARG,
                songCount = yargCount,
                lastSyncedAtMillis = yargMeta?.lastSyncedAtMillis,
                syncStatus = yargResult?.status,
                errorMessage = yargResult?.errorMessage,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        if (autoSyncOnInit) {
            syncAll(forceRefresh = false)
        }
    }

    fun syncAll(forceRefresh: Boolean = false) {
        if (isSyncingFlow.value) return
        viewModelScope.launch {
            isSyncingFlow.value = true
            try {
                val results = repository.syncAll(forceRefresh = forceRefresh)
                syncResultsFlow.value = results
            } finally {
                isSyncingFlow.value = false
            }
        }
    }

    companion object {
        fun provideFactory(repository: SongRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository = repository) as T
                }
            }
    }
}
