package com.leandroleu96.songlister

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.leandroleu96.songlister.ui.navigation.SongListerNavHost
import com.leandroleu96.songlister.ui.theme.SongListerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as SongListerApplication).container
        setContent {
            SongListerTheme {
                SongListerNavHost(appContainer = appContainer)
            }
        }
    }
}
