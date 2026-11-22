package com.leandroleu96.songlister.data.remote

import com.leandroleu96.songlister.core.GameType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface GistApiService {

    @GET
    suspend fun getUltraStarSongs(
        @Url url: String = GameType.ULTRASTAR.fullUrl,
        @Header("If-None-Match") ifNoneMatch: String? = null,
    ): Response<UltraStarResponseDto>

    @GET
    suspend fun getYargSongs(
        @Url url: String = GameType.YARG.fullUrl,
        @Header("If-None-Match") ifNoneMatch: String? = null,
    ): Response<YargResponseDto>
}
