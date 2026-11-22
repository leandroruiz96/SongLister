package com.leandroleu96.songlister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UltraStarSongDao {

    @Query("SELECT * FROM ultrastar_songs ORDER BY artist COLLATE NOCASE ASC, title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<UltraStarSongEntity>>

    @Query(
        """
        SELECT * FROM ultrastar_songs
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%')
          AND (:genre IS NULL OR genre = :genre)
          AND (:language IS NULL OR language = :language)
          AND (:minYear IS NULL OR year >= :minYear)
          AND (:maxYear IS NULL OR year <= :maxYear)
          AND (:duet IS NULL OR duet = :duet)
          AND (:solo IS NULL OR solo = :solo)
        ORDER BY artist COLLATE NOCASE ASC, title COLLATE NOCASE ASC
        """,
    )
    fun observeFiltered(
        query: String = "",
        genre: String? = null,
        language: String? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        duet: Boolean? = null,
        solo: Boolean? = null,
    ): Flow<List<UltraStarSongEntity>>

    @Query("SELECT * FROM ultrastar_songs WHERE id = :id")
    suspend fun getById(id: Long): UltraStarSongEntity?

    @Query("SELECT COUNT(*) FROM ultrastar_songs")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ultrastar_songs")
    suspend fun getCount(): Int

    @Query("SELECT DISTINCT genre FROM ultrastar_songs WHERE genre IS NOT NULL AND genre != '' ORDER BY genre COLLATE NOCASE ASC")
    fun observeDistinctGenres(): Flow<List<String>>

    @Query("SELECT DISTINCT language FROM ultrastar_songs WHERE language IS NOT NULL AND language != '' ORDER BY language COLLATE NOCASE ASC")
    fun observeDistinctLanguages(): Flow<List<String>>

    @Query("SELECT DISTINCT year FROM ultrastar_songs WHERE year IS NOT NULL ORDER BY year ASC")
    fun observeDistinctYears(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<UltraStarSongEntity>)

    @Query("DELETE FROM ultrastar_songs")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(songs: List<UltraStarSongEntity>) {
        deleteAll()
        insertAll(songs)
    }
}
