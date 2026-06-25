package com.steply.app.analysis

interface ChairStandAnalyzer {
    fun startSession(userId: String, startedAt: Long)
    fun addFrame(frame: PoseFrame)
    fun addManualRepetition()
    fun getCurrentState(): ChairStandAnalysisState
    fun finishSession(completedAt: Long): ChairStandAnalysisResult
    fun reset()
}

data class PoseFrame(
    val timestampMs: Long,
    val landmarks: List<PoseLandmarkPoint>,
    val confidence: Float,
)

data class PoseLandmarkPoint(
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float?,
    val visibility: Float?,
)

data class ChairStandAnalysisState(
    val repetitionCount: Int,
    val elapsedSeconds: Int,
    val confidence: Float,
    val isFullBodyVisible: Boolean,
    val warningMessage: String?,
    val postureMessage: String? = null,
    val isArmUseSuspected: Boolean = false,
    val isStandingOrRising: Boolean = false,
)

data class ChairStandAnalysisResult(
    val repetitionCount: Int,
    val averageRepSeconds: Float?,
    val fastestRepSeconds: Float?,
    val slowestRepSeconds: Float?,
    val trunkLeanScore: Float?,
    val symmetryScore: Float?,
    val stabilityScore: Float?,
    val confidence: Float,
    val recommendationLevel: String,
    val armUseDisqualified: Boolean = false,
)
