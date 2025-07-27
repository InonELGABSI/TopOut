package com.topout.kmp.features.live_session
import com.topout.kmp.domain.LiveSessionManager
import com.topout.kmp.features.BaseViewModel
import com.topout.kmp.models.TrackPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.cancellation.CancellationException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class LiveSessionViewModel(
    private val useCases: LiveSessionUseCases,
) : BaseViewModel(),KoinComponent {

    private val _uiState = MutableStateFlow<LiveSessionState>(LiveSessionState.Loading)
    val uiState: StateFlow<LiveSessionState> = _uiState

    // MSL Height state
    private val _mslHeightState = MutableStateFlow<MSLHeightState>(MSLHeightState.Loading)
    val mslHeightState: StateFlow<MSLHeightState> = _mslHeightState

    private var trackPointJob: Job? = null
    private var historyTrackPointsJob: Job? = null

    // 1. Hold references to manager and scope per session
    private var liveSessionManager: LiveSessionManager? = null
    private var sessionScope: CoroutineScope? = null

    // Store current state to combine live point with history
    private var currentTrackPoint: TrackPoint? = null
    private var currentHistoryPoints: List<TrackPoint> = emptyList()

    init {
        // Load current MSL height on initialization
        loadCurrentMSLHeight()
    }

    fun onStartClicked() {
        // 2. Always stop/clean old session before starting new
        stopSessionAndCleanup()

        _uiState.value = LiveSessionState.Loading

        // 3. Create a fresh scope and manager for this session!
        sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        liveSessionManager = inject<LiveSessionManager> { parametersOf(sessionScope) }.value

        trackPointJob = sessionScope!!.launch {
            try {
                val trackPointFlow = liveSessionManager!!.invoke()
                trackPointFlow.collect { point: TrackPoint ->
                    currentTrackPoint = point
                    updateUIState()

                    // Start history tracking once we have a session ID
                    if (historyTrackPointsJob == null) {
                        startHistoryTracking(point.sessionId)
                    }
                }
            } catch (ce: CancellationException) {
                // cancellation (user stopped) – ignore
            } catch (e: Exception) {
                _uiState.value = LiveSessionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun startHistoryTracking(sessionId: String) {
        historyTrackPointsJob = sessionScope!!.launch {
            try {
                useCases.getLocalTrackPointsFlow(sessionId).collect { historyPoints ->
                    currentHistoryPoints = historyPoints
                    updateUIState()
                }
            } catch (ce: CancellationException) {
                // cancellation (user stopped) – ignore
            } catch (e: Exception) {
                // Log error but don't break the session for history tracking failure
                println("Error tracking history points: ${e.message}")
            }
        }
    }

    private fun updateUIState() {
        currentTrackPoint?.let { trackPoint ->
            _uiState.value = LiveSessionState.Loaded(
                trackPoint = trackPoint,
                historyTrackPoints = currentHistoryPoints
            )
        }
    }

    fun onStopClicked(sessionId: String) {
        // Always cancel jobs and manager
        stopSessionAndCleanup()

        _uiState.value = LiveSessionState.Stopping

        // Finish session logic
        scope.launch {
            try {
                // Finish and save the session (if needed)
                useCases.finishSession(sessionId)
                _uiState.value = LiveSessionState.SessionStopped(sessionId)
            } catch (e: Exception) {
                _uiState.value = LiveSessionState.Error(e.message ?: "Error finishing session")
            }
        }
    }

    fun onCancelClicked(sessionId: String) {
        // Always cancel jobs and manager first
        stopSessionAndCleanup()

        _uiState.value = LiveSessionState.Stopping

        // Cancel and delete session locally without saving to remote
        scope.launch {
            try {
                // Delete the session and its track points locally
                useCases.cancelLocalSession(sessionId)
                resetToInitialState()
            } catch (e: Exception) {
                _uiState.value = LiveSessionState.Error(e.message ?: "Error cancelling session")
            }
        }
    }

    fun resetToInitialState() {
        stopSessionAndCleanup()
        _uiState.value = LiveSessionState.Loading
    }

    // 4. Helper for fully stopping and cleaning up any running session
    private fun stopSessionAndCleanup() {
        // Stop manager and clean up scope/jobs
        liveSessionManager?.stop()
        liveSessionManager = null

        trackPointJob?.cancel()
        trackPointJob = null

        historyTrackPointsJob?.cancel()
        historyTrackPointsJob = null

        // Properly cancel the scope
        sessionScope?.coroutineContext?.get(Job)?.cancel()
        sessionScope = null

        // Reset combined state
        currentTrackPoint = null
        currentHistoryPoints = emptyList()
    }

    private fun loadCurrentMSLHeight() {
        scope.launch {
            _mslHeightState.value = MSLHeightState.Loading
            try {
                when (val result = useCases.getCurrentMSLHeight()) {
                    is com.topout.kmp.data.Result.Success -> {
                        result.data?.let { data ->
                            _mslHeightState.value = MSLHeightState.Success(data)
                        } ?: run {
                            _mslHeightState.value = MSLHeightState.Error("No MSL data available")
                        }
                    }
                    is com.topout.kmp.data.Result.Failure -> {
                        _mslHeightState.value = MSLHeightState.Error(
                            result.error?.message ?: "Failed to get MSL height"
                        )
                    }
                }
            } catch (e: Exception) {
                _mslHeightState.value = MSLHeightState.Error(
                    e.message ?: "Unknown error getting MSL height"
                )
            }
        }
    }

    fun refreshMSLHeight() {
        loadCurrentMSLHeight()
    }
}
