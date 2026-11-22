package com.leandroleu96.songlister.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class SongFilterTest {

    @Test
    fun `ultrastar activeFilterCount excludes search query and counts active attributes`() {
        val empty = UltraStarFilter(query = "attack")
        assertEquals(0, empty.activeFilterCount)

        val filtered = UltraStarFilter(
            query = "attack",
            genre = "Rock",
            language = "English",
            minYear = 2000,
            duet = true,
        )
        assertEquals(4, filtered.activeFilterCount)
    }

    @Test
    fun `yarg activeFilterCount counts instruments and metadata filters`() {
        val empty = YargFilter(query = "ghost")
        assertEquals(0, empty.activeFilterCount)

        val filtered = YargFilter(
            query = "ghost",
            genre = "Alternative",
            album = "Distance",
            format = "sng",
            requireGuitar = true,
            minGuitar = 3,
            minDrums = 4,
        )
        assertEquals(5, filtered.activeFilterCount)
    }
}
