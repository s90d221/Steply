package com.steply.app.domain.usecase

import com.steply.app.data.local.entities.ChairStandResultEntity
import com.steply.app.data.local.entities.ExerciseRecommendationEntity
import com.steply.app.data.repository.ExerciseRecommendationRepository
import java.util.UUID

class GenerateExerciseRecommendationsUseCase(
    private val exerciseRecommendationRepository: ExerciseRecommendationRepository,
) {
    suspend operator fun invoke(
        userId: String,
        sessionId: String,
        result: ChairStandResultEntity,
    ): List<ExerciseRecommendationEntity> {
        require(result.userId == userId) {
            "Exercise recommendations must be generated for the result owner."
        }
        require(result.sessionId == sessionId) {
            "Exercise recommendations must be connected to the result session."
        }

        val existing = exerciseRecommendationRepository.getRecommendationsForUserSession(
            userId = userId,
            sessionId = sessionId,
        )
        if (existing.isNotEmpty()) return existing

        val now = System.currentTimeMillis()
        val recommendations = ExerciseRecommendationTemplates.forLevel(result.recommendationLevel).map { template ->
            ExerciseRecommendationEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                sessionId = sessionId,
                title = template.title,
                description = template.description,
                safetyNote = template.safetyNote,
                durationSeconds = template.durationSeconds,
                createdAt = now,
                completedAt = null,
            )
        }

        exerciseRecommendationRepository.saveRecommendations(recommendations)
        return recommendations
    }
}
