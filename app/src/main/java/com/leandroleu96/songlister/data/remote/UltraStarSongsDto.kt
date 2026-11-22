package com.leandroleu96.songlister.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UltraStarResponseDto(
    val generated: String? = null,
    @SerialName("song_count")
    val songCount: Int = 0,
    val songs: List<UltraStarSongDto> = emptyList(),
)

@Serializable
data class UltraStarSongDto(
    val title: String = "",
    val artist: String = "",
    val year: Int? = null,
    val language: String? = null,
    val genre: String? = null,
    val duet: Boolean = false,
    val solo: Boolean = false,
    @SerialName("song_length_ms")
    val songLengthMs: Long? = null,
    @SerialName("song_length")
    val songLength: String? = null,
    val sources: List<String> = emptyList(),
)
