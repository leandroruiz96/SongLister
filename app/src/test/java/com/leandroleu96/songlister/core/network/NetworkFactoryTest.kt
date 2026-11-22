package com.leandroleu96.songlister.core.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

class NetworkFactoryTest {

    @Serializable
    private data class SamplePayload(
        val title: String,
        val count: Int = 0,
    )

    private interface SampleApi {
        @GET("sample.json")
        suspend fun fetchSample(
            @Header("If-None-Match") ifNoneMatch: String? = null,
        ): Response<SamplePayload>
    }

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: SampleApi

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val retrofit = NetworkFactory.createRetrofit(
            baseUrl = mockWebServer.url("/").toString(),
        )
        api = retrofit.create(SampleApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `defaultJson ignores unknown keys and coerces defaults`() {
        val raw = """{"title": "Attack", "count": null, "unknownField": "ignored"}"""
        val decoded = NetworkFactory.defaultJson.decodeFromString<SamplePayload>(raw)
        assertEquals("Attack", decoded.title)
        assertEquals(0, decoded.count)
    }

    @Test
    fun `retrofit deserializes json response and exposes etag header`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("ETag", "W/\"abc123\"")
                .setBody("""{"title": "Song List", "count": 42, "extra": true}"""),
        )

        val response = api.fetchSample()

        assertTrue(response.isSuccessful)
        assertEquals(200, response.code())
        assertEquals("W/\"abc123\"", response.headers()["ETag"])
        assertEquals(SamplePayload(title = "Song List", count = 42), response.body())
    }

    @Test
    fun `retrofit sends if-none-match header and handles 304 not modified`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(304)
                .addHeader("ETag", "W/\"abc123\""),
        )

        val response = api.fetchSample(ifNoneMatch = "W/\"abc123\"")
        val recordedRequest = mockWebServer.takeRequest()

        assertEquals("W/\"abc123\"", recordedRequest.getHeader("If-None-Match"))
        assertFalse(response.isSuccessful)
        assertEquals(304, response.code())
        assertNull(response.body())
    }
}
