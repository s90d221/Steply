package com.steply.app.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.EmptyStateCard
import com.steply.app.ui.screens.components.LocalDataNoticeCard
import com.steply.app.ui.screens.components.MetricCard
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.StatusPill
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.text.SteplyCopy

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onChangeProfile: () -> Unit,
    onStartChairCheck: () -> Unit,
    onOpenResult: (String) -> Unit,
) {
    SteplyScaffold(
        title = "Movement History",
        subtitle = "Local 30-second chair stand records.",
        onBack = onBack,
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            if (uiState.isLoading) {
                Text(
                    text = "Loading history.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                return@SteplyScreenColumn
            }

            val profile = uiState.selectedUser
            if (profile == null) {
                EmptyStateCard(
                    title = "Choose a profile",
                    message = uiState.errorMessage ?: "Select a profile to view movement records.",
                    icon = Icons.Default.Person,
                    actionText = "Choose Profile",
                    onAction = onChangeProfile,
                )
                return@SteplyScreenColumn
            }

            SteplyCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = "${profile.displayName}'s movement records",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Only records saved on this device are shown.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            LocalDataNoticeCard(text = "History stays local to this device.")

            if (uiState.results.isEmpty()) {
                EmptyHistoryState(onStartChairCheck = onStartChairCheck)
            } else {
                SummarySection(uiState = uiState)

                SteplyCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = uiState.trendMessage ?: "Nice work today. Keep moving slowly and safely.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.results.forEach { historyItem ->
                        HistoryCard(
                            item = historyItem,
                            onOpenResult = onOpenResult,
                        )
                    }
                }
            }

            SafetyNoticeCard(
                text = SteplyCopy.ReferenceOnlyDisclaimer,
            )
            SteplySecondaryButton(
                text = "Change Profile",
                icon = Icons.Default.Person,
                onClick = onChangeProfile,
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
private fun SummarySection(uiState: HistoryUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard(
            label = "Latest",
            value = uiState.latestCountText,
            helperText = "Most recent check",
            icon = Icons.Default.History,
        )
        MetricCard(
            label = "Best",
            value = uiState.bestCountText,
            helperText = "Highest chair stand count",
            icon = Icons.Default.PlayArrow,
        )
        MetricCard(
            label = "Average",
            value = uiState.averageCountText,
            helperText = "Across saved checks",
            icon = Icons.Default.History,
        )
    }
}

@Composable
private fun EmptyHistoryState(onStartChairCheck: () -> Unit) {
    EmptyStateCard(
        title = "No saved records yet",
        message = "Start a movement check to see progress here.",
        icon = Icons.Default.History,
    )

    SteplyPrimaryButton(
        text = "Start Movement Check",
        icon = Icons.Default.PlayArrow,
        onClick = onStartChairCheck,
    )
}

@Composable
private fun HistoryCard(
    item: HistoryResultUiItem,
    onOpenResult: (String) -> Unit,
) {
    SteplyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.createdAtText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.durationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.repetitionCountText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = item.chairStandUnitLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        StatusPill(recommendationLevel = item.recommendationLevel)

        SteplySecondaryButton(
            text = "View Details",
            icon = Icons.Default.Visibility,
            onClick = { onOpenResult(item.resultId) },
        )
    }
}
