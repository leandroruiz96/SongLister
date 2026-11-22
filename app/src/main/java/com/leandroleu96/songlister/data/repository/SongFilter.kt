package com.leandroleu96.songlister.data.repository

data class UltraStarFilter(
    val query: String = "",
    val genre: String? = null,
    val language: String? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val duet: Boolean? = null,
    val solo: Boolean? = null,
) {
    val activeFilterCount: Int
        get() = listOfNotNull(
            genre,
            language,
            minYear,
            maxYear,
            duet,
            solo,
        ).size
}

data class YargFilter(
    val query: String = "",
    val genre: String? = null,
    val album: String? = null,
    val format: String? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val requireGuitar: Boolean = false,
    val minGuitar: Int? = null,
    val requireBass: Boolean = false,
    val minBass: Int? = null,
    val requireDrums: Boolean = false,
    val minDrums: Int? = null,
    val requireKeys: Boolean = false,
    val minKeys: Int? = null,
    val requireVocals: Boolean = false,
    val minVocals: Int? = null,
) {
    val activeFilterCount: Int
        get() {
            var count = 0
            if (!genre.isNullOrBlank()) count++
            if (!album.isNullOrBlank()) count++
            if (!format.isNullOrBlank()) count++
            if (minYear != null) count++
            if (maxYear != null) count++
            if (requireGuitar || minGuitar != null) count++
            if (requireBass || minBass != null) count++
            if (requireDrums || minDrums != null) count++
            if (requireKeys || minKeys != null) count++
            if (requireVocals || minVocals != null) count++
            return count
        }
}

data class UltraStarFilterOptions(
    val genres: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val years: List<Int> = emptyList(),
)

data class YargFilterOptions(
    val genres: List<String> = emptyList(),
    val formats: List<String> = emptyList(),
    val years: List<Int> = emptyList(),
)
