package com.leandroleu96.songlister.data.repository

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.local.entity.SyncMetadataEntity
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    suspend fun syncGame(gameType: GameType, forceRefresh: Boolean = false): GameSyncResult
    suspend fun syncAll(forceRefresh: Boolean = false): Map<GameType, GameSyncResult>

    fun observeSyncMetadata(): Flow<Map<GameType, SyncMetadataEntity>>
    fun observeSongCount(gameType: GameType): Flow<Int>

    fun observeUltraStarSongs(): Flow<List<UltraStarSongEntity>>
    fun observeYargSongs(): Flow<List<YargSongEntity>>

    suspend fun getUltraStarSong(id: Long): UltraStarSongEntity?
    suspend fun getYargSong(id: Long): YargSongEntity?
}
