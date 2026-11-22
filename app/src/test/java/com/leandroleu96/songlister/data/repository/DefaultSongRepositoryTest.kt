package com.leandroleu96.songlister.data.repository

import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.core.network.NetworkFactory
import com.leandroleu96.songlister.data.local.dao.SyncMetadataDao
import com.leandroleu96.songlister.data.local.dao.UltraStarSongDao
import com.leandroleu96.songlister.data.local.dao.YargSongDao
import com.leandroleu96.songlister.data.local.entity.SyncMetadataEntity
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity
import com.leandroleu96.songlister.data.remote.GistApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSongRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var ultraStarDao: FakeUltraStarSongDao
    private lateinit var yargDao: FakeYargSongDao
    private lateinit var metadataDao: FakeSyncMetadataDao
    private lateinit var repository: DefaultSongRepository
    private var currentTime = 1_000L

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        ultraStarDao = FakeUltraStarSongDao()
        yargDao = FakeYargSongDao()
        metadataDao = FakeSyncMetadataDao()

        val apiService = NetworkFactory.createRetrofit(
            baseUrl = mockWebServer.url("/").toString(),
        ).create(GistApiService::class.java)

        repository = DefaultSongRepository(
            apiService = apiService,
            ultraStarSongDao = ultraStarDao,
            yargSongDao = yargDao,
            syncMetadataDao = metadataDao,
            ioDispatcher = UnconfinedTestDispatcher(),
            clock = { currentTime },
            ultraStarUrl = mockWebServer.url("/ultrastar.json").toString(),
            yargUrl = mockWebServer.url("/yarg.json").toString(),
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `first sync downloads ultrastar songs and saves etag metadata`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("ETag", "W/\"us-v1\"")
                .setBody(SAMPLE_ULTRASTAR_JSON),
        )

        val result = repository.syncGame(GameType.ULTRASTAR)
        val request = mockWebServer.takeRequest()

        assertNull(request.getHeader("If-None-Match"))
        assertEquals(GameSyncStatus.UPDATED, result.status)
        assertEquals(1, result.songCount)
        assertEquals(1, ultraStarDao.getCount())

        val meta = metadataDao.getMetadata(GameType.ULTRASTAR.name)
        assertEquals("W/\"us-v1\"", meta?.etag)
        assertEquals("2026-11-20T01:49:48", meta?.generatedAt)
        assertEquals(1_000L, meta?.lastSyncedAtMillis)
    }

    @Test
    fun `second sync sends if-none-match and handles 304 without rewriting database`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("ETag", "W/\"yarg-v1\"")
                .setBody(SAMPLE_YARG_JSON),
        )
        repository.syncGame(GameType.YARG)
        mockWebServer.takeRequest()

        currentTime = 2_500L
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(304)
                .addHeader("ETag", "W/\"yarg-v1\""),
        )

        val secondResult = repository.syncGame(GameType.YARG)
        val secondRequest = mockWebServer.takeRequest()

        assertEquals("W/\"yarg-v1\"", secondRequest.getHeader("If-None-Match"))
        assertEquals(GameSyncStatus.UP_TO_DATE, secondResult.status)
        assertEquals(1, secondResult.songCount)
        assertEquals(1, yargDao.replaceCount)
        assertEquals(2_500L, metadataDao.getMetadata(GameType.YARG.name)?.lastSyncedAtMillis)
    }

    @Test
    fun `forceRefresh omits if-none-match header and replaces cached songs`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("ETag", "W/\"us-v1\"")
                .setBody(SAMPLE_ULTRASTAR_JSON),
        )
        repository.syncGame(GameType.ULTRASTAR)
        mockWebServer.takeRequest()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("ETag", "W/\"us-v2\"")
                .setBody(SAMPLE_ULTRASTAR_JSON),
        )

        val result = repository.syncGame(GameType.ULTRASTAR, forceRefresh = true)
        val request = mockWebServer.takeRequest()

        assertNull(request.getHeader("If-None-Match"))
        assertEquals(GameSyncStatus.UPDATED, result.status)
        assertEquals("W/\"us-v2\"", metadataDao.getMetadata(GameType.ULTRASTAR.name)?.etag)
        assertEquals(2, ultraStarDao.replaceCount)
    }

    @Test
    fun `http error falls back to offline cached when database has songs`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("ETag", "W/\"us-v1\"")
                .setBody(SAMPLE_ULTRASTAR_JSON),
        )
        repository.syncGame(GameType.ULTRASTAR)
        mockWebServer.takeRequest()

        mockWebServer.enqueue(MockResponse().setResponseCode(503))

        val result = repository.syncGame(GameType.ULTRASTAR)

        assertEquals(GameSyncStatus.OFFLINE_CACHED, result.status)
        assertEquals(1, result.songCount)
    }

    @Test
    fun `http error returns error status when database is empty`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = repository.syncGame(GameType.YARG)

        assertEquals(GameSyncStatus.ERROR, result.status)
        assertEquals(0, result.songCount)
    }

    companion object {
        private val SAMPLE_ULTRASTAR_JSON = """
            {
              "generated": "2026-11-20T01:49:48",
              "song_count": 1,
              "songs": [
                {
                  "title": "Attack",
                  "artist": "30 Seconds To Mars",
                  "year": 2005,
                  "language": "English",
                  "genre": "Rock",
                  "duet": false,
                  "solo": true,
                  "song_length_ms": 181939,
                  "song_length": "3:02",
                  "sources": ["Attack.txt"]
                }
              ]
            }
        """.trimIndent()

        private val SAMPLE_YARG_JSON = """
            {
              "generated": "2026-11-20T01:17:48",
              "song_count": 1,
              "songs": [
                {
                  "name": "Ghost I Own",
                  "artist": "(Damn) This Desert Air",
                  "album": "Distance Waits",
                  "year": 2010,
                  "genre": "Alternative",
                  "song_length_ms": 219682,
                  "song_length": "3:40",
                  "instruments": {
                    "guitar": 3,
                    "drums": 4
                  },
                  "source": "chart.sng",
                  "format": "sng"
                }
              ]
            }
        """.trimIndent()
    }
}

private class FakeUltraStarSongDao : UltraStarSongDao {
    private val items = MutableStateFlow<List<UltraStarSongEntity>>(emptyList())
    var replaceCount = 0

    override fun observeAll(): Flow<List<UltraStarSongEntity>> = items
    override fun observeFiltered(
        query: String,
        genre: String?,
        language: String?,
        minYear: Int?,
        maxYear: Int?,
        duet: Boolean?,
        solo: Boolean?,
    ): Flow<List<UltraStarSongEntity>> = items

    override suspend fun getById(id: Long): UltraStarSongEntity? = items.value.find { it.id == id }
    override fun observeCount(): Flow<Int> = items.map { it.size }
    override suspend fun getCount(): Int = items.value.size
    override fun observeDistinctGenres(): Flow<List<String>> =
        items.map { list -> list.mapNotNull { it.genre }.distinct().sorted() }
    override fun observeDistinctLanguages(): Flow<List<String>> =
        items.map { list -> list.mapNotNull { it.language }.distinct().sorted() }
    override fun observeDistinctYears(): Flow<List<Int>> =
        items.map { list -> list.mapNotNull { it.year }.distinct().sorted() }

    override suspend fun insertAll(songs: List<UltraStarSongEntity>) {
        items.value = songs.mapIndexed { idx, song -> song.copy(id = (idx + 1).toLong()) }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }

    override suspend fun replaceAll(songs: List<UltraStarSongEntity>) {
        replaceCount++
        deleteAll()
        insertAll(songs)
    }
}

private class FakeYargSongDao : YargSongDao {
    private val items = MutableStateFlow<List<YargSongEntity>>(emptyList())
    var replaceCount = 0

    override fun observeAll(): Flow<List<YargSongEntity>> = items
    override fun observeFiltered(
        query: String,
        genre: String?,
        album: String?,
        format: String?,
        minYear: Int?,
        maxYear: Int?,
        requireGuitar: Boolean,
        minGuitar: Int?,
        requireBass: Boolean,
        minBass: Int?,
        requireDrums: Boolean,
        minDrums: Int?,
        requireKeys: Boolean,
        minKeys: Int?,
        requireVocals: Boolean,
        minVocals: Int?,
    ): Flow<List<YargSongEntity>> = items

    override suspend fun getById(id: Long): YargSongEntity? = items.value.find { it.id == id }
    override fun observeCount(): Flow<Int> = items.map { it.size }
    override suspend fun getCount(): Int = items.value.size
    override fun observeDistinctGenres(): Flow<List<String>> =
        items.map { list -> list.mapNotNull { it.genre }.distinct().sorted() }
    override fun observeDistinctFormats(): Flow<List<String>> =
        items.map { list -> list.mapNotNull { it.format }.distinct().sorted() }
    override fun observeDistinctYears(): Flow<List<Int>> =
        items.map { list -> list.mapNotNull { it.year }.distinct().sorted() }

    override suspend fun insertAll(songs: List<YargSongEntity>) {
        items.value = songs.mapIndexed { idx, song -> song.copy(id = (idx + 1).toLong()) }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }

    override suspend fun replaceAll(songs: List<YargSongEntity>) {
        replaceCount++
        deleteAll()
        insertAll(songs)
    }
}

private class FakeSyncMetadataDao : SyncMetadataDao {
    private val store = MutableStateFlow<Map<String, SyncMetadataEntity>>(emptyMap())

    override suspend fun getMetadata(gameType: String): SyncMetadataEntity? = store.value[gameType]
    override fun observeMetadata(gameType: String): Flow<SyncMetadataEntity?> =
        store.map { it[gameType] }
    override fun observeAllMetadata(): Flow<List<SyncMetadataEntity>> =
        store.map { it.values.toList() }

    override suspend fun upsert(metadata: SyncMetadataEntity) {
        store.value = store.value + (metadata.gameType to metadata)
    }
}
