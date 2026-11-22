package com.leandroleu96.songlister.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leandroleu96.songlister.R
import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.local.entity.UltraStarSongEntity
import com.leandroleu96.songlister.data.local.entity.YargSongEntity

@Composable
fun SongDetailScreen(
    gameType: GameType,
    viewModel: SongDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SongDetailScreenContent(
        gameType = gameType,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreenContent(
    gameType: GameType,
    uiState: SongDetailUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.detail_title, gameType.displayName))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState) {
                SongDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                SongDetailUiState.NotFound -> {
                    Text(
                        text = stringResource(R.string.detail_not_found),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is SongDetailUiState.UltraStarLoaded -> {
                    UltraStarDetailContent(song = uiState.song)
                }

                is SongDetailUiState.YargLoaded -> {
                    YargDetailContent(song = uiState.song)
                }
            }
        }
    }
}

@Composable
private fun UltraStarDetailContent(song: UltraStarSongEntity) {
    val unknown = stringResource(R.string.value_unknown)
    val unspecified = stringResource(R.string.value_unspecified)
    val supported = stringResource(R.string.value_supported)
    val noLabel = stringResource(R.string.option_no)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SongHeaderCard(
            title = song.title,
            artist = song.artist,
            chips = listOfNotNull(
                song.year?.toString(),
                song.genre,
                song.language,
                song.songLength,
            ),
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.detail_section_attributes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                DetailRow(stringResource(R.string.detail_label_language), song.language ?: unknown)
                DetailRow(stringResource(R.string.detail_label_genre), song.genre ?: unspecified)
                DetailRow(stringResource(R.string.detail_label_year), song.year?.toString() ?: unknown)
                DetailRow(
                    stringResource(R.string.detail_label_duration),
                    formatDuration(song.songLength, song.songLengthMs),
                )
                DetailRow(stringResource(R.string.detail_label_solo), if (song.solo) supported else noLabel)
                DetailRow(stringResource(R.string.detail_label_duet), if (song.duet) supported else noLabel)
            }
        }

        if (song.sources.isNotEmpty()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.detail_section_sources, song.sources.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    song.sources.forEachIndexed { index, path ->
                        if (index > 0) HorizontalDivider()
                        Text(
                            text = path,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YargDetailContent(song: YargSongEntity) {
    val unknown = stringResource(R.string.value_unknown)
    val unspecified = stringResource(R.string.value_unspecified)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SongHeaderCard(
            title = song.name,
            artist = song.artist,
            chips = listOfNotNull(
                song.year?.toString(),
                song.genre,
                song.format?.uppercase(),
                song.songLength,
            ),
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.detail_section_instruments),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                InstrumentDifficultyRow(stringResource(R.string.instrument_guitar), song.guitarLevel)
                InstrumentDifficultyRow(stringResource(R.string.instrument_bass), song.bassLevel)
                InstrumentDifficultyRow(stringResource(R.string.instrument_drums), song.drumsLevel)
                InstrumentDifficultyRow(stringResource(R.string.instrument_keys), song.keysLevel)
                InstrumentDifficultyRow(stringResource(R.string.instrument_vocals), song.vocalsLevel)
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.detail_section_metadata),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                DetailRow(stringResource(R.string.detail_label_album), song.album ?: unknown)
                DetailRow(stringResource(R.string.detail_label_genre), song.genre ?: unspecified)
                DetailRow(stringResource(R.string.detail_label_year), song.year?.toString() ?: unknown)
                DetailRow(
                    stringResource(R.string.detail_label_duration),
                    formatDuration(song.songLength, song.songLengthMs),
                )
                DetailRow(stringResource(R.string.detail_label_format), song.format?.uppercase() ?: unknown)
            }
        }

        if (!song.source.isNullOrBlank()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.detail_section_chart_source),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = song.source,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongHeaderCard(
    title: String,
    artist: String,
    chips: List<String>,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (chips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    chips.forEach { label ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun InstrumentDifficultyRow(
    instrument: String,
    level: Int?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = instrument,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = level?.let { stringResource(R.string.detail_instrument_tier, it) }
                    ?: stringResource(R.string.detail_instrument_not_charted),
                style = MaterialTheme.typography.labelMedium,
                color = if (level != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        if (level != null) {
            val fraction = (level.coerceIn(0, 6) / 6f).coerceAtLeast(0.08f)
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            )
        }
    }
}

@Composable
private fun formatDuration(formatted: String?, lengthMs: Long?): String {
    return when {
        formatted != null && lengthMs != null ->
            stringResource(R.string.detail_duration_with_seconds, formatted, lengthMs / 1000)
        formatted != null -> formatted
        lengthMs != null ->
            stringResource(R.string.detail_duration_seconds_only, lengthMs / 1000)
        else -> stringResource(R.string.value_unknown)
    }
}
