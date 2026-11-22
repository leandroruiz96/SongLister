package com.leandroleu96.songlister

import android.app.Application
import com.leandroleu96.songlister.core.AppContainer
import com.leandroleu96.songlister.core.DefaultAppContainer

class SongListerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
