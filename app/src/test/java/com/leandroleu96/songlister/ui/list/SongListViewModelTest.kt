package com.leandroleu96.songlister.ui.list

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import com.leandroleu96.songlister.data.repository.UltraStarFilter
import com.leandroleu96.songlister.data.repository.YargFilter
import com.leandroleu96.songlister.testutil.FakeSongRepository
import com.leandroleu96.songlister.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleUltraStarSongs = listOf(
        UltraStarSongEntity(
            id = 1L,
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
        ),
        UltraStarSongEntity(
            id = 2L,
            title = "Shallow",
            artist = "Lady Gaga & Bradley Cooper",
            year = 2018,
            language = "English",
            genre = "Pop",
            duet = true,
            solo = false,
            songLengthMs = 215000L,
            songLength = "3:35",
            sources = listOf("Shallow.txt"),
        ),
        UltraStarSongEntity(
            id = 3L,
            title = "De Musica Ligera",
            artist = "Soda Stereo",
            year = 1990,
            language = "Spanish",
            genre = "Rock",
            duet = false,
            solo = true,
            songLengthMs = 212000L,
            songLength = "3:32",
            sources = listOf("MusicaLigera.txt"),
        ),
    )

    private val sampleYargSongs = listOf(
        YargSongEntity(
            id = 10L,
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
        ),
        YargSongEntity(
            id = 11L,
            name = "!I",
            artist = "!I!",
            album = "IXI",
            year = 2011,
            genre = "Other",
            songLengthMs = 40477L,
            songLength = "0:40",
            guitarLevel = null,
            bassLevel = null,
            drumsLevel = null,
            keysLevel = 2,
            vocalsLevel = null,
            source = "chart1.sng",
            format = "sng",
        ),
    )

    @Test
    fun `ultrastar search and filters narrow songs and expose distinct filter options`() = runTest {
        val repo = FakeSongRepository(initialUltraStarSongs = sampleUltraStarSongs)
        val viewModel = SongListViewModel(
            gameType = GameType.ULTRASTAR,
            repository = repo,
            debounceMillis = 0L,
        )

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        assertEquals(3, viewModel.uiState.value.songs.size)
        assertEquals(listOf("Pop", "Rock"), viewModel.uiState.value.ultraStarFilterOptions.genres)
        assertEquals(listOf("English", "Spanish"), viewModel.uiState.value.ultraStarFilterOptions.languages)

        viewModel.onSearchQueryChange("Soda")
        assertEquals(listOf("De Musica Ligera"), viewModel.uiState.value.songs.map { it.title })

        viewModel.onSearchQueryChange("")
        viewModel.onUpdateUltraStarFilter(UltraStarFilter(duet = true))
        assertEquals(1, viewModel.uiState.value.activeFilterCount)
        assertEquals(listOf("Shallow"), viewModel.uiState.value.songs.map { it.title })

        viewModel.onClearFilters()
        assertEquals(0, viewModel.uiState.value.activeFilterCount)
        assertEquals(3, viewModel.uiState.value.songs.size)

        collectJob.cancel()
    }

    @Test
    fun `yarg instrument and album filters narrow songs and refresh syncs game`() = runTest {
        val repo = FakeSongRepository(initialYargSongs = sampleYargSongs)
        val viewModel = SongListViewModel(
            gameType = GameType.YARG,
            repository = repo,
            debounceMillis = 0L,
        )

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        assertEquals(2, viewModel.uiState.value.songs.size)

        viewModel.onUpdateYargFilter(YargFilter(requireKeys = true))
        assertEquals(listOf("!I"), viewModel.uiState.value.songs.map { it.title })

        viewModel.onUpdateYargFilter(YargFilter(album = "distance", minDrums = 4))
        assertEquals(2, viewModel.uiState.value.activeFilterCount)
        assertEquals(listOf("Ghost I Own"), viewModel.uiState.value.songs.map { it.title })

        viewModel.onRefresh()
        assertEquals(listOf(GameType.YARG to true), repo.syncGameCalls)

        collectJob.cancel()
    }
}
