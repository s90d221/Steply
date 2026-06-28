package com.steply.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.EmptyStateCard
import com.steply.app.ui.screens.components.ActionCard
import com.steply.app.ui.screens.components.ProfileAvatar
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.StatusPill
import com.steply.app.ui.screens.components.StatusChip
import com.steply.app.ui.screens.components.SteplyBottomNavigation
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyMainTab
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.SteplySpacing
import com.steply.app.ui.text.SteplyCopy

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartChairCheck: () -> Unit,
    onOpenCheck: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenRecommendations: () -> Unit,
    onChangeProfile: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val profile = uiState.selectedProfile

    SteplyScaffold(
        title = "Today",
        subtitle = "Guided movement care",
        bottomBar = {
            if (profile != null) {
                SteplyBottomNavigation(
                    currentTab = SteplyMainTab.Today,
                    onToday = {},
                    onCheck = onOpenCheck,
                    onHistory = onOpenHistory,
                    onSettings = onOpenSettings,
                )
            }
        },
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            if (profile == null) {
                EmptyStateCard(
                    title = "Choose a profile",
                    message = "Select who will use Steply before starting a movement check.",
                    icon = Icons.Default.Person,
                    actionText = "Choose Profile",
                    onAction = onChangeProfile,
                )
                return@SteplyScreenColumn
            }

            SteplyCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentPadding = PaddingValues(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileAvatar(displayName = profile.displayName)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "${profile.displayName}, ready to move safely today?",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            TodayMovementCheckCard(
                onStartChairCheck = onStartChairCheck,
            )

            ActionCard(
                title = "Choose a Different Challenge",
                subtitle = "Balance and walking checks",
                icon = Icons.Default.SwapHoriz,
                onClick = onOpenCheck,
            )

            LatestResultCard(latestResult = uiState.latestResult)

            RecommendedExercisePreviewCard(
                latestRecommendation = uiState.latestRecommendation,
                onOpenRecommendations = onOpenRecommendations,
                onStartChairCheck = onStartChairCheck,
            )

            SafetyNoticeCard(
                title = "Safety reminder",
                text = SteplyCopy.StopIfUncomfortable,
            )
        }
    }
}

@Composable
private fun TodayMovementCheckCard(
    onStartChairCheck: () -> Unit,
) {
    SteplyCard(
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SteplySpacing.SmallGap),
        ) {
            Text(
                text = "Today's Movement Check",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "30-Second Chair Stand",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            StatusChip(
                text = "Suggested",
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Lower-body strength check with a stable chair.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SteplyPrimaryButton(
            text = "Start Check",
            icon = Icons.Default.PlayArrow,
            onClick = onStartChairCheck,
        )
    }
}

@Composable
private fun LatestResultCard(
    latestResult: LatestChairStandResultSummary?,
) {
    if (latestResult == null) {
        EmptyStateCard(
            title = "No checks yet",
            message = "Start your first movement check to see your latest result here.",
            icon = Icons.Default.History,
        )
        return
    }

    SteplyCard(
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Latest movement check result",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = latestResult.createdAtText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusPill(recommendationLevel = latestResult.recommendationLevel)
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = latestResult.repetitionCountText,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = latestResult.chairStandUnitLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RecommendedExercisePreviewCard(
    latestRecommendation: LatestRecommendationSummary?,
    onOpenRecommendations: () -> Unit,
    onStartChairCheck: () -> Unit,
) {
    if (latestRecommendation == null) {
        EmptyStateCard(
            title = "Recommended exercise",
            message = "Complete a movement check to receive a gentle exercise suggestion.",
            icon = Icons.Default.FitnessCenter,
            actionText = "Start Check",
            onAction = onStartChairCheck,
        )
        return
    }

    SteplyCard(
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Recommended exercise",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = latestRecommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = latestRecommendation.safetyNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(
                text = if (latestRecommendation.isCompleted) "Completed" else latestRecommendation.durationText,
                color = if (latestRecommendation.isCompleted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = MaterialTheme.colorScheme.primary,
            )
        }
        SteplySecondaryButton(
            text = "View Recommended Exercises",
            icon = if (latestRecommendation.isCompleted) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
            onClick = onOpenRecommendations,
        )
    }
}
