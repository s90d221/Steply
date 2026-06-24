package com.steply.app.domain.usecase

data class RecommendationLevelFeedback(
    val title: String,
    val message: String,
)

object RecommendationLevelContent {
    fun label(recommendationLevel: String): String {
        return when (recommendationLevel) {
            ChairStandRecommendationLevelCalculator.STEADY -> "Stable"
            ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED -> "Practice Recommended"
            ChairStandRecommendationLevelCalculator.RECHECK -> "Check Again"
            else -> "Recorded"
        }
    }

    fun guidance(recommendationLevel: String): String {
        return when (recommendationLevel) {
            ChairStandRecommendationLevelCalculator.RECHECK ->
                "Let's check again slowly, or try with support from a caregiver."
            ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED ->
                "Practice may help your lower-body strength. Move at your own pace."
            else ->
                "Great work today. Keep moving gently and safely."
        }
    }

    fun feedback(recommendationLevel: String): RecommendationLevelFeedback {
        return when (recommendationLevel) {
            ChairStandRecommendationLevelCalculator.STEADY -> RecommendationLevelFeedback(
                title = "Stable movement",
                message = "Your check was recorded as steady. Keep practicing gently and safely.",
            )
            ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED -> RecommendationLevelFeedback(
                title = "Practice recommended",
                message = "A few supported exercises may help lower-body strength. Move at your own pace.",
            )
            else -> RecommendationLevelFeedback(
                title = "Check again when ready",
                message = "Try again slowly with support nearby. This is a calm reminder, not an emergency alert.",
            )
        }
    }
}
