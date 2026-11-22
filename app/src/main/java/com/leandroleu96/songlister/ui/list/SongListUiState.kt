package com.leandroleu96.songlister.ui.list

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import com.leandroleu96.songlister.data.repository.UltraStarFilter
import com.leandroleu96.songlister.data.repository.UltraStarFilterOptions
import com.leandroleu96.songlister.data.repository.YargFilter
import com.leandroleu96.songlister.data.repository.YargFilterOptions

data class SongListItemUi(
    val id: Long,
    val title: String,
    val artist: String,
    val subtitle: String?,
    val genre: String?,
    val year: Int?,
    val duration: String?,
    val badges: List<String>,
)

data class SongListUiState(
    val gameType: GameType,
    val searchQuery: String = "",
    val songs: List<SongListItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val ultraStarFilter: UltraStarFilter = UltraStarFilter(),
    val yargFilter: YargFilter = YargFilter(),
    val ultraStarFilterOptions: UltraStarFilterOptions = UltraStarFilterOptions(),
    val yargFilterOptions: YargFilterOptions = YargFilterOptions(),
) {
    val activeFilterCount: Int
        get() = when (gameType) {
            GameType.ULTRASTAR -> ultraStarFilter.activeFilterCount
            GameType.YARG -> yargFilter.activeFilterCount
        }
}

fun UltraStarSongEntity.toListItemUi(): SongListItemUi {
    val badges = buildList {
        if (solo) add("Solo")
        if (duet) add("Duet")
    }
    return SongListItemUi(
        id = id,
        title = title,
        artist = artist,
        subtitle = language,
        genre = genre,
        year = year,
        duration = songLength,
        badges = badges,
    )
}

fun YargSongEntity.toListItemUi(): SongListItemUi {
    val badges = buildList {
        guitarLevel?.let { add("Guitar $it") }
        bassLevel?.let { add("Bass $it") }
        drumsLevel?.let { add("Drums $it") }
        keysLevel?.let { add("Keys $it") }
        vocalsLevel?.let { add("Vocals $it") }
    }
    return SongListItemUi(
        id = id,
        title = name,
        artist = artist,
        subtitle = album,
        genre = genre,
        year = year,
        duration = songLength,
        badges = badges,
    )
}
