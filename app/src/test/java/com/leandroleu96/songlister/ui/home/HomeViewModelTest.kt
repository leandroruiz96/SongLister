package com.leandroleu96.songlister.ui.home

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.repository.GameSyncResult
import com.leandroleu96.songlister.data.repository.GameSyncStatus
import com.leandroleu96.songlister.testutil.FakeSongRepository
import com.leandroleu96.songlister.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
