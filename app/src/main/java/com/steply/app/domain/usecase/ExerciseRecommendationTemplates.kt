package com.steply.app.domain.usecase

data class ExerciseRecommendationTemplate(
    val title: String,
    val description: String,
    val safetyNote: String,
    val durationSeconds: Int,
)

object ExerciseRecommendationTemplates {
    private const val STOP_IF_UNCOMFORTABLE = "Stop if you feel pain, dizziness, or discomfort."
    private const val USE_SUPPORT = "Use support or ask a caregiver to help if needed."

    fun forLevel(recommendationLevel: String): List<ExerciseRecommendationTemplate> {
        return when (recommendationLevel) {
            ChairStandRecommendationLevelCalculator.STEADY -> listOf(
                ExerciseRecommendationTemplate(
                    title = "Supported Balance Hold",
                    description = "Hold the back of a stable chair and stand comfortably for 20 seconds.",
                    safetyNote = "Sit down and rest if you feel unsteady. $USE_SUPPORT",
                    durationSeconds = 20,
                ),
                ExerciseRecommendationTemplate(
                    title = "Gentle Chair Stand Practice",
                    description = "Slowly stand up from a chair and sit back down 5 times.",
                    safetyNote = STOP_IF_UNCOMFORTABLE,
                    durationSeconds = 60,
                ),
            )
            ChairStandRecommendationLevelCalculator.PRACTICE_NEEDED -> listOf(
                ExerciseRecommendationTemplate(
                    title = "Supported Chair Stand Practice",
                    description = "Hold a stable chair or support and slowly practice standing up and sitting down 5 times.",
                    safetyNote = "Use a stable chair and do not rush. $USE_SUPPORT",
                    durationSeconds = 60,
                ),
                ExerciseRecommendationTemplate(
                    title = "Gentle Weight Shift",
                    description = "Hold a chair and slowly shift your weight from left to right.",
                    safetyNote = STOP_IF_UNCOMFORTABLE,
                    durationSeconds = 45,
                ),
            )
            else -> listOf(
                ExerciseRecommendationTemplate(
                    title = "Assisted Standing Hold",
                    description = "With support from a chair or caregiver, stand comfortably for 10 seconds.",
                    safetyNote = USE_SUPPORT,
                    durationSeconds = 10,
                ),
                ExerciseRecommendationTemplate(
                    title = "Seated Knee Extension",
                    description = "Sit in a chair and slowly straighten one knee, then lower it. Repeat gently on both sides.",
                    safetyNote = STOP_IF_UNCOMFORTABLE,
                    durationSeconds = 45,
                ),
            )
        }
    }
}
