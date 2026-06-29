package com.steply.app.ui.screens.check

import android.annotation.SuppressLint
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.steply.app.ui.screens.components.SteplyCard
import com.steply.app.ui.screens.components.SteplyCorners

@SuppressLint("MissingPermission")
@Composable
fun CameraSetupPreview(
    challenge: MovementChallengeSpec,
    onCameraStatus: (String) -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnCameraStatus by rememberUpdatedState(onCameraStatus)
    val currentOnCameraError by rememberUpdatedState(onCameraError)

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    SteplyCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(SteplyCorners.Card))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(SteplyCorners.Card)),
                )

                CameraSetupOverlay(
                    challenge = challenge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                )
            }

            Text(
                text = "Check your setup before starting",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "Adjust your camera, chair, and starting posture. Begin only when your full body and chair are clearly visible.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            SetupCheckText("Your full body is inside the camera frame.")
            SetupCheckText("The chair is centered and stable.")
            SetupCheckText("The floor is clear before you begin.")
        }
    }

    DisposableEffect(context, lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener(
            {
                runCatching {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .also { preview ->
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val frontSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    val backSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    val useFrontCamera = runCatching {
                        provider.hasCamera(frontSelector)
                    }.getOrDefault(false)

                    val selector = if (useFrontCamera) {
                        frontSelector
                    } else {
                        backSelector
                    }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                    )

                    currentOnCameraStatus(
                        if (useFrontCamera) {
                            "Front camera active. Adjust your position before starting."
                        } else {
                            "Back camera active. Adjust your position before starting."
                        },
                    )
                }.onFailure { exception ->
                    currentOnCameraError(
                        exception.message ?: "Camera could not be started.",
                    )
                }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            cameraProvider?.unbindAll()
        }
    }
}

@Composable
private fun CameraSetupOverlay(
    challenge: MovementChallengeSpec,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = challenge.accentContainerColor().copy(alpha = 0.88f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Fit your body and chair in the frame",
            style = MaterialTheme.typography.labelLarge,
            color = challenge.accentColor(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SetupCheckText(
    text: String,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}