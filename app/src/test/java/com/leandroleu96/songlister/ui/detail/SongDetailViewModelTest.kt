package com.leandroleu96.songlister.ui.detail

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import com.leandroleu96.songlister.testutil.FakeSongRepository
import com.leandroleu96.songlister.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loads ultrastar song by id`() = runTest {
        val song = UltraStarSongEntity(
            id = 42L,
            title = "Attack",
            artist = "30 Seconds To Mars",
            year = 2005,
            language = "English",
            genre = "Rock",
            duet = false,
            solo = true,
            songLengthMs = 181939L,
            songLength = "3:02",
            sources = listOf("Attack.txt"),
        )
        val repo = FakeSongRepository(initialUltraStarSongs = listOf(song))
        val viewModel = SongDetailViewModel(
            gameType = GameType.ULTRASTAR,
            songId = 42L,
            repository = repo,
        )

        val state = viewModel.uiState.value
        assertTrue(state is SongDetailUiState.UltraStarLoaded)
        assertEquals(song, (state as SongDetailUiState.UltraStarLoaded).song)
    }

    @Test
    fun `loads yarg song by id`() = runTest {
        val song = YargSongEntity(
            id = 99L,
            name = "Ghost I Own",
            artist = "(Damn) This Desert Air",
            album = "Distance Waits",
            year = 2010,
            genre = "Alternative",
            songLengthMs = 219682L,
            songLength = "3:40",
            guitarLevel = 3,
            bassLevel = 1,
            drumsLevel = 4,
            keysLevel = null,
            vocalsLevel = 3,
            source = "chart.sng",
            format = "sng",
        )
        val repo = FakeSongRepository(initialYargSongs = listOf(song))
        val viewModel = SongDetailViewModel(
            gameType = GameType.YARG,
            songId = 99L,
            repository = repo,
        )

        val state = viewModel.uiState.value
        assertTrue(state is SongDetailUiState.YargLoaded)
        assertEquals(song, (state as SongDetailUiState.YargLoaded).song)
    }

    @Test
    fun `emits NotFound when song id does not exist`() = runTest {
        val repo = FakeSongRepository()
        val viewModel = SongDetailViewModel(
            gameType = GameType.ULTRASTAR,
            songId = 999L,
            repository = repo,
        )

        assertEquals(SongDetailUiState.NotFound, viewModel.uiState.value)
    }
}
