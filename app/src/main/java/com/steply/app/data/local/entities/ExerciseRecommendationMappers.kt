package com.steply.app.data.local.entities

import com.steply.app.domain.model.ExerciseRecommendation

fun ExerciseRecommendationEntity.toDomain(): ExerciseRecommendation {
    return ExerciseRecommendation(
        id = id,
        userId = userId,
        sessionId = sessionId,
        title = title,
        description = description,
        safetyNote = safetyNote,
        durationSeconds = durationSeconds,
        createdAt = createdAt,
        completedAt = completedAt,
    )
}
