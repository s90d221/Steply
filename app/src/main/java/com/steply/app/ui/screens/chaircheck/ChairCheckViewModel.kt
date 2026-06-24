package com.steply.app.ui.screens.chaircheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steply.app.AppContainer
import com.steply.app.analysis.ChairStandAnalyzer
import com.steply.app.analysis.MockChairStandAnalyzer
import com.steply.app.ui.text.SteplyCopy
import com.steply.app.ui.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TEST_DURATION_SECONDS = 30

enum class ChairStandCheckPhase {
    PREPARE,
    COUNTDOWN,
    ACTIVE,
    COMPLETED,
    CANCELLED,
}

data class ChairCheckUiState(
    val phase: ChairStandCheckPhase = ChairStandCheckPhase.PREPARE,
    val countdownNumber: Int? = null,
    val remainingSeconds: Int = TEST_DURATION_SECONDS,
    val repetitionCount: Int = 0,
    val isSaving: Boolean = false,
    val savedResultId: String? = null,
    val showStopConfirmation: Boolean = false,
    val errorMessage: String? = null,
)

class ChairCheckViewModel(
    private val appContainer: AppContainer,
    private val analyzer: ChairStandAnalyzer = MockChairStandAnalyzer(TEST_DURATION_SECONDS),
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChairCheckUiState())
    val uiState: StateFlow<ChairCheckUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var timerJob: Job? = null
    private var startedAtMillis: Long? = null
    private var sessionUserId: String? = null

    fun startCountdown() {
        if (_uiState.value.phase != ChairStandCheckPhase.PREPARE) return

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (number in 3 downTo 1) {
                _uiState.update {
                    it.copy(
                        phase = ChairStandCheckPhase.COUNTDOWN,
                        countdownNumber = number,
                        errorMessage = null,
                    )
                }
                delay(1_000L)
            }

            _uiState.update {
                it.copy(
                    phase = ChairStandCheckPhase.COUNTDOWN,
                    countdownNumber = 0,
                )
            }
            delay(700L)
            startActiveTimer()
        }
    }

    fun incrementRepetition() {
        val current = _uiState.value
        if (current.phase == ChairStandCheckPhase.ACTIVE && !current.isSaving) {
            analyzer.addManualRepetition()
            val analysisState = analyzer.getCurrentState()
            _uiState.update {
                it.copy(
                    repetitionCount = analysisState.repetitionCount,
                    errorMessage = analysisState.warningMessage,
                )
            }
        }
    }

    fun complete() {
        val current = _uiState.value
        if (current.phase != ChairStandCheckPhase.ACTIVE || current.isSaving) return

        timerJob?.cancel()
        saveCompletedResult()
    }

    fun requestStop() {
        val phase = _uiState.value.phase
        if (phase == ChairStandCheckPhase.ACTIVE || phase == ChairStandCheckPhase.COUNTDOWN) {
            _uiState.update { it.copy(showStopConfirmation = true) }
        }
    }

    fun dismissStopConfirmation() {
        _uiState.update { it.copy(showStopConfirmation = false) }
    }

    fun confirmStop() {
        countdownJob?.cancel()
        timerJob?.cancel()
        analyzer.reset()
        startedAtMillis = null
        sessionUserId = null
        _uiState.update {
            it.copy(
                phase = ChairStandCheckPhase.CANCELLED,
                showStopConfirmation = false,
                isSaving = false,
                errorMessage = null,
            )
        }
    }

    fun onResultNavigationHandled() {
        _uiState.update { it.copy(savedResultId = null) }
    }

    private suspend fun startActiveTimer() {
        val userId = appContainer.settingsRepository.selectedUserId.first()
        if (userId == null) {
            analyzer.reset()
            _uiState.update {
                it.copy(
                    phase = ChairStandCheckPhase.PREPARE,
                    countdownNumber = null,
                    errorMessage = SteplyCopy.ChooseProfileToBegin,
                )
            }
            return
        }

        val startedAt = System.currentTimeMillis()
        startedAtMillis = startedAt
        sessionUserId = userId
        analyzer.startSession(userId = userId, startedAt = startedAt)
        // TODO: Replace MockChairStandAnalyzer with MediaPipeChairStandAnalyzer.
        // TODO: Connect CameraX frame stream.
        // TODO: Convert MediaPipe pose landmarks to PoseFrame.
        // TODO: Add full-body visibility detection.
        // TODO: Add trunk lean, symmetry, and stability metrics.
        _uiState.update {
            it.copy(
                phase = ChairStandCheckPhase.ACTIVE,
                countdownNumber = null,
                remainingSeconds = TEST_DURATION_SECONDS,
                repetitionCount = 0,
                errorMessage = null,
            )
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.remainingSeconds > 0) {
                delay(1_000L)
                val nextRemaining = (_uiState.value.remainingSeconds - 1).coerceAtLeast(0)
                _uiState.update { it.copy(remainingSeconds = nextRemaining) }
                if (nextRemaining == 0) {
                    saveCompletedResult()
                    break
                }
            }
        }
    }

    private fun saveCompletedResult() {
        if (_uiState.value.isSaving || _uiState.value.phase == ChairStandCheckPhase.COMPLETED) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val userId = sessionUserId ?: appContainer.settingsRepository.selectedUserId.first()
            if (userId == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = SteplyCopy.ChooseProfileToBegin,
                    )
                }
                return@launch
            }

            val completedAt = System.currentTimeMillis()
            val analysisResult = analyzer.finishSession(completedAt)
            runCatching {
                appContainer.screeningRepository.saveChairStandScreening(
                    userId = userId,
                    repetitionCount = analysisResult.repetitionCount,
                    durationSeconds = TEST_DURATION_SECONDS,
                    startedAt = startedAtMillis ?: completedAt - TEST_DURATION_SECONDS * 1_000L,
                    completedAt = completedAt,
                    averageRepSeconds = analysisResult.averageRepSeconds,
                    fastestRepSeconds = analysisResult.fastestRepSeconds,
                    slowestRepSeconds = analysisResult.slowestRepSeconds,
                    trunkLeanScore = analysisResult.trunkLeanScore,
                    symmetryScore = analysisResult.symmetryScore,
                    stabilityScore = analysisResult.stabilityScore,
                    confidence = analysisResult.confidence,
                    recommendationLevel = analysisResult.recommendationLevel,
                    notes = "Demo manual count",
                )
            }.onSuccess { resultId ->
                _uiState.update {
                    it.copy(
                        phase = ChairStandCheckPhase.COMPLETED,
                        isSaving = false,
                        savedResultId = resultId,
                        repetitionCount = analysisResult.repetitionCount,
                    )
                }
                analyzer.reset()
                startedAtMillis = null
                sessionUserId = null
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = SteplyCopy.GenericError,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        timerJob?.cancel()
        analyzer.reset()
        super.onCleared()
    }

    companion object {
        fun factory(appContainer: AppContainer) = viewModelFactory {
            ChairCheckViewModel(appContainer)
        }
    }
}
