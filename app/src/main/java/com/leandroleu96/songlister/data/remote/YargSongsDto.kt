package com.leandroleu96.songlister.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YargResponseDto(
    val generated: String? = null,
    @SerialName("song_count")
    val songCount: Int = 0,
    val songs: List<YargSongDto> = emptyList(),
)

@Serializable
data class YargSongDto(
    val name: String = "",
    val artist: String = "",
    val album: String? = null,
    val year: Int? = null,
    val genre: String? = null,
    @SerialName("song_length_ms")
    val songLengthMs: Long? = null,
    @SerialName("song_length")
    val songLength: String? = null,
    val instruments: Map<String, Int> = emptyMap(),
    val source: String? = null,
    val format: String? = null,
)
