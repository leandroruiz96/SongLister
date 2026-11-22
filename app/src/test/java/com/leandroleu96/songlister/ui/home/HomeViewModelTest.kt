package com.leandroleu96.songlister.ui.home

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.local.entity.SyncMetadataEntity
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import com.leandroleu96.songlister.data.repository.GameSyncResult
import com.leandroleu96.songlister.data.repository.GameSyncStatus
import com.leandroleu96.songlister.data.repository.SongRepository
import com.leandroleu96.songlister.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `auto sync on init triggers non-forced sync and populates counts`() = runTest {
        val repo = FakeSongRepository(
            nextSyncResults = mapOf(
                GameType.ULTRASTAR to GameSyncResult(GameType.ULTRASTAR, GameSyncStatus.UPDATED, 423),
                GameType.YARG to GameSyncResult(GameType.YARG, GameSyncStatus.UP_TO_DATE, 4747),
            ),
        )
        val viewModel = HomeViewModel(repository = repo, autoSyncOnInit = true)

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val state = viewModel.uiState.value
        assertEquals(listOf(false), repo.syncAllCalls)
        assertFalse(state.isSyncing)
        assertEquals(423, state.ultraStar.songCount)
        assertEquals(GameSyncStatus.UPDATED, state.ultraStar.syncStatus)
        assertEquals(4747, state.yarg.songCount)
        assertEquals(GameSyncStatus.UP_TO_DATE, state.yarg.syncStatus)
        assertFalse(state.hasAnyError)
        assertFalse(state.isOfflineCached)

        collectJob.cancel()
    }

    @Test
    fun `manual refresh calls syncAll with forceRefresh true`() = runTest {
        val repo = FakeSongRepository(
            nextSyncResults = mapOf(
                GameType.ULTRASTAR to GameSyncResult(GameType.ULTRASTAR, GameSyncStatus.UPDATED, 423),
                GameType.YARG to GameSyncResult(GameType.YARG, GameSyncStatus.UPDATED, 4747),
            ),
        )
        val viewModel = HomeViewModel(repository = repo, autoSyncOnInit = false)

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.syncAll(forceRefresh = true)

        assertEquals(listOf(true), repo.syncAllCalls)
        assertEquals(423, viewModel.uiState.value.ultraStar.songCount)
        assertEquals(4747, viewModel.uiState.value.yarg.songCount)

        collectJob.cancel()
    }

    @Test
    fun `offline cached and error flags reflect sync status`() = runTest {
        val repo = FakeSongRepository(
            nextSyncResults = mapOf(
                GameType.ULTRASTAR to GameSyncResult(
                    gameType = GameType.ULTRASTAR,
                    status = GameSyncStatus.OFFLINE_CACHED,
                    songCount = 423,
                    errorMessage = "No internet",
                ),
                GameType.YARG to GameSyncResult(
                    gameType = GameType.YARG,
                    status = GameSyncStatus.ERROR,
                    songCount = 0,
                    errorMessage = "No internet",
                ),
            ),
        )
        val viewModel = HomeViewModel(repository = repo, autoSyncOnInit = true)

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val state = viewModel.uiState.value
        assertTrue(state.isOfflineCached)
        assertTrue(state.hasAnyError)
        assertEquals("No internet", state.yarg.errorMessage)

        collectJob.cancel()
    }
}

private class FakeSongRepository(
    var nextSyncResults: Map<GameType, GameSyncResult>,
) : SongRepository {

    val syncAllCalls = mutableListOf<Boolean>()
    private val ultraStarCount = MutableStateFlow(0)
    private val yargCount = MutableStateFlow(0)
    private val metadata = MutableStateFlow<Map<GameType, SyncMetadataEntity>>(emptyMap())

    override suspend fun syncGame(gameType: GameType, forceRefresh: Boolean): GameSyncResult {
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

    override fun observeUltraStarSongs(): Flow<List<UltraStarSongEntity>> = MutableStateFlow(emptyList())
    override fun observeYargSongs(): Flow<List<YargSongEntity>> = MutableStateFlow(emptyList())
    override suspend fun getUltraStarSong(id: Long): UltraStarSongEntity? = null
    override suspend fun getYargSong(id: Long): YargSongEntity? = null
}
