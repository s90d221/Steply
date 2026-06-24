package com.steply.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steply.app.AppContainer
import com.steply.app.domain.model.UserProfile
import com.steply.app.util.formatChairStandCount
import com.steply.app.util.formatChairStandUnitLabel
import com.steply.app.util.formatDisplayDateTime
import com.steply.app.ui.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val selectedUserId: String? = null,
    val selectedProfile: UserProfile? = null,
    val latestResult: LatestChairStandResultSummary? = null,
    val shouldChooseProfile: Boolean = false,
)

data class LatestChairStandResultSummary(
    val createdAtText: String,
    val repetitionCountText: String,
    val chairStandUnitLabel: String,
    val recommendationLevel: String,
)

class HomeViewModel(
    appContainer: AppContainer,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val latestResult = appContainer.settingsRepository.selectedUserId
        .distinctUntilChanged()
        .flatMapLatest { selectedUserId ->
            if (selectedUserId == null) {
                flowOf(null)
            } else {
                appContainer.screeningRepository.observeLatestResultForUser(selectedUserId)
            }
        }

    val uiState = combine(
        appContainer.settingsRepository.selectedUserId,
        appContainer.observeSelectedProfileUseCase(),
        latestResult,
    ) { selectedUserId, profile, result ->
        HomeUiState(
            selectedUserId = selectedUserId,
            selectedProfile = profile,
            latestResult = result?.let {
                LatestChairStandResultSummary(
                    createdAtText = formatDisplayDateTime(it.createdAt),
                    repetitionCountText = formatChairStandCount(it.repetitionCount),
                    chairStandUnitLabel = formatChairStandUnitLabel(),
                    recommendationLevel = it.recommendationLevel,
                )
            },
            shouldChooseProfile = selectedUserId == null || profile == null,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HomeUiState(),
        )

    companion object {
        fun factory(appContainer: AppContainer) = viewModelFactory {
            HomeViewModel(appContainer)
        }
    }
}
