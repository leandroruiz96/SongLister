package com.leandroleu96.songlister.data.local

import androidx.room.TypeConverter
import com.leandroleu96.songlister.core.network.NetworkFactory
import kotlinx.serialization.encodeToString

class SongTypeConverters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return NetworkFactory.defaultJson.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return runCatching {
            NetworkFactory.defaultJson.decodeFromString<List<String>>(value)
        }.getOrDefault(emptyList())
    }
}
