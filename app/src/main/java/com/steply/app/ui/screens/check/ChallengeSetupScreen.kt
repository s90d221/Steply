package com.steply.app.ui.screens.check

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.WarmNoteSurface

@Composable
fun ChallengeSetupScreen(
    challenge: MovementChallengeSpec,
    onBack: () -> Unit,
    onBeginChairStand: () -> Unit,
    onChooseDifferentChallenge: () -> Unit,
) {
    SteplyScaffold(
        title = "Setup",
        subtitle = challenge.title,
        onBack = onBack,
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            CameraSetupPreview(
                challenge = challenge,
                onCameraStatus = { },
                onCameraError = { },
            )

            WarmNoteSurface(
                title = "Ready check",
                text = "${challenge.requiredSetup}. Clear the floor, keep support nearby, and stop if uncomfortable.",
                icon = Icons.Default.Warning,
            )

            WarmNoteSurface(
                title = "Official rule",
                text = challenge.ruleSummary,
                icon = Icons.Default.FitnessCenter,
            )

            if (challenge.isMeasurementReady) {
                SteplyPrimaryButton(
                    text = "Begin Camera Check",
                    icon = Icons.Default.PlayArrow,
                    onClick = onBeginChairStand,
                )
            } else {
                WarmNoteSurface(
                    title = "Saved camera scoring not active yet",
                    text = "This assessment needs a dedicated result flow. Use Chair Stand for saved MediaPipe scoring today.",
                    icon = Icons.Default.Warning,
                )
                SteplyPrimaryButton(
                    text = "Use Chair Stand Check",
                    icon = Icons.Default.FitnessCenter,
                    onClick = onBeginChairStand,
                )
            }

            SteplySecondaryButton(
                text = "Choose Different Challenge",
                icon = Icons.Default.SwapHoriz,
                onClick = onChooseDifferentChallenge,
            )
        }
    }
}

//삭제 가능성
@Composable
private fun CameraSetupPlaceholder(
    challenge: MovementChallengeSpec,
) {
    SteplyCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                )
                .padding(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(challenge.accentContainerColor(), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = challenge.accentColor(),
                        modifier = Modifier.size(32.dp),
                    )
                }
                Text(
                    text = "Body in view",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "The live camera opens on the check screen. Stand where your full body fits on screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
