package com.steply.app.domain.usecase

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
}
