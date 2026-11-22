package com.leandroleu96.songlister.core

enum class GameType(
    val displayName: String,
    val gistPath: String,
) {
    ULTRASTAR(
        displayName = "UltraStar",
        gistPath = "leandroruiz96/d845362c9a00cba7f32e9216cb210876/raw/ultrastar_songs.json",
    ),
    YARG(
        displayName = "YARG",
        gistPath = "leandroruiz96/8a7b242e82040fd39178184701cf11f9/raw/yarg_songs.json",
    ),
    ;

    val fullUrl: String
        get() = "$GIST_BASE_URL$gistPath"

    companion object {
        const val GIST_BASE_URL = "https://gist.github.com/"
    }
}
