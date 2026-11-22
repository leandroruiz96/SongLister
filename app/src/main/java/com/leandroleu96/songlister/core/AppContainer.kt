package com.leandroleu96.songlister.core

import android.content.Context
import com.leandroleu96.songlister.core.network.NetworkFactory
import com.leandroleu96.songlister.data.local.SongListerDatabase
import com.leandroleu96.songlister.data.remote.GistApiService
import com.leandroleu96.songlister.data.repository.DefaultSongRepository
import com.leandroleu96.songlister.data.repository.SongRepository

interface AppContainer {
    val database: SongListerDatabase
    val apiService: GistApiService
    val songRepository: SongRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val database: SongListerDatabase by lazy {
        SongListerDatabase.create(context)
    }

    override val apiService: GistApiService by lazy {
        NetworkFactory.createRetrofit().create(GistApiService::class.java)
    }

    override val songRepository: SongRepository by lazy {
        DefaultSongRepository(
            apiService = apiService,
            ultraStarSongDao = database.ultraStarSongDao(),
            yargSongDao = database.yargSongDao(),
            syncMetadataDao = database.syncMetadataDao(),
        )
    }
}
