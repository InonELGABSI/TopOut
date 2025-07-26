package com.topout.kmp.features.sessions

import com.topout.kmp.features.BaseViewModel
import com.topout.kmp.models.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.topout.kmp.data.Result

enum class SortOption {
    DATE_NEWEST,
    DATE_OLDEST,
    DURATION_LONGEST,
    DURATION_SHORTEST,
    ASCENT_HIGHEST,
    ASCENT_LOWEST
}

class SessionsViewModel (
    private val useCases: SessionsUseCases
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<SessionsState> = MutableStateFlow<SessionsState>(SessionsState.Loading)
    val uiState:StateFlow<SessionsState> get()= _uiState

    private var originalSessions: List<Session> = emptyList()
    private var currentSortOption: SortOption = SortOption.DATE_NEWEST
    private var currentSearchText: String = ""

    // Expose current search text and sort option as state
    private val _currentSearchText = MutableStateFlow("")
    val currentSearchTextState: StateFlow<String> = _currentSearchText

    private val _currentSortOption = MutableStateFlow(SortOption.DATE_NEWEST)
    val currentSortOptionState: StateFlow<SortOption> = _currentSortOption

    init {
        fetchSessions()
    }

    fun fetchSessions() {
        scope.launch {
            when (val result = useCases.getSessions()) {
                is Result.Success -> {
                    originalSessions = result.data?.items ?: emptyList()
                    applyFiltersAndSorting()
                }
                is Result.Failure -> {
                    _uiState.emit(SessionsState.Error(errorMessage = result.error?.message ?: "N/A"))
                }
            }
        }
    }

    fun searchSessions(searchText: String) {
        currentSearchText = searchText
        _currentSearchText.value = searchText
        applyFiltersAndSorting()
    }

    fun sortSessions(sortOption: SortOption) {
        currentSortOption = sortOption
        _currentSortOption.value = sortOption
        applyFiltersAndSorting()
    }

    private fun applyFiltersAndSorting() {
        // First apply search filter
        val filteredSessions = if (currentSearchText.isBlank()) {
            originalSessions
        } else {
            originalSessions.filter { session ->
                val title = session.title ?: ""
                title.contains(currentSearchText, ignoreCase = true)
            }
        }

        // Then apply sorting
        val sortedSessions = when (currentSortOption) {
            SortOption.DATE_NEWEST -> filteredSessions.sortedBy { it.createdAt ?: 0L }
            SortOption.DATE_OLDEST -> filteredSessions.sortedByDescending { it.createdAt ?: 0L }
            SortOption.DURATION_LONGEST -> filteredSessions.sortedBy {
                calculateDuration(it.startTime, it.endTime)
            }
            SortOption.DURATION_SHORTEST -> filteredSessions.sortedByDescending {
                calculateDuration(it.startTime, it.endTime)
            }
            SortOption.ASCENT_HIGHEST -> filteredSessions.sortedBy { it.totalAscent ?: 0.0 }
            SortOption.ASCENT_LOWEST -> filteredSessions.sortedByDescending { it.totalAscent ?: 0.0 }
        }

        scope.launch {
            _uiState.emit(SessionsState.Loaded(sortedSessions))
        }
    }

    private fun applySorting(sortOption: SortOption) {
        // This method is deprecated, use applyFiltersAndSorting instead
        currentSortOption = sortOption
        applyFiltersAndSorting()
    }

    private fun calculateDuration(startTime: Long?, endTime: Long?): Long {
        return if (startTime != null && endTime != null) {
            endTime - startTime
        } else {
            0L
        }
    }
}
