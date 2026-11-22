package com.leandroleu96.songlister.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class SongTypeConvertersTest {

    private val converters = SongTypeConverters()

    @Test
    fun `string list round-trips through json converter`() {
        val original = listOf("folder\\song1.txt", "folder\\song2.txt")
        val encoded = converters.fromStringList(original)
        val decoded = converters.toStringList(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `blank or malformed json falls back to empty list`() {
        assertEquals(emptyList<String>(), converters.toStringList(""))
        assertEquals(emptyList<String>(), converters.toStringList("not-valid-json"))
    }
}
