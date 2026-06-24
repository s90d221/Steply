package com.steply.app.ui.screens.chaircheck

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.TimerCircle
import com.steply.app.ui.screens.components.WarmNoteSurface
import com.steply.app.ui.text.SteplyCopy
import kotlinx.coroutines.delay

@Composable
fun ChairCheckScreen(
    uiState: ChairCheckUiState,
    onBack: () -> Unit,
    onStartCountdown: () -> Unit,
    onIncrement: () -> Unit,
    onComplete: () -> Unit,
    onRequestStop: () -> Unit,
    onDismissStop: () -> Unit,
    onConfirmStop: () -> Unit,
    onCancelled: () -> Unit,
    onSaved: (String) -> Unit,
    onSavedHandled: () -> Unit,
) {
    LaunchedEffect(uiState.savedResultId) {
        val resultId = uiState.savedResultId
        if (resultId != null) {
            onSaved(resultId)
            onSavedHandled()
        }
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == ChairStandCheckPhase.CANCELLED) {
            onCancelled()
        }
    }

    if (uiState.showStopConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissStop,
            title = {
                Text(
                    text = "Stop this check?",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "This check will end and no result will be saved.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmStop) {
                    Text("Stop Check", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissStop) {
                    Text("Keep Going")
                }
            },
        )
    }

    SteplyScaffold(
        title = "Chair Stand Check",
        subtitle = SteplyCopy.MoveSlowly,
        onBack = onBack,
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            when (uiState.phase) {
                ChairStandCheckPhase.PREPARE -> PrepareContent(onStartCountdown)
                ChairStandCheckPhase.COUNTDOWN -> CountdownContent(uiState)
                ChairStandCheckPhase.ACTIVE -> ActiveContent(
                    uiState = uiState,
                    onIncrement = onIncrement,
                    onComplete = onComplete,
                    onRequestStop = onRequestStop,
                )
                ChairStandCheckPhase.COMPLETED -> SavingContent(uiState)
                ChairStandCheckPhase.CANCELLED -> {
                    Text(
                        text = "The check was stopped.",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }

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
private fun PrepareContent(
    onStartCountdown: () -> Unit,
) {
    SteplyCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
    ) {
        Text(
            text = "Sit comfortably and get ready.",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "When the timer starts, stand up and sit back down slowly. Count one stand only after a full stand and safe return to the chair.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    SafetyNoticeCard()
    SteplyPrimaryButton(
        text = "Get Ready",
        icon = Icons.Default.PlayArrow,
        onClick = onStartCountdown,
    )
}

@Composable
private fun CountdownContent(
    uiState: ChairCheckUiState,
) {
    var pulse by remember(uiState.countdownNumber) { mutableStateOf(false) }
    LaunchedEffect(uiState.countdownNumber) {
        pulse = false
        delay(50L)
        pulse = true
    }
    val scale by animateFloatAsState(
        targetValue = if (pulse) 1.12f else 0.92f,
        animationSpec = tween(durationMillis = 420),
        label = "countdown-pulse",
    )

    SteplyCard(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(34.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Get ready",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (uiState.countdownNumber == 0) {
                    "Start"
                } else {
                    uiState.countdownNumber?.toString().orEmpty()
                },
                modifier = Modifier.scale(scale),
                fontSize = 76.sp,
                lineHeight = 84.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Stand and sit safely when the timer begins.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ActiveContent(
    uiState: ChairCheckUiState,
    onIncrement: () -> Unit,
    onComplete: () -> Unit,
    onRequestStop: () -> Unit,
) {
    SteplyCard {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TimerCircle(
                remainingSeconds = uiState.remainingSeconds,
                totalSeconds = 30,
                label = "Time Remaining",
                diameter = 280.dp,
            )
        }
    }

    RepetitionCard(repetitionCount = uiState.repetitionCount)

    WarmNoteSurface(
        text = "Keep your pace calm. ${SteplyCopy.UseSupport}",
        title = "During the check",
    )

    StandButton(
        enabled = !uiState.isSaving,
        onClick = onIncrement,
    )
    SteplySecondaryButton(
        text = if (uiState.isSaving) "Saving" else "Finish",
        icon = Icons.Default.Check,
        onClick = onComplete,
        enabled = !uiState.isSaving,
    )
    TextButton(
        onClick = onRequestStop,
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = "Stop",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun RepetitionCard(repetitionCount: Int) {
    SteplyCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = repetitionCount.toString(),
                fontSize = 68.sp,
                lineHeight = 74.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "chair stands",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Tap +1 after each complete stand and safe sit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StandButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = "+1 Stand",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
private fun SavingContent(
    uiState: ChairCheckUiState,
) {
    SteplyCard {
        Text(
            text = if (uiState.isSaving) "Saving your result." else "Your result has been saved.",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Steply will take you to the result screen when it is ready.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
