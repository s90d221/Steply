package com.steply.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.ActionCard
import com.steply.app.ui.screens.components.EmptyStateCard
import com.steply.app.ui.screens.components.ProfileAvatar
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.StatusPill
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.WarmNoteSurface
import com.steply.app.ui.text.SteplyCopy

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartChairCheck: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenRecommendations: () -> Unit,
    onChangeProfile: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    SteplyScaffold(
        title = "Steply",
        subtitle = "Gentle movement coaching",
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            val profile = uiState.selectedProfile

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
                contentPadding = PaddingValues(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileAvatar(displayName = profile.displayName)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "${profile.displayName}, ready to move safely today?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${SteplyCopy.UseSupport} ${SteplyCopy.MoveSlowly}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            ActionCard(
                title = "Start Movement Check",
                subtitle = "30-second chair stand",
                icon = Icons.Default.PlayArrow,
                onClick = onStartChairCheck,
                primary = true,
            )

            LatestResultCard(latestResult = uiState.latestResult)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Quick actions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title = "History",
                        subtitle = "Past checks",
                        icon = Icons.Default.History,
                        onClick = onOpenHistory,
                        modifier = Modifier.weight(1f),
                    )
                    ActionCard(
                        title = "Exercises",
                        subtitle = "Gentle practice",
                        icon = Icons.Default.FitnessCenter,
                        onClick = onOpenRecommendations,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title = "Change Profile",
                        subtitle = "Switch user",
                        icon = Icons.Default.Person,
                        onClick = onChangeProfile,
                        modifier = Modifier.weight(1f),
                    )
                    ActionCard(
                        title = "Settings",
                        subtitle = "Local data",
                        icon = Icons.Default.Settings,
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            SafetyNoticeCard(
                title = "Safety reminder",
                text = SteplyCopy.MedicalDisclaimerWithStop,
            )
        }
    }
}

@Composable
private fun LatestResultCard(
    latestResult: LatestChairStandResultSummary?,
) {
    if (latestResult == null) {
        WarmNoteSurface(
            text = "Start your first movement check to see your latest result here.",
            title = "No checks yet",
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
                    text = "Latest result",
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
