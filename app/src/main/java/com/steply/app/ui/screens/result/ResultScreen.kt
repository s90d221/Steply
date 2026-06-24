package com.steply.app.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.AnimatedMetricValue
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.StatusPill
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.WarmNoteSurface
import com.steply.app.ui.text.SteplyCopy
import com.steply.app.domain.usecase.RecommendationLevelFeedback

@Composable
fun ResultScreen(
    uiState: ResultUiState,
    onBackHome: () -> Unit,
    onOpenRecommendations: (String) -> Unit,
    onOpenHistory: () -> Unit,
) {
    SteplyScaffold(
        title = "Movement Result",
        subtitle = "Your check has been saved locally.",
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            if (uiState.isLoading) {
                Text(text = "Loading your result.", style = MaterialTheme.typography.bodyLarge)
                return@SteplyScreenColumn
            }

            val report = uiState.report
            if (report == null) {
                Text(
                    text = "We could not load this result. Please check your records again.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                SteplySecondaryButton(
                    text = "Back to Home",
                    icon = Icons.Default.Home,
                    onClick = onBackHome,
                )
                return@SteplyScreenColumn
            }

            SteplyCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentPadding = PaddingValues(26.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "30-Second Chair Stand",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Nice work today, ${report.userDisplayName}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    AnimatedMetricValue(targetValue = report.repetitionCount)
                    Text(
                        text = uiState.chairStandUnitLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusPill(recommendationLevel = report.recommendationLevel)
                    Text(
                        text = uiState.completedAtText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            uiState.feedback?.let { FeedbackSection(feedback = it) }
            ConfidenceCard(confidencePercentText = uiState.confidencePercentText)

            WarmNoteSurface(
                title = "Reference only",
                text = "${SteplyCopy.MedicalDisclaimer} This result helps you understand movement and practice safely.",
            )
            SafetyNoticeCard()

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            SteplyPrimaryButton(
                text = "View Recommended Exercises",
                icon = Icons.Default.FitnessCenter,
                onClick = { onOpenRecommendations(report.sessionId) },
            )
            SteplySecondaryButton(
                text = "Back to Home",
                icon = Icons.Default.Home,
                onClick = onBackHome,
            )
            SteplySecondaryButton(
                text = "View History",
                icon = Icons.Default.History,
                onClick = onOpenHistory,
            )
        }
    }
}

@Composable
private fun FeedbackSection(
    feedback: RecommendationLevelFeedback,
) {
    SteplyCard(containerColor = MaterialTheme.colorScheme.surface) {
        Text(
            text = feedback.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = feedback.message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ConfidenceCard(confidencePercentText: String) {
    SteplyCard(containerColor = MaterialTheme.colorScheme.surface) {
        Text(
            text = "Confidence",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = confidencePercentText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "A calm estimate for this saved check.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
