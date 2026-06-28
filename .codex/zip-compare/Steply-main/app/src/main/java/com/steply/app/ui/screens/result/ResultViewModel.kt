package com.steply.app.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steply.app.AppContainer
import com.steply.app.domain.model.ChairStandResultReport
import com.steply.app.domain.usecase.RecommendationLevelContent
import com.steply.app.domain.usecase.RecommendationLevelFeedback
import com.steply.app.util.formatChairStandUnitLabel
import com.steply.app.util.formatConfidencePercent
import com.steply.app.util.formatDisplayDateTime
import com.steply.app.ui.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ResultUiState(
    val isLoading: Boolean = true,
    val report: ChairStandResultReport? = null,
    val feedback: RecommendationLevelFeedback? = null,
    val completedAtText: String = "",
    val confidencePercentText: String = "",
    val chairStandUnitLabel: String = "",
    val shouldChooseProfile: Boolean = false,
    val errorMessage: String? = null,
)

class ResultViewModel(
    private val appContainer: AppContainer,
    resultId: String,
) : ViewModel() {
    private val generationError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            runCatching {
                val selectedUserId = appContainer.settingsRepository.selectedUserId.first()
                    ?: return@launch
                val result = appContainer.screeningRepository.getResultById(resultId)
                    ?: return@launch
                if (result.userId != selectedUserId) return@launch
                val session = appContainer.screeningRepository.getSessionById(result.sessionId)
                    ?: return@launch
                if (session.userId != selectedUserId) return@launch

                appContainer.generateExerciseRecommendationsUseCase(
                    userId = result.userId,
                    sessionId = session.id,
                    result = result,
                )
            }.onFailure {
                generationError.value = "Something went wrong while preparing exercises. Your result was saved."
            }
        }
    }

    val uiState = combine(
        appContainer.observeChairStandResultReportUseCase(resultId),
        appContainer.settingsRepository.selectedUserId,
        generationError,
    ) { report, selectedUserId, errorMessage ->
        ResultUiState(
            isLoading = false,
            report = report,
            feedback = report?.recommendationLevel?.let(RecommendationLevelContent::feedback),
            completedAtText = report?.let { formatDisplayDateTime(it.completedAt) }.orEmpty(),
            confidencePercentText = report?.let { formatConfidencePercent(it.confidence) }.orEmpty(),
            chairStandUnitLabel = if (report == null) "" else formatChairStandUnitLabel(),
            shouldChooseProfile = selectedUserId == null,
            errorMessage = errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ResultUiState(),
    )

    companion object {
        fun factory(
            appContainer: AppContainer,
            resultId: String,
        ) = viewModelFactory {
            ResultViewModel(appContainer, resultId)
        }
    }
}
