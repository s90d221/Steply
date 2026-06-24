package com.steply.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steply.app.AppContainer
import com.steply.app.data.local.entities.ChairStandResultEntity
import com.steply.app.data.local.entities.ExerciseRecommendationEntity
import com.steply.app.data.local.entities.ScreeningSessionEntity
import com.steply.app.domain.model.UserProfile
import com.steply.app.ui.text.SteplyCopy
import com.steply.app.ui.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val selectedUser: UserProfile? = null,
    val exportState: SettingsExportState = SettingsExportState.Idle,
    val deleteState: SettingsDeleteState = SettingsDeleteState.Idle,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

sealed interface SettingsExportState {
    data object Idle : SettingsExportState
    data object Exporting : SettingsExportState
    data class Ready(val json: String) : SettingsExportState
}

sealed interface SettingsDeleteState {
    data object Idle : SettingsDeleteState
    data object Deleting : SettingsDeleteState
}

class SettingsViewModel(
    private val appContainer: AppContainer,
) : ViewModel() {
    private val exportState = MutableStateFlow<SettingsExportState>(SettingsExportState.Idle)
    private val deleteState = MutableStateFlow<SettingsDeleteState>(SettingsDeleteState.Idle)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val successMessage = MutableStateFlow<String?>(null)

    val uiState = combine(
        appContainer.observeSelectedProfileUseCase(),
        exportState,
        deleteState,
        errorMessage,
        successMessage,
    ) { selectedUser, exportState, deleteState, errorMessage, successMessage ->
        SettingsUiState(
            isLoading = false,
            selectedUser = selectedUser,
            exportState = exportState,
            deleteState = deleteState,
            errorMessage = errorMessage,
            successMessage = successMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState(),
    )

    fun exportSelectedUserData() {
        viewModelScope.launch {
            exportState.value = SettingsExportState.Exporting
            errorMessage.value = null
            successMessage.value = null

            val userId = appContainer.settingsRepository.selectedUserId.first()
            if (userId == null) {
                exportState.value = SettingsExportState.Idle
                errorMessage.value = SteplyCopy.ChooseProfileToExport
                return@launch
            }

            runCatching {
                val profile = appContainer.userProfileRepository.getProfileById(userId)
                    ?: error("Selected user profile not found.")
                val sessions = appContainer.screeningRepository.observeSessionsForUser(userId).first()
                val results = appContainer.screeningRepository.observeResultsForUser(userId).first()
                val recommendations = appContainer.exerciseRecommendationRepository
                    .observeRecommendationsForUser(userId)
                    .first()

                buildExportJson(
                    profile = profile,
                    sessions = sessions,
                    results = results,
                    recommendations = recommendations,
                )
            }.onSuccess { json ->
                exportState.value = SettingsExportState.Ready(json)
                successMessage.value = "Current profile data is ready to export."
            }.onFailure {
                exportState.value = SettingsExportState.Idle
                errorMessage.value = SteplyCopy.GenericError
            }
        }
    }

    fun onExportShared() {
        if (exportState.value is SettingsExportState.Ready) {
            exportState.value = SettingsExportState.Idle
        }
    }

    fun deleteSelectedUserData(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteState.value = SettingsDeleteState.Deleting
            errorMessage.value = null
            successMessage.value = null

            val userId = appContainer.settingsRepository.selectedUserId.first()
            if (userId == null) {
                deleteState.value = SettingsDeleteState.Idle
                errorMessage.value = "There is no current profile to delete."
                return@launch
            }

            runCatching {
                appContainer.screeningRepository.deleteSelectedUserData(userId)
            }.onSuccess {
                deleteState.value = SettingsDeleteState.Idle
                onDeleted()
            }.onFailure {
                deleteState.value = SettingsDeleteState.Idle
                errorMessage.value = SteplyCopy.GenericError
            }
        }
    }

    fun deleteAllLocalData(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteState.value = SettingsDeleteState.Deleting
            errorMessage.value = null
            successMessage.value = null

            runCatching {
                appContainer.screeningRepository.deleteAllLocalData()
            }.onSuccess {
                deleteState.value = SettingsDeleteState.Idle
                onDeleted()
            }.onFailure {
                deleteState.value = SettingsDeleteState.Idle
                errorMessage.value = SteplyCopy.GenericError
            }
        }
    }

    companion object {
        fun factory(appContainer: AppContainer) = viewModelFactory {
            SettingsViewModel(appContainer)
        }
    }
}

private fun buildExportJson(
    profile: UserProfile,
    sessions: List<ScreeningSessionEntity>,
    results: List<ChairStandResultEntity>,
    recommendations: List<ExerciseRecommendationEntity>,
): String {
    return JSONObject()
        .put("app", "Steply")
        .put("exportedAt", System.currentTimeMillis())
        .put("profile", profile.toJson())
        .put("screeningSessions", JSONArray(sessions.map { it.toJson() }))
        .put("chairStandResults", JSONArray(results.map { it.toJson() }))
        .put("exerciseRecommendations", JSONArray(recommendations.map { it.toJson() }))
        .toString(2)
}

private fun UserProfile.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("displayName", displayName)
        .putNullable("birthYear", birthYear)
        .putNullable("gender", gender)
        .putNullable("heightCm", heightCm)
        .putNullable("mobilityNote", mobilityNote)
        .putNullable("emergencyNote", emergencyNote)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
        .putNullable("archivedAt", archivedAt)
}

private fun ScreeningSessionEntity.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("userId", userId)
        .put("type", type)
        .put("startedAt", startedAt)
        .put("completedAt", completedAt)
        .put("durationSeconds", durationSeconds)
        .put("confidence", confidence)
        .putNullable("notes", notes)
}

private fun ChairStandResultEntity.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("sessionId", sessionId)
        .put("userId", userId)
        .put("repetitionCount", repetitionCount)
        .putNullable("averageRepSeconds", averageRepSeconds)
        .putNullable("fastestRepSeconds", fastestRepSeconds)
        .putNullable("slowestRepSeconds", slowestRepSeconds)
        .putNullable("trunkLeanScore", trunkLeanScore)
        .putNullable("symmetryScore", symmetryScore)
        .putNullable("stabilityScore", stabilityScore)
        .put("recommendationLevel", recommendationLevel)
        .put("createdAt", createdAt)
}

private fun ExerciseRecommendationEntity.toJson(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("userId", userId)
        .putNullable("sessionId", sessionId)
        .put("title", title)
        .put("description", description)
        .put("safetyNote", safetyNote)
        .put("durationSeconds", durationSeconds)
        .put("createdAt", createdAt)
        .putNullable("completedAt", completedAt)
}

private fun JSONObject.putNullable(name: String, value: Any?): JSONObject {
    return put(name, value ?: JSONObject.NULL)
}
