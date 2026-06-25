package com.steply.app.ui.screens.chaircheck

import android.annotation.SuppressLint
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.steply.app.analysis.MediaPipePoseFrameDetector
import com.steply.app.analysis.PoseFrame
import com.steply.app.ui.screens.components.SteplyCorners
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
@Composable
fun PoseCameraPreview(
    onPoseFrame: (PoseFrame) -> Unit,
    onCameraStatus: (String) -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnPoseFrame by rememberUpdatedState(onPoseFrame)
    val currentOnCameraStatus by rememberUpdatedState(onCameraStatus)
    val currentOnCameraError by rememberUpdatedState(onCameraError)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(SteplyCorners.Card))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
            .padding(1.dp),
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(SteplyCorners.Card)),
        )
    }

    DisposableEffect(context, lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null
        var poseDetector: MediaPipePoseFrameDetector? = null

        cameraExecutor.execute {
            poseDetector = MediaPipePoseFrameDetector(
                context = context,
                onFrame = { frame -> currentOnPoseFrame(frame) },
                onError = { message -> currentOnCameraError(message) },
            )
        }

        cameraProviderFuture.addListener(
            {
                runCatching {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()

                    val frontSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    val backSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val useFrontCamera = runCatching { provider.hasCamera(frontSelector) }
                        .getOrDefault(false)
                    val selector = if (useFrontCamera) frontSelector else backSelector

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val detector = poseDetector
                        if (detector == null) {
                            imageProxy.close()
                        } else {
                            detector.detect(imageProxy = imageProxy, isFrontCamera = useFrontCamera)
                        }
                    }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis,
                    )
                    currentOnCameraStatus(
                        if (useFrontCamera) {
                            "Front camera active. Keep your whole body in view."
                        } else {
                            "Back camera active. Keep your whole body in view."
                        },
                    )
                }.onFailure { exception ->
                    currentOnCameraError(exception.message ?: "Camera could not be started.")
                }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            cameraProvider?.unbindAll()
            cameraExecutor.execute {
                poseDetector?.close()
                poseDetector = null
            }
            cameraExecutor.shutdown()
        }
    }
}
