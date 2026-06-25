package com.steply.app.domain.usecase

import com.steply.app.analysis.SteadiAssessmentRules

object ChairStandRecommendationLevelCalculator {
    const val STEADY = "steady"
    const val PRACTICE_NEEDED = "practice_needed"
    const val RECHECK = "recheck"

    fun calculate(repetitionCount: Int): String {
        return when {
            repetitionCount >= 12 -> STEADY
            repetitionCount >= 8 -> PRACTICE_NEEDED
            else -> RECHECK
        }
    }

    fun calculateWithSteadiProfile(
        repetitionCount: Int,
        ageYears: Int?,
        gender: String?,
        armUseDisqualified: Boolean = false,
    ): String {
        if (armUseDisqualified || repetitionCount <= 0) return RECHECK
        val belowAverageThreshold = SteadiAssessmentRules
            .chairStandBelowAverageThreshold(ageYears = ageYears, gender = gender)
            ?: return calculate(repetitionCount)

        return if (repetitionCount < belowAverageThreshold) {
            PRACTICE_NEEDED
        } else {
            STEADY
        }
    }
}
