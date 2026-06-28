package com.steply.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ChairStandRecommendationLevelCalculatorTest {
    @Test
    fun `counts twelve and above are steady`() {
        assertEquals(
            ChairStandRecommendationLevelCalculator.STEADY,
            ChairStandRecommendationLevelCalculator.calculate(12),
        )
        assertEquals(
            ChairStandRecommendationLevelCalculator.STEADY,
            ChairStandRecommendationLevelCalculator.calculate(16),
        )
    }

    @Test
    fun `counts from eight to eleven need practice`() {
        assertEquals(
            ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED,
            ChairStandRecommendationLevelCalculator.calculate(8),
        )
        assertEquals(
            ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED,
            ChairStandRecommendationLevelCalculator.calculate(11),
        )
    }

    @Test
    fun `counts below eight should be rechecked`() {
        assertEquals(
            ChairStandRecommendationLevelCalculator.RECHECK,
            ChairStandRecommendationLevelCalculator.calculate(0),
        )
        assertEquals(
            ChairStandRecommendationLevelCalculator.RECHECK,
            ChairStandRecommendationLevelCalculator.calculate(7),
        )
    }
}
