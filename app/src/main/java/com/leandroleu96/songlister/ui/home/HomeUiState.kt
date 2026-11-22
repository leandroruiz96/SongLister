package com.leandroleu96.songlister.ui.home

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.repository.GameSyncStatus

data class GameCardUiState(
    val gameType: GameType,
    val songCount: Int = 0,
    val lastSyncedAtMillis: Long? = null,
    val syncStatus: GameSyncStatus? = null,
    val errorMessage: String? = null,
)

data class HomeUiState(
    val isSyncing: Boolean = false,
    val ultraStar: GameCardUiState = GameCardUiState(GameType.ULTRASTAR),
    val yarg: GameCardUiState = GameCardUiState(GameType.YARG),
) {
    val hasAnyError: Boolean
        get() = ultraStar.syncStatus == GameSyncStatus.ERROR || yarg.syncStatus == GameSyncStatus.ERROR

    val isOfflineCached: Boolean
        get() = ultraStar.syncStatus == GameSyncStatus.OFFLINE_CACHED ||
            yarg.syncStatus == GameSyncStatus.OFFLINE_CACHED
}
