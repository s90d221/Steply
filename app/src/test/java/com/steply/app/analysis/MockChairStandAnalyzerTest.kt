package com.steply.app.analysis

import com.steply.app.domain.usecase.ChairStandRecommendationLevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MockChairStandAnalyzerTest {
    @Test
    fun `manual repetitions finish with demo confidence and calculated average`() {
        val analyzer = MockChairStandAnalyzer()
        analyzer.startSession(userId = "user-a", startedAt = 1_000L)

        repeat(10) {
            analyzer.addManualRepetition()
        }

        val result = analyzer.finishSession(completedAt = 31_000L)

        assertEquals(10, result.repetitionCount)
        assertEquals(3f, result.averageRepSeconds ?: 0f, 0.001f)
        assertEquals(0.85f, result.confidence, 0.001f)
        assertEquals(ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED, result.recommendationLevel)
        assertNull(result.trunkLeanScore)
        assertNull(result.symmetryScore)
        assertNull(result.stabilityScore)
    }

    @Test
    fun `reset clears manual repetition state`() {
        val analyzer = MockChairStandAnalyzer()
        analyzer.startSession(userId = "user-a", startedAt = 1_000L)
        analyzer.addManualRepetition()

        analyzer.reset()
        val result = analyzer.finishSession(completedAt = 31_000L)

        assertEquals(0, result.repetitionCount)
        assertNull(result.averageRepSeconds)
        assertEquals(ChairStandRecommendationLevelCalculator.RECHECK, result.recommendationLevel)
    }
}
