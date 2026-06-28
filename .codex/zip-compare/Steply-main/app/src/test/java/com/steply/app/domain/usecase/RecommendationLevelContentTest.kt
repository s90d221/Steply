package com.steply.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendationLevelContentTest {
    @Test
    fun `labels map recommendation levels to friendly display text`() {
        assertEquals("Stable", RecommendationLevelContent.label(ChairStandRecommendationLevelCalculator.STEADY))
        assertEquals(
            "Practice Recommended",
            RecommendationLevelContent.label(ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED),
        )
        assertEquals("Check Again", RecommendationLevelContent.label(ChairStandRecommendationLevelCalculator.RECHECK))
    }

    @Test
    fun `result feedback maps recommendation levels to calm messages`() {
        assertEquals(
            "Stable movement",
            RecommendationLevelContent.feedback(ChairStandRecommendationLevelCalculator.STEADY).title,
        )
        assertEquals(
            "Practice recommended",
            RecommendationLevelContent.feedback(ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED).title,
        )
        assertEquals(
            "Check again when ready",
            RecommendationLevelContent.feedback(ChairStandRecommendationLevelCalculator.RECHECK).title,
        )
    }
}
