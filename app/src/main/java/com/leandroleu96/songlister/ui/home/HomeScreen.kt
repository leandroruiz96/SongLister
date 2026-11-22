package com.leandroleu96.songlister.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.repository.GameSyncStatus
import com.leandroleu96.songlister.ui.theme.SongListerTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSelectGame: (GameType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        uiState = uiState,
        onSelectGame = onSelectGame,
        onRefresh = { viewModel.syncAll(forceRefresh = true) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onSelectGame: (GameType) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("SongLister") },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !uiState.isSyncing,
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh song lists",
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            SyncStatusBanner(uiState = uiState)

            Text(
                text = "Choose a Game",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Select a library to search and filter songs offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            GameSelectionCard(
                state = uiState.ultraStar,
                subtitle = "Karaoke & vocal tracks",
                icon = Icons.Default.Mic,
                onClick = { onSelectGame(GameType.ULTRASTAR) },
            )

            GameSelectionCard(
                state = uiState.yarg,
                subtitle = "Full-band rhythm charts",
                icon = Icons.Default.MusicNote,
                onClick = { onSelectGame(GameType.YARG) },
            )
        }
    }
}

@Composable
private fun SyncStatusBanner(uiState: HomeUiState) {
    val (icon, message, containerColor, contentColor) = when {
        uiState.isSyncing -> StatusStyle(
            icon = Icons.Default.Refresh,
            message = "Checking for song list updates...",
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        uiState.hasAnyError -> StatusStyle(
            icon = Icons.Default.ErrorOutline,
            message = "Could not sync one or more lists. Tap refresh to retry.",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        uiState.isOfflineCached -> StatusStyle(
            icon = Icons.Default.CloudOff,
            message = "Offline mode — showing cached local song lists.",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        uiState.ultraStar.syncStatus != null || uiState.yarg.syncStatus != null -> StatusStyle(
            icon = Icons.Default.CloudDone,
            message = "Song libraries are synced and ready offline.",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        else -> return
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private data class StatusStyle(
    val icon: ImageVector,
    val message: String,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
)

@Composable
private fun GameSelectionCard(
    state: GameCardUiState,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    val countLabel = if (state.songCount > 0) {
        "${numberFormat.format(state.songCount)} songs cached"
    } else if (state.syncStatus == GameSyncStatus.ERROR) {
        "Download failed"
    } else {
        "Not downloaded yet"
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = state.gameType.displayName,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.gameType.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = countLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (state.syncStatus == GameSyncStatus.ERROR && state.songCount == 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open ${state.gameType.displayName}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SongListerTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isSyncing = false,
                ultraStar = GameCardUiState(
                    gameType = GameType.ULTRASTAR,
                    songCount = 423,
                    syncStatus = GameSyncStatus.UP_TO_DATE,
                ),
                yarg = GameCardUiState(
                    gameType = GameType.YARG,
                    songCount = 4747,
                    syncStatus = GameSyncStatus.UPDATED,
                ),
            ),
            onSelectGame = {},
            onRefresh = {},
        )
    }
}
