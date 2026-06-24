package com.steply.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steply.app.AppContainer
import com.steply.app.domain.model.UserProfile
import com.steply.app.domain.usecase.CalculateHistorySummaryUseCase
import com.steply.app.ui.text.SteplyCopy
import com.steply.app.ui.viewmodel.viewModelFactory
import com.steply.app.util.formatAverageCount
import com.steply.app.util.formatChairStandCount
import com.steply.app.util.formatChairStandDurationLabel
import com.steply.app.util.formatChairStandUnitLabel
import com.steply.app.util.formatDisplayDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val isLoading: Boolean = true,
    val selectedUser: UserProfile? = null,
    val results: List<HistoryResultUiItem> = emptyList(),
    val latestCountText: String = "-",
    val bestCountText: String = "-",
    val averageCountText: String = "-",
    val trendMessage: String? = null,
    val errorMessage: String? = null,
)

data class HistoryResultUiItem(
    val resultId: String,
    val createdAtText: String,
    val durationText: String,
    val repetitionCountText: String,
    val chairStandUnitLabel: String,
    val recommendationLevel: String,
)

class HistoryViewModel(
    appContainer: AppContainer,
) : ViewModel() {
    val uiState = combine(
        appContainer.observeSelectedProfileUseCase(),
        appContainer.observeChairStandHistoryUseCase(),
    ) { selectedProfile, history ->
        if (selectedProfile == null) {
            return@combine HistoryUiState(
                isLoading = false,
                errorMessage = SteplyCopy.ChooseProfileToBegin,
            )
        }

        val results = history.filter { it.userId == selectedProfile.id }
        val summary = CalculateHistorySummaryUseCase(results)

        HistoryUiState(
            isLoading = false,
            selectedUser = selectedProfile,
            results = results.map { it.toUiItem() },
            latestCountText = summary.latestCount?.let(::formatChairStandCount) ?: "-",
            bestCountText = summary.bestCount?.let(::formatChairStandCount) ?: "-",
            averageCountText = summary.averageCount?.let(::formatAverageCount) ?: "-",
            trendMessage = summary.trendMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HistoryUiState(),
    )

    companion object {
        fun factory(appContainer: AppContainer) = viewModelFactory {
            HistoryViewModel(appContainer)
        }
    }
}

private fun com.steply.app.domain.model.ChairStandHistoryItem.toUiItem(): HistoryResultUiItem {
    return HistoryResultUiItem(
        resultId = resultId,
        createdAtText = formatDisplayDateTime(createdAt),
        durationText = formatChairStandDurationLabel(durationSeconds),
        repetitionCountText = formatChairStandCount(repetitionCount),
        chairStandUnitLabel = formatChairStandUnitLabel(),
        recommendationLevel = recommendationLevel,
    )
}
