package com.topout.kmp.features.live_session
import com.topout.kmp.domain.LiveSessionManager
import com.topout.kmp.features.BaseViewModel
import com.topout.kmp.models.TrackPoint
import com.topout.kmp.domain.SessionBackgroundManager
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

open class LiveSessionViewModel(
    private val useCases: LiveSessionUseCases,
) : BaseViewModel<LiveSessionState>(),KoinComponent {

    private val _uiState = MutableStateFlow<LiveSessionState>(LiveSessionState.Loading)
    override val uiState: StateFlow<LiveSessionState> = _uiState

    private val _mslHeightState = MutableStateFlow<MSLHeightState>(MSLHeightState.Loading)
    val mslHeightState: StateFlow<MSLHeightState> = _mslHeightState

    private var trackPointJob: Job? = null
    private var historyTrackPointsJob: Job? = null

    private var liveSessionManager: LiveSessionManager? = null
    private var sessionScope: CoroutineScope? = null

    private var currentTrackPoint: TrackPoint? = null
    private var currentHistoryPoints: List<TrackPoint> = emptyList()

    private var isPaused: Boolean = false

    private val sessionBackgroundManager: SessionBackgroundManager by inject()

    init {
        loadCurrentMSLHeight()
    }

    open fun onStartClicked(): Boolean {
        return try {
            sessionBackgroundManager.startBackgroundSession()
            stopSessionAndCleanup()
            _uiState.value = LiveSessionState.Loading
            sessionScope = try {
                sessionBackgroundManager.getBackgroundScope() ?: CoroutineScope(SupervisorJob() + Dispatchers.Default)
            } catch (_: Exception) {
                CoroutineScope(SupervisorJob() + Dispatchers.Default)
            }
            liveSessionManager = inject<LiveSessionManager> { parametersOf(sessionScope) }.value
            trackPointJob = sessionScope!!.launch {
                try {
                    val trackPointFlow = liveSessionManager!!.invoke()
                    trackPointFlow.collect { point: TrackPoint ->
                        currentTrackPoint = point
                        if (isPaused) {
                            _uiState.value = LiveSessionState.Paused(
                                trackPoint = point,
                                historyTrackPoints = currentHistoryPoints
                            )
                        } else {
                            _uiState.value = LiveSessionState.Loaded(
                                trackPoint = point,
                                historyTrackPoints = currentHistoryPoints
                            )
                        }
                        if (historyTrackPointsJob == null) {
                            startHistoryTracking(point.sessionId)
                        }
                    }
                } catch (_: CancellationException) {
                    // ignore
                } catch (e: Exception) {
                    _uiState.value = LiveSessionState.Error(e.message ?: "Unknown error")
                }
            }
            true
        } catch (e: Exception) {
            _uiState.value = LiveSessionState.Error(e.message ?: "Unknown error starting session")
            false
        }
    }

    private fun startHistoryTracking(sessionId: String) {
        historyTrackPointsJob = sessionScope!!.launch {
            try {
                useCases.getLocalTrackPointsFlow(sessionId).collect { historyPoints ->
                    currentHistoryPoints = historyPoints
                    updateUIState()
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                println("Error tracking history points: ${e.message}")
            }
        }
    }

    private fun updateUIState() {
        currentTrackPoint?.let { trackPoint ->
            _uiState.value = if (isPaused) {
                LiveSessionState.Paused(trackPoint, currentHistoryPoints)
            } else {
                LiveSessionState.Loaded(trackPoint, currentHistoryPoints)
            }
        }
    }

    open fun onStopClicked(sessionId: String): Boolean {
        return try {
            sessionBackgroundManager.stopBackgroundSession()
            stopSessionAndCleanup()
            _uiState.value = LiveSessionState.Stopping
            scope.launch {
                try {
                    useCases.finishSession(sessionId)
                    _uiState.value = LiveSessionState.SessionStopped(sessionId)
                } catch (e: Exception) {
                    _uiState.value = LiveSessionState.Error(e.message ?: "Error finishing session")
                }
            }
            true
        } catch (e: Exception) {
            _uiState.value = LiveSessionState.Error(e.message ?: "Error stopping session")
            false
        }
    }

    open fun onCancelClicked(sessionId: String): Boolean {
        return try {
            sessionBackgroundManager.stopBackgroundSession()
            stopSessionAndCleanup()
            _uiState.value = LiveSessionState.Stopping
            scope.launch {
                try {
                    useCases.cancelLocalSession(sessionId)
                    resetToInitialState()
                } catch (e: Exception) {
                    _uiState.value = LiveSessionState.Error(e.message ?: "Error cancelling session")
                }
            }
            true
        } catch (e: Exception) {
            _uiState.value = LiveSessionState.Error(e.message ?: "Error cancelling session")
            false
        }
    }

    fun resetToInitialState() {
        stopSessionAndCleanup()
        _uiState.value = LiveSessionState.Loading
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
    open fun onPauseClicked(): Boolean {
        return try {
            liveSessionManager?.pause()
            isPaused = true
            val tp = currentTrackPoint
            if (tp != null) {
                _uiState.value = LiveSessionState.Paused(tp, currentHistoryPoints)
            }
            true
        } catch (e: Exception) {
            _uiState.value = LiveSessionState.Error(e.message ?: "Error pausing session")
            false
        }
    }

    fun refreshMSLHeight() {
        loadCurrentMSLHeight()
    }

    open fun onResumeClicked(): Boolean {
        return try {
            liveSessionManager?.resume()
            isPaused = false
            val tp = currentTrackPoint
            if (tp != null) {
                _uiState.value = LiveSessionState.Loaded(tp, currentHistoryPoints)
            }
            true
        } catch (e: Exception) {
            _uiState.value = LiveSessionState.Error(e.message ?: "Error resuming session")
            false
        }
    }

    private fun stopSessionAndCleanup() {
        liveSessionManager?.stop()
        liveSessionManager = null

        trackPointJob?.cancel()
        trackPointJob = null

        historyTrackPointsJob?.cancel()
        historyTrackPointsJob = null

        sessionScope?.coroutineContext?.get(Job)?.cancel()
        sessionScope = null

        currentTrackPoint = null
        currentHistoryPoints = emptyList()
        isPaused = false
    }

}