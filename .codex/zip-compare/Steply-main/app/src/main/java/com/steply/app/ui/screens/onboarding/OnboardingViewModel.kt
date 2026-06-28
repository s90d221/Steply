package com.steply.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steply.app.AppContainer
import com.steply.app.ui.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isSaving: Boolean = false,
    val completed: Boolean = false,
)

class OnboardingViewModel(
    private val appContainer: AppContainer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun completeOnboarding() {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            appContainer.settingsRepository.setOnboardingCompleted(true)
            _uiState.value = OnboardingUiState(completed = true)
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(completed = false) }
    }

    companion object {
        fun factory(appContainer: AppContainer) = viewModelFactory {
            OnboardingViewModel(appContainer)
        }
    }
}
