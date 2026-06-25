package com.steply.app.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaPipeChairStandAnalyzerTest {
    @Test
    fun `counts each full stand after seated reset`() {
        val analyzer = MediaPipeChairStandAnalyzer()
        analyzer.startSession(userId = "user-a", startedAt = 1_000L)

        listOf(
            seatedFrame(1_000L),
            seatedFrame(1_100L),
            standingFrame(1_200L),
            standingFrame(1_300L),
            seatedFrame(1_400L),
            seatedFrame(1_500L),
            standingFrame(1_600L),
            standingFrame(1_700L),
        ).forEach(analyzer::addFrame)

        val result = analyzer.finishSession(completedAt = 31_000L)

        assertEquals(2, result.repetitionCount)
        assertEquals(15f, result.averageRepSeconds ?: 0f, 0.001f)
        assertTrue(result.confidence > 0.9f)
    }

    @Test
    fun `counts halfway rise at the end of the thirty seconds`() {
        val analyzer = MediaPipeChairStandAnalyzer()
        analyzer.startSession(userId = "user-a", startedAt = 1_000L)

        analyzer.addFrame(seatedFrame(1_000L))
        analyzer.addFrame(seatedFrame(1_100L))
        analyzer.addFrame(risingFrame(31_000L))

        val result = analyzer.finishSession(completedAt = 31_000L)

        assertEquals(1, result.repetitionCount)
    }

    @Test
    fun `arm support disqualifies official chair stand score`() {
        val analyzer = MediaPipeChairStandAnalyzer()
        analyzer.startSession(userId = "user-a", startedAt = 1_000L)

        analyzer.addFrame(seatedFrame(1_000L))
        analyzer.addFrame(risingFrame(1_100L, wrists = WristPosition.Supporting))
        analyzer.addFrame(risingFrame(1_200L, wrists = WristPosition.Supporting))
        analyzer.addFrame(risingFrame(1_300L, wrists = WristPosition.Supporting))
        analyzer.addFrame(standingFrame(1_400L, wrists = WristPosition.Supporting))
        analyzer.addFrame(standingFrame(1_500L, wrists = WristPosition.Supporting))

        val state = analyzer.getCurrentState()
        val result = analyzer.finishSession(completedAt = 31_000L)

        assertEquals(0, result.repetitionCount)
        assertTrue(result.armUseDisqualified)
        assertTrue(state.warningMessage?.contains("0") == true)
    }

    private fun seatedFrame(timestampMs: Long): PoseFrame {
        return poseFrame(
            timestampMs = timestampMs,
            leftHip = Point(0.46f, 0.62f),
            rightHip = Point(0.54f, 0.62f),
            leftKnee = Point(0.46f, 0.72f),
            rightKnee = Point(0.54f, 0.72f),
            leftAnkle = Point(0.56f, 0.72f),
            rightAnkle = Point(0.44f, 0.72f),
        )
    }

    private fun risingFrame(
        timestampMs: Long,
        wrists: WristPosition = WristPosition.Crossed,
    ): PoseFrame {
        return poseFrame(
            timestampMs = timestampMs,
            leftHip = Point(0.46f, 0.56f),
            rightHip = Point(0.54f, 0.56f),
            leftKnee = Point(0.46f, 0.72f),
            rightKnee = Point(0.54f, 0.72f),
            leftAnkle = Point(0.55f, 0.82f),
            rightAnkle = Point(0.45f, 0.82f),
            wrists = wrists,
        )
    }

    private fun standingFrame(
        timestampMs: Long,
        wrists: WristPosition = WristPosition.Crossed,
    ): PoseFrame {
        return poseFrame(
            timestampMs = timestampMs,
            leftHip = Point(0.46f, 0.50f),
            rightHip = Point(0.54f, 0.50f),
            leftKnee = Point(0.46f, 0.70f),
            rightKnee = Point(0.54f, 0.70f),
            leftAnkle = Point(0.46f, 0.90f),
            rightAnkle = Point(0.54f, 0.90f),
            wrists = wrists,
        )
    }

    private fun poseFrame(
        timestampMs: Long,
        leftHip: Point,
        rightHip: Point,
        leftKnee: Point,
        rightKnee: Point,
        leftAnkle: Point,
        rightAnkle: Point,
        wrists: WristPosition = WristPosition.Crossed,
    ): PoseFrame {
        val leftShoulder = Point(0.45f, 0.25f)
        val rightShoulder = Point(0.55f, 0.25f)
        val leftWrist = when (wrists) {
            WristPosition.Crossed -> Point(0.53f, 0.34f)
            WristPosition.Supporting -> Point(0.43f, 0.72f)
        }
        val rightWrist = when (wrists) {
            WristPosition.Crossed -> Point(0.47f, 0.34f)
            WristPosition.Supporting -> Point(0.57f, 0.72f)
        }

        return PoseFrame(
            timestampMs = timestampMs,
            confidence = 0.96f,
            landmarks = listOf(
                PoseLandmarks.LeftShoulder to leftShoulder,
                PoseLandmarks.RightShoulder to rightShoulder,
                PoseLandmarks.LeftWrist to leftWrist,
                PoseLandmarks.RightWrist to rightWrist,
                PoseLandmarks.LeftHip to leftHip,
                PoseLandmarks.RightHip to rightHip,
                PoseLandmarks.LeftKnee to leftKnee,
                PoseLandmarks.RightKnee to rightKnee,
                PoseLandmarks.LeftAnkle to leftAnkle,
                PoseLandmarks.RightAnkle to rightAnkle,
            ).map { (name, point) ->
                PoseLandmarkPoint(
                    name = name,
                    x = point.x,
                    y = point.y,
                    z = 0f,
                    visibility = 0.96f,
                )
            },
        )
    }

    private data class Point(
        val x: Float,
        val y: Float,
    )

    private enum class WristPosition {
        Crossed,
        Supporting,
    }
}
