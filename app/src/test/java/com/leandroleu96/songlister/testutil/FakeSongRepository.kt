package com.leandroleu96.songlister.testutil

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.local.entity.SyncMetadataEntity
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import com.leandroleu96.songlister.data.repository.GameSyncResult
import com.leandroleu96.songlister.data.repository.GameSyncStatus
import com.leandroleu96.songlister.data.repository.SongRepository
import com.leandroleu96.songlister.data.repository.UltraStarFilter
import com.leandroleu96.songlister.data.repository.UltraStarFilterOptions
import com.leandroleu96.songlister.data.repository.YargFilter
import com.leandroleu96.songlister.data.repository.YargFilterOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSongRepository(
    var nextSyncResults: Map<GameType, GameSyncResult> = mapOf(
        GameType.ULTRASTAR to GameSyncResult(GameType.ULTRASTAR, GameSyncStatus.UP_TO_DATE, 0),
        GameType.YARG to GameSyncResult(GameType.YARG, GameSyncStatus.UP_TO_DATE, 0),
    ),
    initialUltraStarSongs: List<UltraStarSongEntity> = emptyList(),
    initialYargSongs: List<YargSongEntity> = emptyList(),
) : SongRepository {

    val syncAllCalls = mutableListOf<Boolean>()
    val syncGameCalls = mutableListOf<Pair<GameType, Boolean>>()

    private val ultraStarSongs = MutableStateFlow(initialUltraStarSongs)
    private val yargSongs = MutableStateFlow(initialYargSongs)
    private val ultraStarCount = MutableStateFlow(initialUltraStarSongs.size)
    private val yargCount = MutableStateFlow(initialYargSongs.size)
    private val metadata = MutableStateFlow<Map<GameType, SyncMetadataEntity>>(emptyMap())

    override suspend fun syncGame(gameType: GameType, forceRefresh: Boolean): GameSyncResult {
        syncGameCalls.add(gameType to forceRefresh)
        return nextSyncResults.getValue(gameType)
    }

    override suspend fun syncAll(forceRefresh: Boolean): Map<GameType, GameSyncResult> {
        syncAllCalls.add(forceRefresh)
        nextSyncResults[GameType.ULTRASTAR]?.let { ultraStarCount.value = it.songCount }
        nextSyncResults[GameType.YARG]?.let { yargCount.value = it.songCount }
        return nextSyncResults
    }

    override fun observeSyncMetadata(): Flow<Map<GameType, SyncMetadataEntity>> = metadata

    override fun observeSongCount(gameType: GameType): Flow<Int> = when (gameType) {
        GameType.ULTRASTAR -> ultraStarCount
        GameType.YARG -> yargCount
    }

    override fun observeUltraStarSongs(): Flow<List<UltraStarSongEntity>> = ultraStarSongs

    override fun observeYargSongs(): Flow<List<YargSongEntity>> = yargSongs

    override fun observeFilteredUltraStarSongs(filter: UltraStarFilter): Flow<List<UltraStarSongEntity>> {
        return ultraStarSongs.map { list ->
            val q = filter.query.trim().lowercase()
            list.filter { song ->
                (q.isEmpty() || song.title.lowercase().contains(q) || song.artist.lowercase().contains(q)) &&
                    (filter.genre == null || song.genre == filter.genre) &&
                    (filter.language == null || song.language == filter.language) &&
                    (filter.minYear == null || (song.year != null && song.year >= filter.minYear)) &&
                    (filter.maxYear == null || (song.year != null && song.year <= filter.maxYear)) &&
                    (filter.duet == null || song.duet == filter.duet) &&
                    (filter.solo == null || song.solo == filter.solo)
            }
        }
    }

    override fun observeFilteredYargSongs(filter: YargFilter): Flow<List<YargSongEntity>> {
        return yargSongs.map { list ->
            val q = filter.query.trim().lowercase()
            val albumQuery = filter.album?.trim()?.lowercase()
            list.filter { song ->
                (q.isEmpty() || song.name.lowercase().contains(q) || song.artist.lowercase().contains(q)) &&
                    (filter.genre == null || song.genre == filter.genre) &&
                    (albumQuery.isNullOrEmpty() || (song.album?.lowercase()?.contains(albumQuery) == true)) &&
                    (filter.format == null || song.format == filter.format) &&
                    (filter.minYear == null || (song.year != null && song.year >= filter.minYear)) &&
                    (filter.maxYear == null || (song.year != null && song.year <= filter.maxYear)) &&
                    (!filter.requireGuitar || song.guitarLevel != null) &&
                    (filter.minGuitar == null || (song.guitarLevel != null && song.guitarLevel >= filter.minGuitar)) &&
                    (!filter.requireBass || song.bassLevel != null) &&
                    (filter.minBass == null || (song.bassLevel != null && song.bassLevel >= filter.minBass)) &&
                    (!filter.requireDrums || song.drumsLevel != null) &&
                    (filter.minDrums == null || (song.drumsLevel != null && song.drumsLevel >= filter.minDrums)) &&
                    (!filter.requireKeys || song.keysLevel != null) &&
                    (filter.minKeys == null || (song.keysLevel != null && song.keysLevel >= filter.minKeys)) &&
                    (!filter.requireVocals || song.vocalsLevel != null) &&
                    (filter.minVocals == null || (song.vocalsLevel != null && song.vocalsLevel >= filter.minVocals))
            }
        }
    }

    override fun observeUltraStarFilterOptions(): Flow<UltraStarFilterOptions> {
        return ultraStarSongs.map { list ->
            UltraStarFilterOptions(
                genres = list.mapNotNull { it.genre }.distinct().sorted(),
                languages = list.mapNotNull { it.language }.distinct().sorted(),
                years = list.mapNotNull { it.year }.distinct().sorted(),
            )
        }
    }

    override fun observeYargFilterOptions(): Flow<YargFilterOptions> {
        return yargSongs.map { list ->
            YargFilterOptions(
                genres = list.mapNotNull { it.genre }.distinct().sorted(),
                formats = list.mapNotNull { it.format }.distinct().sorted(),
                years = list.mapNotNull { it.year }.distinct().sorted(),
            )
        }
    }

    override suspend fun getUltraStarSong(id: Long): UltraStarSongEntity? {
        return ultraStarSongs.value.find { it.id == id }
    }

    override suspend fun getYargSong(id: Long): YargSongEntity? {
        return yargSongs.value.find { it.id == id }
    }
}
