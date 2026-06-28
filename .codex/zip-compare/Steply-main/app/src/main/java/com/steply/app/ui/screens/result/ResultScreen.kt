package com.steply.app.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.ResultSummaryCard
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.WarmNoteSurface
import com.steply.app.ui.text.SteplyCopy
import com.steply.app.domain.usecase.RecommendationLevelFeedback
import java.util.Locale

@Composable
fun ResultScreen(
    uiState: ResultUiState,
    onBackHome: () -> Unit,
    onOpenRecommendations: (String) -> Unit,
    onOpenHistory: () -> Unit,
) {
    SteplyScaffold(
        title = "Result Report",
        subtitle = "Saved locally on this device.",
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

            ResultSummaryCard(
                challengeTitle = "30-Second Chair Stand",
                userMessage = "Nice work today, ${report.userDisplayName}.",
                primaryValue = report.repetitionCount,
                primaryUnitLabel = uiState.chairStandUnitLabel,
                recommendationLevel = report.recommendationLevel,
                confidenceText = uiState.confidencePercentText,
                completedAtText = uiState.completedAtText,
            )

            SteplyPrimaryButton(
                text = "Recommended Exercises",
                icon = Icons.Default.FitnessCenter,
                onClick = { onOpenRecommendations(report.sessionId) },
            )
            SteplySecondaryButton(
                text = "View History",
                icon = Icons.Default.History,
                onClick = onOpenHistory,
            )
            SteplySecondaryButton(
                text = "Back to Today",
                icon = Icons.Default.Home,
                onClick = onBackHome,
            )

            EvidenceCard(
                repetitionCount = report.repetitionCount,
                chairStandUnitLabel = uiState.chairStandUnitLabel,
                confidencePercentText = uiState.confidencePercentText,
                averageRepSeconds = report.averageRepSeconds,
                guidanceText = report.guidanceText,
            )

            uiState.feedback?.let { NextActionCard(feedback = it) }

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
        }
    }
}

@Composable
private fun NextActionCard(
    feedback: RecommendationLevelFeedback,
) {
    SteplyCard(containerColor = MaterialTheme.colorScheme.surface) {
        Text(
            text = "Recommended next action",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = feedback.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = feedback.message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EvidenceCard(
    repetitionCount: Int,
    chairStandUnitLabel: String,
    confidencePercentText: String,
    averageRepSeconds: Float?,
    guidanceText: String,
) {
    SteplyCard(containerColor = MaterialTheme.colorScheme.surface) {
        Text(
            text = "Why this result was shown",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        EvidenceRow(label = "Primary score", value = "$repetitionCount $chairStandUnitLabel")
        EvidenceRow(label = "Confidence", value = confidencePercentText)
        EvidenceRow(
            label = "Rep timing",
            value = averageRepSeconds?.let { "${it.formatOneDecimal()} sec average" }
                ?: "Manual count without camera timing",
        )
        EvidenceRow(label = "Guidance", value = guidanceText)
    }
}

@Composable
private fun EvidenceRow(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun Float.formatOneDecimal(): String {
    return String.format(Locale.US, "%.1f", this)
}
