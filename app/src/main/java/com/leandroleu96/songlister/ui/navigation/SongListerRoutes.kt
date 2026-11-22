package com.leandroleu96.songlister.ui.navigation

import com.leandroleu96.songlister.core.GameType
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class SongListRoute(
    val gameType: GameType,
)

@Serializable
data class SongDetailRoute(
    val gameType: GameType,
    val songId: Long,
)
