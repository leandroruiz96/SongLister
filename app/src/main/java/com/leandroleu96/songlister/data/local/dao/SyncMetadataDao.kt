package com.leandroleu96.songlister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leandroleu96.songlister.data.local.entity.SyncMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetadataDao {

    @Query("SELECT * FROM sync_metadata WHERE gameType = :gameType")
    suspend fun getMetadata(gameType: String): SyncMetadataEntity?

    @Query("SELECT * FROM sync_metadata WHERE gameType = :gameType")
    fun observeMetadata(gameType: String): Flow<SyncMetadataEntity?>

    @Query("SELECT * FROM sync_metadata")
    fun observeAllMetadata(): Flow<List<SyncMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SyncMetadataEntity)
}
