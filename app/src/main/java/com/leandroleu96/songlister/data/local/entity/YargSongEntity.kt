package com.leandroleu96.songlister.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "yarg_songs",
    indices = [
        Index(value = ["name"]),
        Index(value = ["artist"]),
        Index(value = ["genre"]),
        Index(value = ["year"]),
        Index(value = ["album"]),
        Index(value = ["format"]),
    ],
)
data class YargSongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val artist: String,
    val album: String?,
    val year: Int?,
    val genre: String?,
    val songLengthMs: Long?,
    val songLength: String?,
    val guitarLevel: Int?,
    val bassLevel: Int?,
    val drumsLevel: Int?,
    val keysLevel: Int?,
    val vocalsLevel: Int?,
    val source: String?,
    val format: String?,
)
