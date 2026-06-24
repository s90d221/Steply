package com.steply.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steply.app.AppContainer
import com.steply.app.domain.model.UserProfile
import com.steply.app.ui.text.SteplyCopy
import com.steply.app.ui.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddEditProfileUiState(
    val isEditMode: Boolean = false,
    val displayName: String = "",
    val birthYear: String = "",
    val gender: String = "",
    val heightCm: String = "",
    val mobilityNote: String = "",
    val emergencyNote: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

class AddEditProfileViewModel(
    private val appContainer: AppContainer,
    private val profileId: String?,
) : ViewModel() {
    private var loadedProfile: UserProfile? = null
    private val _uiState = MutableStateFlow(
        AddEditProfileUiState(
            isEditMode = profileId != null,
            isLoading = profileId != null,
        ),
    )
    val uiState: StateFlow<AddEditProfileUiState> = _uiState.asStateFlow()

    init {
        if (profileId != null) {
            viewModelScope.launch {
                val profile = appContainer.userProfileRepository.getProfileById(profileId)
                loadedProfile = profile
                if (profile == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "We could not load this profile. ${SteplyCopy.ChooseProfileAgain}",
                        )
                    }
                } else {
                    _uiState.value = AddEditProfileUiState(
                        isEditMode = true,
                        displayName = profile.displayName,
                        birthYear = profile.birthYear?.toString().orEmpty(),
                        gender = profile.gender.orEmpty(),
                        heightCm = profile.heightCm?.toString().orEmpty(),
                        mobilityNote = profile.mobilityNote.orEmpty(),
                        emergencyNote = profile.emergencyNote.orEmpty(),
                    )
                }
            }
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayName = value, errorMessage = null) }
    }

    fun onBirthYearChanged(value: String) {
        _uiState.update { it.copy(birthYear = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun onGenderChanged(value: String) {
        _uiState.update { it.copy(gender = value, errorMessage = null) }
    }

    fun onHeightCmChanged(value: String) {
        _uiState.update { it.copy(heightCm = value.filter(Char::isDigit), errorMessage = null) }
    }

    fun onMobilityNoteChanged(value: String) {
        _uiState.update { it.copy(mobilityNote = value, errorMessage = null) }
    }

    fun onEmergencyNoteChanged(value: String) {
        _uiState.update { it.copy(emergencyNote = value, errorMessage = null) }
    }

    fun save() {
        val current = _uiState.value
        if (current.isSaving) return

        val displayName = current.displayName.trim()
        val birthYear = current.birthYear.takeIf { it.isNotBlank() }?.toIntOrNull()
        val heightCm = current.heightCm.takeIf { it.isNotBlank() }?.toIntOrNull()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val error = when {
            displayName.isBlank() -> "Please enter a name or nickname."
            birthYear != null && birthYear !in 1900..currentYear -> "Please check the birth year."
            heightCm != null && heightCm !in 80..220 -> "Please enter a height between 80 cm and 220 cm."
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                val existing = loadedProfile
                if (existing == null) {
                    appContainer.userProfileRepository.create(
                        displayName = displayName,
                        birthYear = birthYear,
                        gender = current.gender,
                        heightCm = heightCm,
                        mobilityNote = current.mobilityNote,
                        emergencyNote = current.emergencyNote,
                    )
                } else {
                    appContainer.userProfileRepository.edit(
                        existing.copy(
                            displayName = displayName,
                            birthYear = birthYear,
                            gender = current.gender.trim().takeIf { it.isNotBlank() },
                            heightCm = heightCm,
                            mobilityNote = current.mobilityNote.trim().takeIf { it.isNotBlank() },
                            emergencyNote = current.emergencyNote.trim().takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
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

    fun onSavedNavigationHandled() {
        _uiState.update { it.copy(saved = false) }
    }

    companion object {
        fun factory(
            appContainer: AppContainer,
            profileId: String?,
        ) = viewModelFactory {
            AddEditProfileViewModel(appContainer, profileId)
        }
    }
}
