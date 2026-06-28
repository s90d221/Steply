package com.steply.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseRecommendationTemplatesTest {
    @Test
    fun `steady creates balance and gentle chair stand recommendations`() {
        val recommendations = ExerciseRecommendationTemplates.forLevel(ChairStandRecommendationLevelCalculator.STEADY)

        assertEquals(listOf("Supported Balance Hold", "Gentle Chair Stand Practice"), recommendations.map { it.title })
        assertEquals(listOf(20, 60), recommendations.map { it.durationSeconds })
    }

    @Test
    fun `practice needed creates supported strength and weight shift recommendations`() {
        val recommendations = ExerciseRecommendationTemplates.forLevel(ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED)

        assertEquals(listOf("Supported Chair Stand Practice", "Gentle Weight Shift"), recommendations.map { it.title })
        assertEquals(listOf(60, 45), recommendations.map { it.durationSeconds })
    }

    @Test
    fun `recheck creates supported standing and seated knee recommendations`() {
        val recommendations = ExerciseRecommendationTemplates.forLevel(ChairStandRecommendationLevelCalculator.RECHECK)

        assertEquals(listOf("Assisted Standing Hold", "Seated Knee Extension"), recommendations.map { it.title })
        assertEquals(listOf(10, 45), recommendations.map { it.durationSeconds })
    }
}
