package com.leandroleu96.songlister.core

import org.junit.Assert.assertEquals
import org.junit.Test

class GameTypeTest {

    @Test
    fun `ultrastar game type has expected url and display name`() {
        assertEquals("UltraStar", GameType.ULTRASTAR.displayName)
        assertEquals(
            "https://gist.github.com/leandroruiz96/d845362c9a00cba7f32e9216cb210876/raw/ultrastar_songs.json",
            GameType.ULTRASTAR.fullUrl,
        )
    }

    @Test
    fun `yarg game type has expected url and display name`() {
        assertEquals("YARG", GameType.YARG.displayName)
        assertEquals(
            "https://gist.github.com/leandroruiz96/8a7b242e82040fd39178184701cf11f9/raw/yarg_songs.json",
            GameType.YARG.fullUrl,
        )
    }
}
