package com.leandroleu96.songlister.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.leandroleu96.songlister.core.GameType
import com.leandroleu96.songlister.data.repository.SongRepository
import com.leandroleu96.songlister.data.repository.UltraStarFilter
import com.leandroleu96.songlister.data.repository.UltraStarFilterOptions
import com.leandroleu96.songlister.data.repository.YargFilter
import com.leandroleu96.songlister.data.repository.YargFilterOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SongListViewModel(
    private val gameType: GameType,
    private val repository: SongRepository,
    debounceMillis: Long = 250L,
) : ViewModel() {

    private val searchQueryFlow = MutableStateFlow("")
    private val ultraStarFilterFlow = MutableStateFlow(UltraStarFilter())
    private val yargFilterFlow = MutableStateFlow(YargFilter())
    private val isRefreshingFlow = MutableStateFlow(false)

    private val debouncedQueryFlow: Flow<String> = if (debounceMillis > 0L) {
        searchQueryFlow.debounce(debounceMillis).distinctUntilChanged()
    } else {
        searchQueryFlow
    }

    private val filteredSongsFlow: Flow<List<SongListItemUi>> = when (gameType) {
        GameType.ULTRASTAR -> combine(
            debouncedQueryFlow,
            ultraStarFilterFlow,
        ) { query, filter ->
            filter.copy(query = query)
        }.flatMapLatest { combinedFilter ->
            repository.observeFilteredUltraStarSongs(combinedFilter)
                .map { songs -> songs.map { it.toListItemUi() } }
        }

        GameType.YARG -> combine(
            debouncedQueryFlow,
            yargFilterFlow,
        ) { query, filter ->
            filter.copy(query = query)
        }.flatMapLatest { combinedFilter ->
            repository.observeFilteredYargSongs(combinedFilter)
                .map { songs -> songs.map { it.toListItemUi() } }
        }
    }

    private val ultraStarOptionsFlow: Flow<UltraStarFilterOptions> =
        if (gameType == GameType.ULTRASTAR) {
            repository.observeUltraStarFilterOptions()
        } else {
            flowOf(UltraStarFilterOptions())
        }

    private val yargOptionsFlow: Flow<YargFilterOptions> =
        if (gameType == GameType.YARG) {
            repository.observeYargFilterOptions()
        } else {
            flowOf(YargFilterOptions())
        }

    val uiState: StateFlow<SongListUiState> = combine(
        combine(searchQueryFlow, filteredSongsFlow, isRefreshingFlow) { query, songs, refreshing ->
            Triple(query, songs, refreshing)
        },
        combine(
            ultraStarFilterFlow,
            yargFilterFlow,
            ultraStarOptionsFlow,
            yargOptionsFlow,
        ) { usFilter, yargFilter, usOptions, yargOptions ->
            FilterBundle(usFilter, yargFilter, usOptions, yargOptions)
        },
    ) { (query, songs, refreshing), filters ->
        SongListUiState(
            gameType = gameType,
            searchQuery = query,
            songs = songs,
            isLoading = false,
            isRefreshing = refreshing,
            ultraStarFilter = filters.ultraStarFilter,
            yargFilter = filters.yargFilter,
            ultraStarFilterOptions = filters.ultraStarFilterOptions,
            yargFilterOptions = filters.yargFilterOptions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SongListUiState(gameType = gameType),
    )

    fun onSearchQueryChange(query: String) {
        searchQueryFlow.value = query
    }

    fun onUpdateUltraStarFilter(filter: UltraStarFilter) {
        ultraStarFilterFlow.value = filter
    }

    fun onUpdateYargFilter(filter: YargFilter) {
        yargFilterFlow.value = filter
    }

    fun onClearFilters() {
        ultraStarFilterFlow.value = UltraStarFilter()
        yargFilterFlow.value = YargFilter()
    }

    fun onRefresh() {
        if (isRefreshingFlow.value) return
        viewModelScope.launch {
            isRefreshingFlow.value = true
            try {
                repository.syncGame(gameType, forceRefresh = true)
            } finally {
                isRefreshingFlow.value = false
            }
        }
    }

    private data class FilterBundle(
        val ultraStarFilter: UltraStarFilter,
        val yargFilter: YargFilter,
        val ultraStarFilterOptions: UltraStarFilterOptions,
        val yargFilterOptions: YargFilterOptions,
    )

    companion object {
        fun provideFactory(
            gameType: GameType,
            repository: SongRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SongListViewModel(
                    gameType = gameType,
                    repository = repository,
                ) as T
            }
        }
    }
}
