package com.leandroleu96.songlister.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.leandroleu96.songlister.R
import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.repository.UltraStarFilter
import com.leandroleu96.songlister.data.repository.UltraStarFilterOptions
import com.leandroleu96.songlister.data.repository.YargFilter
import com.leandroleu96.songlister.data.repository.YargFilterOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    uiState: SongListUiState,
    onDismiss: () -> Unit,
    onApplyUltraStarFilter: (UltraStarFilter) -> Unit,
    onApplyYargFilter: (YargFilter) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        when (uiState.gameType) {
            GameType.ULTRASTAR -> UltraStarFilterSheetContent(
                initialFilter = uiState.ultraStarFilter,
                options = uiState.ultraStarFilterOptions,
                onDismiss = onDismiss,
                onApply = {
                    onApplyUltraStarFilter(it)
                    onDismiss()
                },
            )

            GameType.YARG -> YargFilterSheetContent(
                initialFilter = uiState.yargFilter,
                options = uiState.yargFilterOptions,
                onDismiss = onDismiss,
                onApply = {
                    onApplyYargFilter(it)
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun UltraStarFilterSheetContent(
    initialFilter: UltraStarFilter,
    options: UltraStarFilterOptions,
    onDismiss: () -> Unit,
    onApply: (UltraStarFilter) -> Unit,
) {
    var draft by remember(initialFilter) { mutableStateOf(initialFilter) }
    var minYearText by remember(initialFilter) { mutableStateOf(initialFilter.minYear?.toString().orEmpty()) }
    var maxYearText by remember(initialFilter) { mutableStateOf(initialFilter.maxYear?.toString().orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
    ) {
        FilterSheetHeader(
            title = stringResource(R.string.filter_ultrastar_title),
            onReset = {
                draft = UltraStarFilter(query = draft.query)
                minYearText = ""
                maxYearText = ""
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DropdownFilterField(
                label = stringResource(R.string.filter_genre),
                selectedValue = draft.genre,
                options = options.genres,
                onSelect = { draft = draft.copy(genre = it) },
            )

            DropdownFilterField(
                label = stringResource(R.string.filter_language),
                selectedValue = draft.language,
                options = options.languages,
                onSelect = { draft = draft.copy(language = it) },
            )

            YearRangeSection(
                minYearText = minYearText,
                maxYearText = maxYearText,
                availableYears = options.years,
                onMinYearChange = { value ->
                    minYearText = value.filter { it.isDigit() }.take(4)
                    draft = draft.copy(minYear = minYearText.toIntOrNull())
                },
                onMaxYearChange = { value ->
                    maxYearText = value.filter { it.isDigit() }.take(4)
                    draft = draft.copy(maxYear = maxYearText.toIntOrNull())
                },
            )

            TriStateBooleanSection(
                label = stringResource(R.string.filter_duet),
                value = draft.duet,
                onValueChange = { draft = draft.copy(duet = it) },
            )

            TriStateBooleanSection(
                label = stringResource(R.string.filter_solo),
                value = draft.solo,
                onValueChange = { draft = draft.copy(solo = it) },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        FilterSheetActions(
            onCancel = onDismiss,
            onApply = { onApply(draft) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YargFilterSheetContent(
    initialFilter: YargFilter,
    options: YargFilterOptions,
    onDismiss: () -> Unit,
    onApply: (YargFilter) -> Unit,
) {
    var draft by remember(initialFilter) { mutableStateOf(initialFilter) }
    var minYearText by remember(initialFilter) { mutableStateOf(initialFilter.minYear?.toString().orEmpty()) }
    var maxYearText by remember(initialFilter) { mutableStateOf(initialFilter.maxYear?.toString().orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
    ) {
        FilterSheetHeader(
            title = stringResource(R.string.filter_yarg_title),
            onReset = {
                draft = YargFilter(query = draft.query)
                minYearText = ""
                maxYearText = ""
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DropdownFilterField(
                label = stringResource(R.string.filter_genre),
                selectedValue = draft.genre,
                options = options.genres,
                onSelect = { draft = draft.copy(genre = it) },
            )

            OutlinedTextField(
                value = draft.album.orEmpty(),
                onValueChange = { draft = draft.copy(album = it.ifBlank { null }) },
                label = { Text(stringResource(R.string.filter_album)) },
                placeholder = { Text(stringResource(R.string.filter_album_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (options.formats.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.filter_format),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChip(
                            selected = draft.format == null,
                            onClick = { draft = draft.copy(format = null) },
                            label = { Text(stringResource(R.string.option_any)) },
                        )
                        options.formats.forEach { format ->
                            FilterChip(
                                selected = draft.format == format,
                                onClick = {
                                    draft = draft.copy(
                                        format = if (draft.format == format) null else format,
                                    )
                                },
                                label = { Text(format.uppercase()) },
                            )
                        }
                    }
                }
            }

            YearRangeSection(
                minYearText = minYearText,
                maxYearText = maxYearText,
                availableYears = options.years,
                onMinYearChange = { value ->
                    minYearText = value.filter { it.isDigit() }.take(4)
                    draft = draft.copy(minYear = minYearText.toIntOrNull())
                },
                onMaxYearChange = { value ->
                    maxYearText = value.filter { it.isDigit() }.take(4)
                    draft = draft.copy(maxYear = maxYearText.toIntOrNull())
                },
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.filter_instruments_title),
                    style = MaterialTheme.typography.labelLarge,
                )

                InstrumentFilterRow(
                    label = stringResource(R.string.instrument_guitar),
                    enabled = draft.requireGuitar || draft.minGuitar != null,
                    minLevel = draft.minGuitar,
                    onToggle = { enabled ->
                        draft = draft.copy(
                            requireGuitar = enabled,
                            minGuitar = if (enabled) draft.minGuitar else null,
                        )
                    },
                    onMinLevelChange = { level ->
                        draft = draft.copy(requireGuitar = true, minGuitar = level)
                    },
                )

                InstrumentFilterRow(
                    label = stringResource(R.string.instrument_bass),
                    enabled = draft.requireBass || draft.minBass != null,
                    minLevel = draft.minBass,
                    onToggle = { enabled ->
                        draft = draft.copy(
                            requireBass = enabled,
                            minBass = if (enabled) draft.minBass else null,
                        )
                    },
                    onMinLevelChange = { level ->
                        draft = draft.copy(requireBass = true, minBass = level)
                    },
                )

                InstrumentFilterRow(
                    label = stringResource(R.string.instrument_drums),
                    enabled = draft.requireDrums || draft.minDrums != null,
                    minLevel = draft.minDrums,
                    onToggle = { enabled ->
                        draft = draft.copy(
                            requireDrums = enabled,
                            minDrums = if (enabled) draft.minDrums else null,
                        )
                    },
                    onMinLevelChange = { level ->
                        draft = draft.copy(requireDrums = true, minDrums = level)
                    },
                )

                InstrumentFilterRow(
                    label = stringResource(R.string.instrument_keys),
                    enabled = draft.requireKeys || draft.minKeys != null,
                    minLevel = draft.minKeys,
                    onToggle = { enabled ->
                        draft = draft.copy(
                            requireKeys = enabled,
                            minKeys = if (enabled) draft.minKeys else null,
                        )
                    },
                    onMinLevelChange = { level ->
                        draft = draft.copy(requireKeys = true, minKeys = level)
                    },
                )

                InstrumentFilterRow(
                    label = stringResource(R.string.instrument_vocals),
                    enabled = draft.requireVocals || draft.minVocals != null,
                    minLevel = draft.minVocals,
                    onToggle = { enabled ->
                        draft = draft.copy(
                            requireVocals = enabled,
                            minVocals = if (enabled) draft.minVocals else null,
                        )
                    },
                    onMinLevelChange = { level ->
                        draft = draft.copy(requireVocals = true, minVocals = level)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        FilterSheetActions(
            onCancel = onDismiss,
            onApply = { onApply(draft) },
        )
    }
}

@Composable
private fun FilterSheetHeader(
    title: String,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onReset) {
            Text(stringResource(R.string.action_reset))
        }
    }
}

@Composable
private fun FilterSheetActions(
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.action_cancel))
        }
        Button(
            onClick = onApply,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.action_apply_filters))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownFilterField(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val anyLabel = stringResource(R.string.option_any)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedValue ?: anyLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(anyLabel) },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun YearRangeSection(
    minYearText: String,
    maxYearText: String,
    availableYears: List<Int>,
    onMinYearChange: (String) -> Unit,
    onMaxYearChange: (String) -> Unit,
) {
    val minHint = availableYears.firstOrNull()?.toString() ?: stringResource(R.string.filter_year_min_hint)
    val maxHint = availableYears.lastOrNull()?.toString() ?: stringResource(R.string.filter_year_max_hint)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.filter_year_range),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = minYearText,
                onValueChange = onMinYearChange,
                label = { Text(stringResource(R.string.filter_year_from)) },
                placeholder = { Text(minHint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = maxYearText,
                onValueChange = onMaxYearChange,
                label = { Text(stringResource(R.string.filter_year_to)) },
                placeholder = { Text(maxHint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TriStateBooleanSection(
    label: String,
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = value == null,
                onClick = { onValueChange(null) },
                label = { Text(stringResource(R.string.option_any)) },
            )
            FilterChip(
                selected = value == true,
                onClick = { onValueChange(true) },
                label = { Text(stringResource(R.string.option_yes)) },
            )
            FilterChip(
                selected = value == false,
                onClick = { onValueChange(false) },
                label = { Text(stringResource(R.string.option_no)) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InstrumentFilterRow(
    label: String,
    enabled: Boolean,
    minLevel: Int?,
    onToggle: (Boolean) -> Unit,
    onMinLevelChange: (Int?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = enabled,
                onClick = { onToggle(!enabled) },
                label = { Text(label) },
            )
            if (enabled) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = minLevel?.let { stringResource(R.string.filter_difficulty_min, it) }
                        ?: stringResource(R.string.filter_difficulty_any),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (enabled) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = minLevel == null,
                    onClick = { onMinLevelChange(null) },
                    label = { Text(stringResource(R.string.option_any)) },
                )
                (1..6).forEach { tier ->
                    FilterChip(
                        selected = minLevel == tier,
                        onClick = { onMinLevelChange(tier) },
                        label = { Text(stringResource(R.string.filter_tier_plus, tier)) },
                    )
                }
            }
        }
    }
}
