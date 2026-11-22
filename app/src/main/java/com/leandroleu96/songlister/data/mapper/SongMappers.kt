package com.leandroleu96.songlister.data.mapper

import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import com.leandroleu96.songlister.data.remote.UltraStarSongDto
import com.leandroleu96.songlister.data.remote.YargSongDto

fun UltraStarSongDto.toEntity(): UltraStarSongEntity {
    return UltraStarSongEntity(
        title = title.trim(),
        artist = artist.trim(),
        year = year,
        language = language?.trim()?.takeIf { it.isNotEmpty() },
        genre = genre?.trim()?.takeIf { it.isNotEmpty() },
        duet = duet,
        solo = solo,
        songLengthMs = songLengthMs,
        songLength = songLength?.trim()?.takeIf { it.isNotEmpty() },
        sources = sources,
    )
}

fun YargSongDto.toEntity(): YargSongEntity {
    return YargSongEntity(
        name = name.trim(),
        artist = artist.trim(),
        album = album?.trim()?.takeIf { it.isNotEmpty() },
        year = year,
        genre = genre?.trim()?.takeIf { it.isNotEmpty() },
        songLengthMs = songLengthMs,
        songLength = songLength?.trim()?.takeIf { it.isNotEmpty() },
        guitarLevel = instruments["guitar"],
        bassLevel = instruments["bass"],
        drumsLevel = instruments["drums"],
        keysLevel = instruments["keys"],
        vocalsLevel = instruments["vocals"],
        source = source?.trim()?.takeIf { it.isNotEmpty() },
        format = format?.trim()?.takeIf { it.isNotEmpty() },
    )
}
