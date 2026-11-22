package com.leandroleu96.songlister.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey
    val gameType: String,
    val etag: String?,
    val generatedAt: String?,
    val lastSyncedAtMillis: Long,
    val songCount: Int,
)
