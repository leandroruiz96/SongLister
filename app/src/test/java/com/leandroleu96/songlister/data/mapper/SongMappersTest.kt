package com.leandroleu96.songlister.data.mapper

import com.leandroleu96.songlister.data.remote.UltraStarSongDto
import com.leandroleu96.songlister.data.remote.YargSongDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SongMappersTest {

    @Test
    fun `ultrastar dto maps cleanly to entity`() {
        val dto = UltraStarSongDto(
            title = "  Attack  ",
            artist = " 30 Seconds To Mars ",
            year = 2005,
            language = "English",
            genre = "   ",
            duet = false,
            solo = true,
            songLengthMs = 181939L,
            songLength = "3:02",
            sources = listOf("30 Seconds To Mars - Attack\\Attack.txt"),
        )

        val entity = dto.toEntity()

        assertEquals("Attack", entity.title)
        assertEquals("30 Seconds To Mars", entity.artist)
        assertEquals(2005, entity.year)
        assertEquals("English", entity.language)
        assertNull(entity.genre)
        assertFalse(entity.duet)
        assertTrue(entity.solo)
        assertEquals(181939L, entity.songLengthMs)
        assertEquals("3:02", entity.songLength)
        assertEquals(listOf("30 Seconds To Mars - Attack\\Attack.txt"), entity.sources)
    }

    @Test
    fun `yarg dto maps instruments and nullable fields to entity`() {
        val dto = YargSongDto(
            name = " Ghost I Own ",
            artist = "(Damn) This Desert Air",
            album = "Distance Waits",
            year = 2010,
            genre = "Alternative",
            songLengthMs = 219682L,
            songLength = "3:40",
            instruments = mapOf(
                "guitar" to 3,
                "bass" to 1,
                "drums" to 4,
                "vocals" to 3,
            ),
            source = "Rock Band Network\\chart 534.sng",
            format = "sng",
        )

        val entity = dto.toEntity()

        assertEquals("Ghost I Own", entity.name)
        assertEquals("(Damn) This Desert Air", entity.artist)
        assertEquals("Distance Waits", entity.album)
        assertEquals(2010, entity.year)
        assertEquals("Alternative", entity.genre)
        assertEquals(3, entity.guitarLevel)
        assertEquals(1, entity.bassLevel)
        assertEquals(4, entity.drumsLevel)
        assertNull(entity.keysLevel)
        assertEquals(3, entity.vocalsLevel)
        assertEquals("Rock Band Network\\chart 534.sng", entity.source)
        assertEquals("sng", entity.format)
    }
}
