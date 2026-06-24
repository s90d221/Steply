package com.steply.app.ui.screens.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.EmptyStateCard
import com.steply.app.ui.screens.components.ExerciseCard
import com.steply.app.ui.screens.components.LocalDataNoticeCard
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.text.SteplyCopy

@Composable
fun RecommendationScreen(
    uiState: RecommendationUiState,
    onBack: () -> Unit,
    onMarkCompleted: (String) -> Unit,
    onBackHome: () -> Unit,
    onOpenHistory: () -> Unit,
    onStartChairCheck: () -> Unit,
) {
    SteplyScaffold(
        title = "Recommended Exercises",
        subtitle = "Practice gently based on today's check.",
        onBack = onBack,
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            if (uiState.isLoading) {
                Text(text = "Loading recommended exercises.", style = MaterialTheme.typography.bodyLarge)
                return@SteplyScreenColumn
            }

            SteplyCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = "Move gently and keep support nearby.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                uiState.selectedUserName?.let { selectedUserName ->
                    Text(
                        text = "Recommended for $selectedUserName.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            LocalDataNoticeCard(text = "Exercise completion is stored only on this device.")
            SafetyNoticeCard(text = SteplyCopy.SafetyReminderInline)

            uiState.message?.let { message ->
                SteplyCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            uiState.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (uiState.recommendations.isEmpty()) {
                EmptyRecommendationState(
                    onStartChairCheck = onStartChairCheck,
                    onOpenHistory = onOpenHistory,
                    onBackHome = onBackHome,
                )
                return@SteplyScreenColumn
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                uiState.recommendations.forEach { recommendation ->
                    RecommendationCard(
                        recommendation = recommendation,
                        onMarkCompleted = onMarkCompleted,
                    )
                }
            }

            SteplySecondaryButton(
                text = "View History",
                icon = Icons.Default.History,
                onClick = onOpenHistory,
            )
            SteplySecondaryButton(
                text = "Back to Home",
                icon = Icons.Default.Home,
                onClick = onBackHome,
            )
        }
    }
}

@Composable
private fun EmptyRecommendationState(
    onStartChairCheck: () -> Unit,
    onOpenHistory: () -> Unit,
    onBackHome: () -> Unit,
) {
    EmptyStateCard(
        title = "No recommendations yet",
        message = "Complete a movement check to receive gentle exercises.",
        icon = Icons.Default.FitnessCenter,
    )

    SteplyPrimaryButton(
        text = "Start Movement Check",
        icon = Icons.Default.PlayArrow,
        onClick = onStartChairCheck,
    )
    SteplySecondaryButton(
        text = "View History",
        icon = Icons.Default.History,
        onClick = onOpenHistory,
    )
    SteplySecondaryButton(
        text = "Back to Home",
        icon = Icons.Default.Home,
        onClick = onBackHome,
    )
}

@Composable
private fun RecommendationCard(
    recommendation: ExerciseRecommendationUiItem,
    onMarkCompleted: (String) -> Unit,
) {
    ExerciseCard(
        title = recommendation.title,
        description = recommendation.description,
        safetyNote = recommendation.safetyNote,
        durationText = recommendation.durationText,
        isCompleted = recommendation.isCompleted,
        onMarkCompleted = { onMarkCompleted(recommendation.id) },
    )
}
