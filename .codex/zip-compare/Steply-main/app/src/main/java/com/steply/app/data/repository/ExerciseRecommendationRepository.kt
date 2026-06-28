package com.steply.app.data.repository

import com.steply.app.data.local.dao.ExerciseRecommendationDao
import com.steply.app.data.local.entities.ExerciseRecommendationEntity
import kotlinx.coroutines.flow.Flow

class ExerciseRecommendationRepository(
    private val recommendationDao: ExerciseRecommendationDao,
) {
    suspend fun saveRecommendations(recommendations: List<ExerciseRecommendationEntity>) {
        recommendationDao.insertRecommendations(recommendations)
    }

    fun observeRecommendationsForUser(userId: String): Flow<List<ExerciseRecommendationEntity>> {
        return recommendationDao.observeRecommendationsForUser(userId)
    }

    fun observeRecommendationsForUserSession(
        userId: String,
        sessionId: String,
    ): Flow<List<ExerciseRecommendationEntity>> {
        return recommendationDao.observeRecommendationsForUserSession(
            userId = userId,
            sessionId = sessionId,
        )
    }

    suspend fun getRecommendationsForUserSession(
        userId: String,
        sessionId: String,
    ): List<ExerciseRecommendationEntity> {
        return recommendationDao.getRecommendationsForUserSession(
            userId = userId,
            sessionId = sessionId,
        )
    }

    suspend fun markCompletedForUser(
        id: String,
        userId: String,
        completedAt: Long = System.currentTimeMillis(),
    ): Boolean {
        return recommendationDao.markCompletedForUser(
            id = id,
            userId = userId,
            completedAt = completedAt,
        ) > 0
    }

    suspend fun deleteRecommendationsForUser(userId: String) {
        recommendationDao.deleteRecommendationsForUser(userId)
    }
}
