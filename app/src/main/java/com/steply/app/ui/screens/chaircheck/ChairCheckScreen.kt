package com.steply.app.ui.screens.chaircheck

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.steply.app.analysis.PoseFrame
import com.steply.app.analysis.SteadiAssessmentRules
import com.steply.app.ui.screens.components.SafetyNoticeCard
import com.steply.app.ui.screens.components.SteplyDestructiveButton
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyPrimaryButton
import com.steply.app.ui.screens.components.SteplyScaffold
import com.steply.app.ui.screens.components.SteplyScreenColumn
import com.steply.app.ui.screens.components.SteplySecondaryButton
import com.steply.app.ui.screens.components.TimerCircle
import com.steply.app.ui.text.SteplyCopy
import kotlinx.coroutines.delay

@Composable
fun ChairCheckScreen(
    uiState: ChairCheckUiState,
    onBack: () -> Unit,
    onStartCountdown: () -> Unit,
    onPoseFrame: (PoseFrame) -> Unit,
    onCameraStatus: (String) -> Unit,
    onCameraError: (String) -> Unit,
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
        title = "30-Second Chair Stand",
        subtitle = "Count each safe stand and sit.",
        onBack = onBack,
    ) { paddingValues ->
        SteplyScreenColumn(paddingValues = paddingValues) {
            when (uiState.phase) {
                ChairStandCheckPhase.PREPARE -> PrepareContent(onStartCountdown)
                ChairStandCheckPhase.COUNTDOWN -> CountdownContent(uiState)
                ChairStandCheckPhase.ACTIVE -> ActiveContent(
                    uiState = uiState,
                    onPoseFrame = onPoseFrame,
                    onCameraStatus = onCameraStatus,
                    onCameraError = onCameraError,
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
    onPoseFrame: (PoseFrame) -> Unit,
    onCameraStatus: (String) -> Unit,
    onCameraError: (String) -> Unit,
    onComplete: () -> Unit,
    onRequestStop: () -> Unit,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            onCameraStatus("Camera permission granted. Starting camera analysis.")
        } else {
            onCameraError("Camera permission is needed for automatic counting.")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraAnalysisCard(
            uiState = uiState,
            onPoseFrame = onPoseFrame,
            onCameraStatus = onCameraStatus,
            onCameraError = onCameraError,
        )
    } else {
        CameraPermissionCard(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
        )
    }

    ActiveGuidanceCard(uiState)

    ActiveMetricCard(
        remainingSeconds = uiState.remainingSeconds,
        repetitionCount = uiState.repetitionCount,
        poseConfidence = uiState.poseConfidence,
        isFullBodyVisible = uiState.isFullBodyVisible,
        movementWarningMessage = uiState.movementWarningMessage,
    )

    SteplySecondaryButton(
        text = if (uiState.isSaving) "Saving" else "Finish and Save",
        icon = Icons.Default.Check,
        onClick = onComplete,
        enabled = !uiState.isSaving,
    )
    SteplyDestructiveButton(
        text = "Stop Check",
        icon = Icons.Default.Close,
        onClick = onRequestStop,
        enabled = !uiState.isSaving,
    )
}

@Composable
private fun CameraAnalysisCard(
    uiState: ChairCheckUiState,
    onPoseFrame: (PoseFrame) -> Unit,
    onCameraStatus: (String) -> Unit,
    onCameraError: (String) -> Unit,
) {
    SteplyCard(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
    ) {
        PoseCameraPreview(
            onPoseFrame = onPoseFrame,
            onCameraStatus = onCameraStatus,
            onCameraError = onCameraError,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = uiState.cameraStatusMessage,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CameraPermissionCard(
    onRequestPermission: () -> Unit,
) {
    SteplyCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
    ) {
        Text(
            text = "Camera access is needed for automatic counting.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Steply uses on-device pose landmarks only. No video is saved or uploaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        SteplyPrimaryButton(
            text = "Allow Camera",
            icon = Icons.Default.CameraAlt,
            onClick = onRequestPermission,
        )
    }
}

@Composable
private fun ActiveGuidanceCard(
    uiState: ChairCheckUiState,
) {
    SteplyCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
    ) {
        Text(
            text = "Camera is counting your stands.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
            }
            Text(
                text = uiState.poseFeedbackMessage,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = "${SteadiAssessmentRules.ChairStandRuleSummary} ${SteadiAssessmentRules.ChairStandArmRule}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActiveMetricCard(
    remainingSeconds: Int,
    repetitionCount: Int,
    poseConfidence: Float,
    isFullBodyVisible: Boolean,
    movementWarningMessage: String?,
) {
    SteplyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimerCircle(
                remainingSeconds = remainingSeconds,
                totalSeconds = 30,
                label = "Time",
                diameter = 112.dp,
                strokeWidth = 12.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = repetitionCount.toString(),
                    fontSize = 48.sp,
                    lineHeight = 52.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "camera-counted stands",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isFullBodyVisible) {
                        "Full body visible. Confidence ${(poseConfidence * 100).toInt().coerceIn(0, 100)}%."
                    } else {
                        "Move back until your full body is visible."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        movementWarningMessage?.let { warning ->
            Text(
                text = warning,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
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
