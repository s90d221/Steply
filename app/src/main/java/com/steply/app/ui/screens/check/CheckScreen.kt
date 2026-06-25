package com.steply.app.ui.screens.check

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.ChallengeCard
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplyBottomNavigation
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyMainTab
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.text.SteplyCopy

@Composable
fun CheckScreen(
    onStartChallenge: (String) -> Unit,
    onToday: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    SteplyScaffold(
        title = "Check",
        subtitle = "Choose today's movement check.",
        bottomBar = {
            SteplyBottomNavigation(
                currentTab = SteplyMainTab.Check,
                onToday = onToday,
                onCheck = {},
                onHistory = onHistory,
                onSettings = onSettings,
            )
        },
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            SteplyCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentPadding = PaddingValues(20.dp),
            ) {
                Text(
                    text = "Choose or start a movement check.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Pick the check that matches today's space, support, and energy level.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                SteplyPrimaryButton(
                    text = "Start Chair Stand Check",
                    icon = Icons.Default.PlayArrow,
                    onClick = { onStartChallenge(MovementChallengeIds.ChairStand) },
                )
            }

            MovementChallenges.forEach { challenge ->
                ChallengeCard(
                    title = challenge.title,
                    purpose = challenge.purpose,
                    requiredSetup = challenge.requiredSetup,
                    durationText = challenge.durationText,
                    primaryMetric = challenge.primaryMetric,
                    icon = challenge.icon(),
                    accentColor = challenge.accentColor(),
                    accentContainerColor = challenge.accentContainerColor(),
                    onStart = { onStartChallenge(challenge.id) },
                )
            }

            SafetyNoticeCard(
                title = "Before any check",
                text = SteplyCopy.SafetyReminderInline,
            )
        }
    }
}
