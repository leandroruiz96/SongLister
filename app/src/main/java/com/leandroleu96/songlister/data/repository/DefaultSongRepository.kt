package com.leandroleu96.songlister.data.repository

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.local.dao.SyncMetadataDao
import com.leandroleu96.songlister.data.local.dao.UltraStarSongDao
import com.leandroleu96.songlister.data.local.dao.YargSongDao
import com.leandroleu96.songlister.data.local.entity.SyncMetadataEntity
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import com.leandroleu96.songlister.data.mapper.toEntity
import com.leandroleu96.songlister.data.remote.GistApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DefaultSongRepository(
    private val apiService: GistApiService,
    private val ultraStarSongDao: UltraStarSongDao,
    private val yargSongDao: YargSongDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis,
    private val ultraStarUrl: String = GameType.ULTRASTAR.fullUrl,
    private val yargUrl: String = GameType.YARG.fullUrl,
) : SongRepository {

    override suspend fun syncAll(forceRefresh: Boolean): Map<GameType, GameSyncResult> {
        return coroutineScope {
            val ultraStarDeferred = async { syncGame(GameType.ULTRASTAR, forceRefresh) }
            val yargDeferred = async { syncGame(GameType.YARG, forceRefresh) }
            mapOf(
                GameType.ULTRASTAR to ultraStarDeferred.await(),
                GameType.YARG to yargDeferred.await(),
            )
        }
    }

    override suspend fun syncGame(
        gameType: GameType,
        forceRefresh: Boolean,
    ): GameSyncResult = withContext(ioDispatcher) {
        val existingCount = when (gameType) {
            GameType.ULTRASTAR -> ultraStarSongDao.getCount()
            GameType.YARG -> yargSongDao.getCount()
        }
        val existingMetadata = syncMetadataDao.getMetadata(gameType.name)
        val ifNoneMatch = if (!forceRefresh && existingCount > 0) {
            existingMetadata?.etag
        } else {
            null
        }

        try {
            when (gameType) {
                GameType.ULTRASTAR -> syncUltraStar(existingCount, existingMetadata, ifNoneMatch)
                GameType.YARG -> syncYarg(existingCount, existingMetadata, ifNoneMatch)
            }
        } catch (e: Exception) {
            fallbackOrError(gameType, existingCount, e.message ?: "Network error")
        }
    }

    private suspend fun syncUltraStar(
        existingCount: Int,
        existingMetadata: SyncMetadataEntity?,
        ifNoneMatch: String?,
    ): GameSyncResult {
        val response = apiService.getUltraStarSongs(
            url = ultraStarUrl,
            ifNoneMatch = ifNoneMatch,
        )
        if (response.code() == HTTP_NOT_MODIFIED) {
            if (existingMetadata != null) {
                syncMetadataDao.upsert(existingMetadata.copy(lastSyncedAtMillis = clock()))
            }
            return GameSyncResult(
                gameType = GameType.ULTRASTAR,
                status = GameSyncStatus.UP_TO_DATE,
                songCount = existingCount,
            )
        }
        if (response.isSuccessful) {
            val body = response.body()
                ?: return fallbackOrError(GameType.ULTRASTAR, existingCount, "Empty response body")
            val entities = body.songs.map { it.toEntity() }
            ultraStarSongDao.replaceAll(entities)
            syncMetadataDao.upsert(
                SyncMetadataEntity(
                    gameType = GameType.ULTRASTAR.name,
                    etag = response.headers()["ETag"],
                    generatedAt = body.generated,
                    lastSyncedAtMillis = clock(),
                    songCount = entities.size,
                ),
            )
            return GameSyncResult(
                gameType = GameType.ULTRASTAR,
                status = GameSyncStatus.UPDATED,
                songCount = entities.size,
            )
        }
        return fallbackOrError(
            gameType = GameType.ULTRASTAR,
            existingCount = existingCount,
            errorMessage = "HTTP ${response.code()}: ${response.message()}",
        )
    }

    private suspend fun syncYarg(
        existingCount: Int,
        existingMetadata: SyncMetadataEntity?,
        ifNoneMatch: String?,
    ): GameSyncResult {
        val response = apiService.getYargSongs(
            url = yargUrl,
            ifNoneMatch = ifNoneMatch,
        )
        if (response.code() == HTTP_NOT_MODIFIED) {
            if (existingMetadata != null) {
                syncMetadataDao.upsert(existingMetadata.copy(lastSyncedAtMillis = clock()))
            }
            return GameSyncResult(
                gameType = GameType.YARG,
                status = GameSyncStatus.UP_TO_DATE,
                songCount = existingCount,
            )
        }
        if (response.isSuccessful) {
            val body = response.body()
                ?: return fallbackOrError(GameType.YARG, existingCount, "Empty response body")
            val entities = body.songs.map { it.toEntity() }
            yargSongDao.replaceAll(entities)
            syncMetadataDao.upsert(
                SyncMetadataEntity(
                    gameType = GameType.YARG.name,
                    etag = response.headers()["ETag"],
                    generatedAt = body.generated,
                    lastSyncedAtMillis = clock(),
                    songCount = entities.size,
                ),
            )
            return GameSyncResult(
                gameType = GameType.YARG,
                status = GameSyncStatus.UPDATED,
                songCount = entities.size,
            )
        }
        return fallbackOrError(
            gameType = GameType.YARG,
            existingCount = existingCount,
            errorMessage = "HTTP ${response.code()}: ${response.message()}",
        )
    }

    private fun fallbackOrError(
        gameType: GameType,
        existingCount: Int,
        errorMessage: String,
    ): GameSyncResult {
        return if (existingCount > 0) {
            GameSyncResult(
                gameType = gameType,
                status = GameSyncStatus.OFFLINE_CACHED,
                songCount = existingCount,
                errorMessage = errorMessage,
            )
        } else {
            GameSyncResult(
                gameType = gameType,
                status = GameSyncStatus.ERROR,
                songCount = 0,
                errorMessage = errorMessage,
            )
        }
    }

    override fun observeSyncMetadata(): Flow<Map<GameType, SyncMetadataEntity>> {
        return syncMetadataDao.observeAllMetadata().map { list ->
            list.mapNotNull { entity ->
                val type = runCatching { GameType.valueOf(entity.gameType) }.getOrNull()
                type?.let { it to entity }
            }.toMap()
        }
    }

    override fun observeSongCount(gameType: GameType): Flow<Int> {
        return when (gameType) {
            GameType.ULTRASTAR -> ultraStarSongDao.observeCount()
            GameType.YARG -> yargSongDao.observeCount()
        }
    }

    override fun observeUltraStarSongs(): Flow<List<UltraStarSongEntity>> {
        return ultraStarSongDao.observeAll()
    }

    override fun observeYargSongs(): Flow<List<YargSongEntity>> {
        return yargSongDao.observeAll()
    }

    override suspend fun getUltraStarSong(id: Long): UltraStarSongEntity? {
        return ultraStarSongDao.getById(id)
    }

    override suspend fun getYargSong(id: Long): YargSongEntity? {
        return yargSongDao.getById(id)
    }

    companion object {
        private const val HTTP_NOT_MODIFIED = 304
    }
}
