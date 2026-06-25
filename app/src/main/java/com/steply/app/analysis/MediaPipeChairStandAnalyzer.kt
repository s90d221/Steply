package com.steply.app.analysis

import com.steply.app.domain.usecase.ChairStandRecommendationLevelCalculator
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class MediaPipeChairStandAnalyzer(
    private val durationSeconds: Int = SteadiAssessmentRules.ChairStandDurationSeconds,
) : ChairStandAnalyzer {
    private var userId: String? = null
    private var startedAt: Long? = null
    private var latestTimestampMs: Long? = null
    private var latestFeatures: ChairStandPoseFeatures? = null
    private var latestState = ChairStandAnalysisState(
        repetitionCount = 0,
        elapsedSeconds = 0,
        confidence = 0f,
        isFullBodyVisible = false,
        warningMessage = "Move your full body into the camera view.",
        postureMessage = "Camera analysis will count each full stand.",
    )

    private val countedAtMs = mutableListOf<Long>()
    private val confidenceSamples = mutableListOf<Float>()
    private val trunkLeanSamples = mutableListOf<Float>()
    private val symmetrySamples = mutableListOf<Float>()
    private val stabilitySamples = mutableListOf<Float>()
    private val recentBodyCenters = ArrayDeque<BodyCenter>()

    private var repetitionCount = 0
    private var readyForNextStand = true
    private var standingStreak = 0
    private var seatedStreak = 0
    private var armSupportFrames = 0
    private var armUseDisqualified = false

    override fun startSession(userId: String, startedAt: Long) {
        reset()
        this.userId = userId
        this.startedAt = startedAt
        latestTimestampMs = startedAt
    }

    override fun addFrame(frame: PoseFrame) {
        if (startedAt == null) return
        latestTimestampMs = frame.timestampMs

        val features = frame.toChairStandFeatures()
        latestFeatures = features
        if (features == null) {
            latestState = stateForMissingPose(frame.timestampMs)
            return
        }

        confidenceSamples.add(features.confidence)
        trunkLeanSamples.add(features.trunkLeanScore)
        symmetrySamples.add(features.symmetryScore)
        stabilitySamples.add(features.stabilityScore)
        rememberBodyCenter(features.bodyCenter)

        updateArmRule(features)
        updateRepetitionCount(frame.timestampMs, features)
        latestState = features.toState(
            timestampMs = frame.timestampMs,
            repetitionCount = repetitionCount,
            elapsedSeconds = elapsedSeconds(frame.timestampMs),
            armUseDisqualified = armUseDisqualified,
        )
    }

    override fun addManualRepetition() {
        if (startedAt == null || armUseDisqualified) return
        repetitionCount += 1
        countedAtMs.add(System.currentTimeMillis())
        latestState = latestState.copy(repetitionCount = repetitionCount)
    }

    override fun getCurrentState(): ChairStandAnalysisState {
        return latestState.copy(elapsedSeconds = elapsedSeconds(System.currentTimeMillis()))
    }

    override fun finishSession(completedAt: Long): ChairStandAnalysisResult {
        val finalRepetitionCount = if (armUseDisqualified) {
            0
        } else {
            repetitionCount + finalHalfStandCredit()
        }
        val repIntervalsSeconds = countedAtMs
            .zipWithNext { first, second -> (second - first) / 1_000f }
            .filter { it > 0f }
        return ChairStandAnalysisResult(
            repetitionCount = finalRepetitionCount,
            averageRepSeconds = if (finalRepetitionCount > 0) {
                durationSeconds.toFloat() / finalRepetitionCount
            } else {
                null
            },
            fastestRepSeconds = repIntervalsSeconds.minOrNull(),
            slowestRepSeconds = repIntervalsSeconds.maxOrNull(),
            trunkLeanScore = trunkLeanSamples.averageOrNull(),
            symmetryScore = symmetrySamples.averageOrNull(),
            stabilityScore = stabilitySamples.averageOrNull(),
            confidence = confidenceSamples.averageOrNull() ?: latestState.confidence,
            recommendationLevel = ChairStandRecommendationLevelCalculator.calculate(finalRepetitionCount),
            armUseDisqualified = armUseDisqualified,
        )
    }

    override fun reset() {
        userId = null
        startedAt = null
        latestTimestampMs = null
        latestFeatures = null
        latestState = ChairStandAnalysisState(
            repetitionCount = 0,
            elapsedSeconds = 0,
            confidence = 0f,
            isFullBodyVisible = false,
            warningMessage = "Move your full body into the camera view.",
            postureMessage = "Camera analysis will count each full stand.",
        )
        countedAtMs.clear()
        confidenceSamples.clear()
        trunkLeanSamples.clear()
        symmetrySamples.clear()
        stabilitySamples.clear()
        recentBodyCenters.clear()
        repetitionCount = 0
        readyForNextStand = true
        standingStreak = 0
        seatedStreak = 0
        armSupportFrames = 0
        armUseDisqualified = false
    }

    private fun updateRepetitionCount(timestampMs: Long, features: ChairStandPoseFeatures) {
        standingStreak = if (features.phase == ChairStandPosePhase.Standing) standingStreak + 1 else 0
        seatedStreak = if (features.phase == ChairStandPosePhase.Seated) seatedStreak + 1 else 0

        if (seatedStreak >= REQUIRED_STABLE_FRAMES) {
            readyForNextStand = true
        }

        if (
            readyForNextStand &&
            standingStreak >= REQUIRED_STABLE_FRAMES &&
            features.fullBodyVisible &&
            !armUseDisqualified
        ) {
            repetitionCount += 1
            countedAtMs.add(timestampMs)
            readyForNextStand = false
        }
    }

    private fun updateArmRule(features: ChairStandPoseFeatures) {
        val possibleArmSupport = features.phase == ChairStandPosePhase.Rising && features.armSupportLikely
        armSupportFrames = if (possibleArmSupport) {
            armSupportFrames + 1
        } else {
            (armSupportFrames - 1).coerceAtLeast(0)
        }
        if (armSupportFrames >= ARM_SUPPORT_DISQUALIFY_FRAMES) {
            armUseDisqualified = true
        }
    }

    private fun finalHalfStandCredit(): Int {
        val features = latestFeatures ?: return 0
        return if (readyForNextStand && features.halfwayToStanding) 1 else 0
    }

    private fun PoseFrame.toChairStandFeatures(): ChairStandPoseFeatures? {
        val leftShoulder = visiblePoint(PoseLandmarks.LeftShoulder)
        val rightShoulder = visiblePoint(PoseLandmarks.RightShoulder)
        val leftHip = visiblePoint(PoseLandmarks.LeftHip)
        val rightHip = visiblePoint(PoseLandmarks.RightHip)
        val leftKnee = visiblePoint(PoseLandmarks.LeftKnee)
        val rightKnee = visiblePoint(PoseLandmarks.RightKnee)
        val leftAnkle = visiblePoint(PoseLandmarks.LeftAnkle)
        val rightAnkle = visiblePoint(PoseLandmarks.RightAnkle)

        if (
            leftShoulder == null || rightShoulder == null ||
            leftHip == null || rightHip == null ||
            leftKnee == null || rightKnee == null ||
            leftAnkle == null || rightAnkle == null
        ) {
            return null
        }

        val shoulderCenter = midpoint(leftShoulder, rightShoulder)
        val hipCenter = midpoint(leftHip, rightHip)
        val kneeCenter = midpoint(leftKnee, rightKnee)
        val bodyCenter = BodyCenter(
            x = (shoulderCenter.x + hipCenter.x) / 2f,
            y = (shoulderCenter.y + hipCenter.y) / 2f,
        )

        val leftKneeAngle = angleDegrees(leftHip, leftKnee, leftAnkle)
        val rightKneeAngle = angleDegrees(rightHip, rightKnee, rightAnkle)
        val averageKneeAngle = (leftKneeAngle + rightKneeAngle) / 2f
        val hipAboveKnees = kneeCenter.y - hipCenter.y
        val fullBodyVisible = requiredLandmarks.all { visiblePoint(it) != null }
        val shoulderWidth = distance(leftShoulder, rightShoulder).coerceAtLeast(0.08f)

        val phase = when {
            averageKneeAngle >= STANDING_KNEE_ANGLE && hipAboveKnees >= STANDING_HIP_MARGIN ->
                ChairStandPosePhase.Standing
            averageKneeAngle <= SEATED_KNEE_ANGLE || hipAboveKnees < SEATED_HIP_MARGIN ->
                ChairStandPosePhase.Seated
            hipAboveKnees >= RISING_HIP_MARGIN ->
                ChairStandPosePhase.Rising
            else ->
                ChairStandPosePhase.Unknown
        }

        val trunkLeanScore = (1f - abs(shoulderCenter.x - hipCenter.x) / (shoulderWidth * 0.75f))
            .coerceIn(0f, 1f)
        val symmetryScore = (1f - abs(leftKneeAngle - rightKneeAngle) / 55f).coerceIn(0f, 1f)
        val stabilityScore = stabilityScoreWith(bodyCenter)
        val confidence = requiredLandmarks
            .mapNotNull { landmark(it)?.visibility ?: this.confidence }
            .average()
            .toFloat()
            .coerceIn(0f, 1f)

        return ChairStandPoseFeatures(
            phase = phase,
            fullBodyVisible = fullBodyVisible,
            confidence = confidence,
            trunkLeanScore = trunkLeanScore,
            symmetryScore = symmetryScore,
            stabilityScore = stabilityScore,
            armSupportLikely = armSupportLikely(hipCenter),
            armsCrossedLikely = armsCrossedLikely(shoulderWidth),
            halfwayToStanding = averageKneeAngle >= HALFWAY_KNEE_ANGLE &&
                hipAboveKnees >= HALFWAY_HIP_MARGIN,
            bodyCenter = bodyCenter,
        )
    }

    private fun PoseFrame.visiblePoint(name: String): PosePoint? {
        val landmark = landmark(name) ?: return null
        val visibility = landmark.visibility ?: confidence
        return if (visibility >= MIN_LANDMARK_VISIBILITY) {
            PosePoint(x = landmark.x, y = landmark.y)
        } else {
            null
        }
    }

    private fun PoseFrame.landmark(name: String): PoseLandmarkPoint? {
        return landmarks.firstOrNull { it.name == name }
    }

    private fun PoseFrame.armSupportLikely(hipCenter: PosePoint): Boolean {
        val leftWrist = visiblePoint(PoseLandmarks.LeftWrist)
        val rightWrist = visiblePoint(PoseLandmarks.RightWrist)
        if (leftWrist == null || rightWrist == null) return false
        return leftWrist.y > hipCenter.y + ARM_SUPPORT_Y_MARGIN &&
            rightWrist.y > hipCenter.y + ARM_SUPPORT_Y_MARGIN
    }

    private fun PoseFrame.armsCrossedLikely(shoulderWidth: Float): Boolean? {
        val leftWrist = visiblePoint(PoseLandmarks.LeftWrist)
        val rightWrist = visiblePoint(PoseLandmarks.RightWrist)
        val leftShoulder = visiblePoint(PoseLandmarks.LeftShoulder)
        val rightShoulder = visiblePoint(PoseLandmarks.RightShoulder)
        val leftHip = visiblePoint(PoseLandmarks.LeftHip)
        val rightHip = visiblePoint(PoseLandmarks.RightHip)
        if (
            leftWrist == null || rightWrist == null ||
            leftShoulder == null || rightShoulder == null ||
            leftHip == null || rightHip == null
        ) {
            return null
        }
        val hipY = (leftHip.y + rightHip.y) / 2f
        val shoulderY = (leftShoulder.y + rightShoulder.y) / 2f
        val wristsInChestBand = leftWrist.y in shoulderY..hipY && rightWrist.y in shoulderY..hipY
        val leftNearRightShoulder = distance(leftWrist, rightShoulder) <= shoulderWidth
        val rightNearLeftShoulder = distance(rightWrist, leftShoulder) <= shoulderWidth
        return wristsInChestBand && leftNearRightShoulder && rightNearLeftShoulder
    }

    private fun ChairStandPoseFeatures.toState(
        timestampMs: Long,
        repetitionCount: Int,
        elapsedSeconds: Int,
        armUseDisqualified: Boolean,
    ): ChairStandAnalysisState {
        val warning = when {
            armUseDisqualified -> "Arm use detected. Official Chair Stand score is 0."
            !fullBodyVisible -> "Move back until shoulders, hips, knees, and feet are visible."
            phase == ChairStandPosePhase.Rising && armsCrossedLikely == false ->
                "Keep arms crossed against your chest while standing."
            phase == ChairStandPosePhase.Standing && trunkLeanScore < TRUNK_WARNING_SCORE ->
                "Keep your chest centered over your hips."
            phase == ChairStandPosePhase.Standing && stabilityScore < STABILITY_WARNING_SCORE ->
                "Movement looks unsteady. Slow down and keep support nearby."
            else -> null
        }
        val posture = when {
            armUseDisqualified -> SteadiAssessmentRules.ChairStandArmRule
            phase == ChairStandPosePhase.Standing -> "Full stand detected. Sit safely to reset the next count."
            phase == ChairStandPosePhase.Rising -> "Rising detected. Stand fully before sitting."
            phase == ChairStandPosePhase.Seated -> "Seated position detected. Stand when ready."
            else -> "Camera is tracking your movement."
        }
        return ChairStandAnalysisState(
            repetitionCount = repetitionCount,
            elapsedSeconds = elapsedSeconds,
            confidence = confidence,
            isFullBodyVisible = fullBodyVisible,
            warningMessage = warning,
            postureMessage = posture,
            isArmUseSuspected = armUseDisqualified || armSupportLikely,
            isStandingOrRising = phase == ChairStandPosePhase.Standing || phase == ChairStandPosePhase.Rising,
        )
    }

    private fun stateForMissingPose(timestampMs: Long): ChairStandAnalysisState {
        return ChairStandAnalysisState(
            repetitionCount = repetitionCount,
            elapsedSeconds = elapsedSeconds(timestampMs),
            confidence = 0f,
            isFullBodyVisible = false,
            warningMessage = "Move into view so the camera can see your whole body.",
            postureMessage = "No full-body pose detected yet.",
            isArmUseSuspected = armUseDisqualified,
            isStandingOrRising = false,
        )
    }

    private fun elapsedSeconds(nowMs: Long): Int {
        val start = startedAt ?: nowMs
        return ((nowMs - start).coerceAtLeast(0L) / 1_000L)
            .toInt()
            .coerceAtMost(durationSeconds)
    }

    private fun rememberBodyCenter(center: BodyCenter) {
        recentBodyCenters.addLast(center)
        while (recentBodyCenters.size > STABILITY_SAMPLE_LIMIT) {
            recentBodyCenters.removeFirst()
        }
    }

    private fun stabilityScoreWith(center: BodyCenter): Float {
        val samples = recentBodyCenters + center
        if (samples.size < MIN_STABILITY_SAMPLES) return 1f
        val meanX = samples.map { it.x }.average().toFloat()
        val meanY = samples.map { it.y }.average().toFloat()
        val variance = samples.map { (it.x - meanX).pow(2) + (it.y - meanY).pow(2) }.average().toFloat()
        val sway = sqrt(variance)
        return (1f - sway * 18f).coerceIn(0f, 1f)
    }

    private fun List<Float>.averageOrNull(): Float? {
        return if (isEmpty()) null else average().toFloat().coerceIn(0f, 1f)
    }

    private fun midpoint(first: PosePoint, second: PosePoint): PosePoint {
        return PosePoint(
            x = (first.x + second.x) / 2f,
            y = (first.y + second.y) / 2f,
        )
    }

    private fun distance(first: PosePoint, second: PosePoint): Float {
        return sqrt((first.x - second.x).pow(2) + (first.y - second.y).pow(2))
    }

    private fun angleDegrees(first: PosePoint, center: PosePoint, third: PosePoint): Float {
        val firstVectorX = first.x - center.x
        val firstVectorY = first.y - center.y
        val secondVectorX = third.x - center.x
        val secondVectorY = third.y - center.y
        val dot = firstVectorX * secondVectorX + firstVectorY * secondVectorY
        val magnitude = max(
            sqrt(firstVectorX.pow(2) + firstVectorY.pow(2)) *
                sqrt(secondVectorX.pow(2) + secondVectorY.pow(2)),
            MIN_VECTOR_MAGNITUDE,
        )
        return (acos((dot / magnitude).coerceIn(-1f, 1f)) * 180f / PI.toFloat())
    }

    private companion object {
        const val MIN_LANDMARK_VISIBILITY = 0.45f
        const val REQUIRED_STABLE_FRAMES = 2
        const val ARM_SUPPORT_DISQUALIFY_FRAMES = 3
        const val ARM_SUPPORT_Y_MARGIN = 0.05f
        const val STANDING_KNEE_ANGLE = 150f
        const val SEATED_KNEE_ANGLE = 128f
        const val HALFWAY_KNEE_ANGLE = 138f
        const val STANDING_HIP_MARGIN = 0.08f
        const val RISING_HIP_MARGIN = 0.04f
        const val SEATED_HIP_MARGIN = 0.03f
        const val HALFWAY_HIP_MARGIN = 0.03f
        const val TRUNK_WARNING_SCORE = 0.55f
        const val STABILITY_WARNING_SCORE = 0.45f
        const val STABILITY_SAMPLE_LIMIT = 20
        const val MIN_STABILITY_SAMPLES = 4
        const val MIN_VECTOR_MAGNITUDE = 0.0001f

        val requiredLandmarks = listOf(
            PoseLandmarks.LeftShoulder,
            PoseLandmarks.RightShoulder,
            PoseLandmarks.LeftHip,
            PoseLandmarks.RightHip,
            PoseLandmarks.LeftKnee,
            PoseLandmarks.RightKnee,
            PoseLandmarks.LeftAnkle,
            PoseLandmarks.RightAnkle,
        )
    }
}

private enum class ChairStandPosePhase {
    Unknown,
    Seated,
    Rising,
    Standing,
}

private data class ChairStandPoseFeatures(
    val phase: ChairStandPosePhase,
    val fullBodyVisible: Boolean,
    val confidence: Float,
    val trunkLeanScore: Float,
    val symmetryScore: Float,
    val stabilityScore: Float,
    val armSupportLikely: Boolean,
    val armsCrossedLikely: Boolean?,
    val halfwayToStanding: Boolean,
    val bodyCenter: BodyCenter,
)

private data class PosePoint(
    val x: Float,
    val y: Float,
)

private data class BodyCenter(
    val x: Float,
    val y: Float,
)
