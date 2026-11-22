package com.leandroleu96.songlister.data.repository

import com.leandroleu96.songlister.core.GameType

enum class GameSyncStatus {
    UPDATED,
    UP_TO_DATE,
    OFFLINE_CACHED,
    ERROR,
}

data class GameSyncResult(
    val gameType: GameType,
    val status: GameSyncStatus,
    val songCount: Int,
    val errorMessage: String? = null,
)
