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

    init {
        fetchSessions()
    }

    fun fetchSessions() {
        scope.launch {
            when (val result = useCases.getSessions()) {
                is Result.Success -> {
                    originalSessions = result.data?.items ?: emptyList()
                    applySorting(currentSortOption)
                }
                is Result.Failure -> {
                    _uiState.emit(SessionsState.Error(errorMessage = result.error?.message ?: "N/A"))
                }
            }
        }
    }

    fun sortSessions(sortOption: SortOption) {
        currentSortOption = sortOption
        applySorting(sortOption)
    }

    private fun applySorting(sortOption: SortOption) {
        val sortedSessions = when (sortOption) {
            SortOption.DATE_NEWEST -> originalSessions.sortedBy { it.createdAt ?: 0L }
            SortOption.DATE_OLDEST -> originalSessions.sortedByDescending { it.createdAt ?: 0L }
            SortOption.DURATION_LONGEST -> originalSessions.sortedBy {
                calculateDuration(it.startTime, it.endTime)
            }
            SortOption.DURATION_SHORTEST -> originalSessions.sortedByDescending {
                calculateDuration(it.startTime, it.endTime)
            }
            SortOption.ASCENT_HIGHEST -> originalSessions.sortedBy { it.totalAscent ?: 0.0 }
            SortOption.ASCENT_LOWEST -> originalSessions.sortedByDescending { it.totalAscent ?: 0.0 }
        }

        scope.launch {
            _uiState.emit(SessionsState.Loaded(sortedSessions))
        }
    }

    private fun calculateDuration(startTime: Long?, endTime: Long?): Long {
        return if (startTime != null && endTime != null) {
            endTime - startTime
        } else {
            0L
        }
    }
}
