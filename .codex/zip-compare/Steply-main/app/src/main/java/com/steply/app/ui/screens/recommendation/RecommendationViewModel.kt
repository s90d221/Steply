package com.steply.app.ui.screens.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.steply.app.AppContainer
import com.steply.app.data.local.entities.toDomain
import com.steply.app.domain.model.ExerciseRecommendation
import com.steply.app.ui.text.SteplyCopy
import com.steply.app.ui.viewmodel.viewModelFactory
import com.steply.app.util.formatDurationShort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecommendationUiState(
    val isLoading: Boolean = true,
    val selectedUserName: String? = null,
    val recommendations: List<ExerciseRecommendationUiItem> = emptyList(),
    val message: String? = null,
    val errorMessage: String? = null,
)

data class ExerciseRecommendationUiItem(
    val id: String,
    val title: String,
    val description: String,
    val safetyNote: String,
    val durationText: String,
    val isCompleted: Boolean,
)

class RecommendationViewModel(
    private val appContainer: AppContainer,
    private val sessionId: String?,
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)
    private val actionError = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val content = appContainer.settingsRepository.selectedUserId.flatMapLatest { selectedUserId ->
        if (selectedUserId == null) {
            flowOf(
                RecommendationContent(
                    errorMessage = SteplyCopy.ChooseProfileToBegin,
                ),
            )
        } else {
            combine(
                appContainer.userProfileRepository.observeProfileById(selectedUserId),
                observeRecommendations(selectedUserId),
            ) { selectedUser, recommendations ->
                if (selectedUser == null) {
                    RecommendationContent(
                        errorMessage = SteplyCopy.ChooseProfileAgain,
                    )
                } else {
                    RecommendationContent(
                        selectedUserName = selectedUser.displayName,
                        recommendations = recommendations,
                    )
                }
            }
        }
    }

    val uiState = combine(
        content,
        message,
        actionError,
    ) { content, message, actionError ->
        RecommendationUiState(
            isLoading = false,
            selectedUserName = content.selectedUserName,
            recommendations = content.recommendations,
            message = message,
            errorMessage = actionError ?: content.errorMessage,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RecommendationUiState(),
        )

    fun markCompleted(recommendationId: String) {
        viewModelScope.launch {
            val selectedUserId = appContainer.settingsRepository.selectedUserId.first()
            if (selectedUserId == null) {
                actionError.value = SteplyCopy.ChooseProfileToBegin
                return@launch
            }

            runCatching {
                appContainer.exerciseRecommendationRepository.markCompletedForUser(
                    id = recommendationId,
                    userId = selectedUserId,
                )
            }.onSuccess { updated ->
                if (updated) {
                    actionError.value = null
                    message.value = "Nice work. Your exercise is marked completed."
                } else {
                    actionError.value = "Please check this recommendation again."
                }
            }.onFailure {
                actionError.value = SteplyCopy.GenericError
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRecommendations(selectedUserId: String): Flow<List<ExerciseRecommendationUiItem>> {
        return when {
            sessionId != null -> {
                appContainer.exerciseRecommendationRepository.observeRecommendationsForUserSession(
                    userId = selectedUserId,
                    sessionId = sessionId,
                ).map { recommendations ->
                    recommendations.map { it.toDomain().toUiItem() }
                }
            }
            else -> {
                appContainer.exerciseRecommendationRepository.observeRecommendationsForUser(selectedUserId)
                    .map { recommendations ->
                        recommendations.map { it.toDomain().toUiItem() }
                    }
            }
        }
    }

    companion object {
        fun factory(
            appContainer: AppContainer,
            sessionId: String? = null,
        ) = viewModelFactory {
            RecommendationViewModel(appContainer, sessionId)
        }
    }
}

private data class RecommendationContent(
    val selectedUserName: String? = null,
    val recommendations: List<ExerciseRecommendationUiItem> = emptyList(),
    val errorMessage: String? = null,
)

private fun ExerciseRecommendation.toUiItem(): ExerciseRecommendationUiItem {
    return ExerciseRecommendationUiItem(
        id = id,
        title = title,
        description = description,
        safetyNote = safetyNote,
        durationText = "${formatDurationShort(durationSeconds)} · gentle",
        isCompleted = completedAt != null,
    )
}
