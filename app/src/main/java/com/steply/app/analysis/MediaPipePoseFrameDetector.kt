package com.steply.app.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class MediaPipePoseFrameDetector(
    context: Context,
    private val onFrame: (PoseFrame) -> Unit,
    private val onCameraBitmap: ((Bitmap) -> Unit)? = null,
    private val onError: (String) -> Unit,
) : AutoCloseable {
    private val appContext = context.applicationContext
    private var poseLandmarker: PoseLandmarker? = null

    init {
        setupPoseLandmarker()
    }

    fun detect(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val landmarker = poseLandmarker
        if (landmarker == null) {
            imageProxy.close()
            return
        }

        val frameTime = SystemClock.uptimeMillis()
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888,
        )

        try {
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                if (isFrontCamera) {
                    postScale(-1f, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat())
                }
            }
            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                matrix,
                true,
            )
            onCameraBitmap?.invoke(rotatedBitmap)
            val image = BitmapImageBuilder(rotatedBitmap).build()
            landmarker.detectAsync(image, frameTime)
        } catch (exception: RuntimeException) {
            onError(exception.message ?: "Pose detection failed.")
        } finally {
            imageProxy.close()
        }
    }

    override fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    private fun setupPoseLandmarker() {
        runCatching {
            val baseOptions = BaseOptions.builder()
                .setDelegate(Delegate.CPU)
                .setModelAssetPath(MODEL_ASSET_PATH)
                .build()
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinPoseDetectionConfidence(MIN_POSE_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setMinPosePresenceConfidence(MIN_PRESENCE_CONFIDENCE)
                .setNumPoses(1)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(::handleResult)
                .setErrorListener { error ->
                    onError(error.message ?: "Pose detection failed.")
                }
                .build()
            poseLandmarker = PoseLandmarker.createFromOptions(appContext, options)
        }.onFailure { exception ->
            onError(exception.message ?: "Pose model could not be loaded.")
        }
    }

    private fun handleResult(result: PoseLandmarkerResult, input: MPImage) {
        val landmarks = result.landmarks().firstOrNull()
        if (landmarks.isNullOrEmpty()) {
            onFrame(
                PoseFrame(
                    timestampMs = result.timestampMs(),
                    landmarks = emptyList(),
                    confidence = 0f,
                ),
            )
            return
        }

        val points = landmarks.mapIndexed { index, landmark ->
            PoseLandmarkPoint(
                name = PoseLandmarks.MediaPipeNames.getOrElse(index) { "landmark_$index" },
                x = landmark.x(),
                y = landmark.y(),
                z = landmark.z(),
                visibility = if (landmark.visibility().isPresent) {
                    landmark.visibility().get()
                } else {
                    null
                },
            )
        }
        val confidence = points
            .mapNotNull { it.visibility }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()
            ?: 1f

        onFrame(
            PoseFrame(
                timestampMs = result.timestampMs(),
                landmarks = points,
                confidence = confidence,
            ),
        )
    }

    private companion object {
        const val MODEL_ASSET_PATH = "pose_landmarker_lite.task"
        const val MIN_POSE_CONFIDENCE = 0.5f
        const val MIN_TRACKING_CONFIDENCE = 0.5f
        const val MIN_PRESENCE_CONFIDENCE = 0.5f
    }
}
