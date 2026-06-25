package com.steply.app.analysis

object SteadiAssessmentRules {
    const val ChairStandDurationSeconds = 30
    const val BalanceHoldSeconds = 10
    const val TugRiskSeconds = 12

    const val ChairStandRuleSummary =
        "Count each full stand in 30 seconds. If the person is more than halfway up when time ends, count it."
    const val ChairStandArmRule =
        "If arms are used to stand, the official Chair Stand score is 0."
    const val BalanceRuleSummary =
        "Hold each balance position for 10 seconds without moving the feet or needing support."
    const val TugRuleSummary =
        "Time standing up, walking 10 feet, turning, walking back, and sitting down. 12 seconds or more indicates fall risk."

    fun chairStandBelowAverageThreshold(ageYears: Int?, gender: String?): Int? {
        if (ageYears == null || gender == null) return null
        val table = when {
            gender.trim().lowercase().startsWith("f") ||
                gender.trim().lowercase().contains("woman") ||
                gender.trim().lowercase().contains("female") -> womenChairStandBelowAverage
            gender.trim().lowercase().startsWith("m") ||
                gender.trim().lowercase().contains("man") ||
                gender.trim().lowercase().contains("male") -> menChairStandBelowAverage
            else -> return null
        }
        return table.firstOrNull { ageYears in it.ageRange }?.belowAverageScore
    }

    private val menChairStandBelowAverage = listOf(
        ChairStandThreshold(60..64, 14),
        ChairStandThreshold(65..69, 12),
        ChairStandThreshold(70..74, 12),
        ChairStandThreshold(75..79, 11),
        ChairStandThreshold(80..84, 10),
        ChairStandThreshold(85..89, 8),
        ChairStandThreshold(90..94, 7),
    )

    private val womenChairStandBelowAverage = listOf(
        ChairStandThreshold(60..64, 12),
        ChairStandThreshold(65..69, 11),
        ChairStandThreshold(70..74, 10),
        ChairStandThreshold(75..79, 10),
        ChairStandThreshold(80..84, 9),
        ChairStandThreshold(85..89, 8),
        ChairStandThreshold(90..94, 4),
    )
}

private data class ChairStandThreshold(
    val ageRange: IntRange,
    val belowAverageScore: Int,
)
