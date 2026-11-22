package com.leandroleu96.songlister.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leandroleu96.songlister.data.local.dao.SyncMetadataDao
import com.leandroleu96.songlister.data.local.dao.UltraStarSongDao
import com.leandroleu96.songlister.data.local.dao.YargSongDao
import com.leandroleu96.songlister.data.local.entity.SyncMetadataEntity
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity

@Database(
    entities = [
        UltraStarSongEntity::class,
        YargSongEntity::class,
        SyncMetadataEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(SongTypeConverters::class)
abstract class SongListerDatabase : RoomDatabase() {
    abstract fun ultraStarSongDao(): UltraStarSongDao
    abstract fun yargSongDao(): YargSongDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        private const val DATABASE_NAME = "song_lister.db"

        fun create(context: Context): SongListerDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SongListerDatabase::class.java,
                DATABASE_NAME,
            ).build()
        }
    }
}
