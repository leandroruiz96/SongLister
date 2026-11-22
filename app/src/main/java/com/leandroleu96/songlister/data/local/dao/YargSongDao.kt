package com.leandroleu96.songlister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface YargSongDao {

    @Query("SELECT * FROM yarg_songs ORDER BY artist COLLATE NOCASE ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<YargSongEntity>>

    @Query(
        """
        SELECT * FROM yarg_songs
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%')
          AND (:genre IS NULL OR genre = :genre)
          AND (:album IS NULL OR album = :album)
          AND (:format IS NULL OR format = :format)
          AND (:minYear IS NULL OR year >= :minYear)
          AND (:maxYear IS NULL OR year <= :maxYear)
          AND (:requireGuitar = 0 OR guitarLevel IS NOT NULL)
          AND (:minGuitar IS NULL OR guitarLevel >= :minGuitar)
          AND (:requireBass = 0 OR bassLevel IS NOT NULL)
          AND (:minBass IS NULL OR bassLevel >= :minBass)
          AND (:requireDrums = 0 OR drumsLevel IS NOT NULL)
          AND (:minDrums IS NULL OR drumsLevel >= :minDrums)
          AND (:requireKeys = 0 OR keysLevel IS NOT NULL)
          AND (:minKeys IS NULL OR keysLevel >= :minKeys)
          AND (:requireVocals = 0 OR vocalsLevel IS NOT NULL)
          AND (:minVocals IS NULL OR vocalsLevel >= :minVocals)
        ORDER BY artist COLLATE NOCASE ASC, name COLLATE NOCASE ASC
        """,
    )
    fun observeFiltered(
        query: String = "",
        genre: String? = null,
        album: String? = null,
        format: String? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        requireGuitar: Boolean = false,
        minGuitar: Int? = null,
        requireBass: Boolean = false,
        minBass: Int? = null,
        requireDrums: Boolean = false,
        minDrums: Int? = null,
        requireKeys: Boolean = false,
        minKeys: Int? = null,
        requireVocals: Boolean = false,
        minVocals: Int? = null,
    ): Flow<List<YargSongEntity>>

    @Query("SELECT * FROM yarg_songs WHERE id = :id")
    suspend fun getById(id: Long): YargSongEntity?

    @Query("SELECT COUNT(*) FROM yarg_songs")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM yarg_songs")
    suspend fun getCount(): Int

    @Query("SELECT DISTINCT genre FROM yarg_songs WHERE genre IS NOT NULL AND genre != '' ORDER BY genre COLLATE NOCASE ASC")
    fun observeDistinctGenres(): Flow<List<String>>

    @Query("SELECT DISTINCT format FROM yarg_songs WHERE format IS NOT NULL AND format != '' ORDER BY format COLLATE NOCASE ASC")
    fun observeDistinctFormats(): Flow<List<String>>

    @Query("SELECT DISTINCT year FROM yarg_songs WHERE year IS NOT NULL ORDER BY year ASC")
    fun observeDistinctYears(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<YargSongEntity>)

    @Query("DELETE FROM yarg_songs")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(songs: List<YargSongEntity>) {
        deleteAll()
        insertAll(songs)
    }
}
