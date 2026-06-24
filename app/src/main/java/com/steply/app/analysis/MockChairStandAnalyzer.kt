package com.steply.app.analysis

import com.steply.app.domain.usecase.ChairStandRecommendationLevelCalculator

class MockChairStandAnalyzer(
    private val durationSeconds: Int = DEFAULT_DURATION_SECONDS,
) : ChairStandAnalyzer {
    private var userId: String? = null
    private var startedAt: Long? = null
    private var latestTimestampMs: Long? = null
    private var repetitionCount: Int = 0
    private var confidence: Float = DEMO_CONFIDENCE

    override fun startSession(userId: String, startedAt: Long) {
        reset()
        this.userId = userId
        this.startedAt = startedAt
        latestTimestampMs = startedAt
    }

    override fun addFrame(frame: PoseFrame) {
        latestTimestampMs = frame.timestampMs
        confidence = frame.confidence
    }

    override fun addManualRepetition() {
        repetitionCount += 1
    }

    override fun getCurrentState(): ChairStandAnalysisState {
        return ChairStandAnalysisState(
            repetitionCount = repetitionCount,
            elapsedSeconds = elapsedSeconds(nowMs = System.currentTimeMillis()),
            confidence = confidence,
            isFullBodyVisible = true,
            warningMessage = null,
        )
    }

    override fun finishSession(completedAt: Long): ChairStandAnalysisResult {
        return ChairStandAnalysisResult(
            repetitionCount = repetitionCount,
            averageRepSeconds = if (repetitionCount > 0) durationSeconds.toFloat() / repetitionCount else null,
            fastestRepSeconds = null,
            slowestRepSeconds = null,
            trunkLeanScore = null,
            symmetryScore = null,
            stabilityScore = null,
            confidence = DEMO_CONFIDENCE,
            recommendationLevel = ChairStandRecommendationLevelCalculator.calculate(repetitionCount),
        )
    }

    override fun reset() {
        userId = null
        startedAt = null
        latestTimestampMs = null
        repetitionCount = 0
        confidence = DEMO_CONFIDENCE
    }

    private fun elapsedSeconds(nowMs: Long): Int {
        val start = startedAt ?: nowMs
        return ((nowMs - start).coerceAtLeast(0L) / 1_000L)
            .toInt()
            .coerceAtMost(durationSeconds)
    }

    private companion object {
        const val DEFAULT_DURATION_SECONDS = 30
        const val DEMO_CONFIDENCE = 0.85f
    }
}
