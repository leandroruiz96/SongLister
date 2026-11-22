package com.leandroleu96.songlister.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ultrastar_songs",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["genre"]),
        Index(value = ["year"]),
        Index(value = ["language"]),
    ],
)
data class UltraStarSongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val year: Int?,
    val language: String?,
    val genre: String?,
    val duet: Boolean,
    val solo: Boolean,
    val songLengthMs: Long?,
    val songLength: String?,
    val sources: List<String>,
)
