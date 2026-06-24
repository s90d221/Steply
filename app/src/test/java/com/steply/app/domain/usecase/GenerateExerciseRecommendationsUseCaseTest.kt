package com.steply.app.domain.usecase

import com.steply.app.data.local.dao.ExerciseRecommendationDao
import com.steply.app.data.local.entities.ChairStandResultEntity
import com.steply.app.data.local.entities.ExerciseRecommendationEntity
import com.steply.app.data.repository.ExerciseRecommendationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateExerciseRecommendationsUseCaseTest {
    @Test
    fun `recommendations are generated once for the same user session`() = runBlocking {
        val fakeDao = FakeExerciseRecommendationDao()
        val repository = ExerciseRecommendationRepository(fakeDao)
        val useCase = GenerateExerciseRecommendationsUseCase(repository)

        val result = chairStandResult(
            userId = "user-a",
            sessionId = "session-a",
            recommendationLevel = ChairStandRecommendationLevelCalculator.STEADY,
        )

        val first = useCase(
            userId = "user-a",
            sessionId = "session-a",
            result = result,
        )
        val second = useCase(
            userId = "user-a",
            sessionId = "session-a",
            result = result,
        )

        assertEquals(2, first.size)
        assertEquals(first.map { it.id }, second.map { it.id })
        assertEquals(2, repository.getRecommendationsForUserSession("user-a", "session-a").size)
    }

    @Test
    fun `repository queries recommendations by user id without mixing users`() = runBlocking {
        val repository = ExerciseRecommendationRepository(FakeExerciseRecommendationDao())

        repository.saveRecommendations(
            listOf(
                exerciseRecommendation(id = "rec-a", userId = "user-a", sessionId = "session-a", title = "User A Exercise"),
                exerciseRecommendation(id = "rec-b", userId = "user-b", sessionId = "session-b", title = "User B Exercise"),
            ),
        )

        val userA = repository.observeRecommendationsForUser("user-a").first()
        val userB = repository.observeRecommendationsForUser("user-b").first()

        assertEquals(listOf("User A Exercise"), userA.map { it.title })
        assertEquals(listOf("User B Exercise"), userB.map { it.title })
    }

    @Test
    fun `mark completed updates only matching user recommendation`() = runBlocking {
        val repository = ExerciseRecommendationRepository(FakeExerciseRecommendationDao())

        repository.saveRecommendations(
            listOf(
                exerciseRecommendation(id = "rec-a", userId = "user-a", sessionId = "session-a", title = "User A Exercise"),
                exerciseRecommendation(id = "rec-b", userId = "user-b", sessionId = "session-b", title = "User B Exercise"),
            ),
        )

        assertFalse(repository.markCompletedForUser(id = "rec-a", userId = "user-b", completedAt = 3_000L))
        assertTrue(repository.markCompletedForUser(id = "rec-a", userId = "user-a", completedAt = 4_000L))

        val userA = repository.observeRecommendationsForUser("user-a").first()
        val userB = repository.observeRecommendationsForUser("user-b").first()

        assertEquals(4_000L, userA.single().completedAt)
        assertNull(userB.single().completedAt)
    }
}

private class FakeExerciseRecommendationDao : ExerciseRecommendationDao {
    private val stored = MutableStateFlow<List<ExerciseRecommendationEntity>>(emptyList())

    override suspend fun insertRecommendations(recommendations: List<ExerciseRecommendationEntity>) {
        stored.value = stored.value + recommendations
    }

    override fun observeRecommendationsForUser(userId: String): Flow<List<ExerciseRecommendationEntity>> {
        return stored.map { recommendations ->
            recommendations
                .filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
        }
    }

    override fun observeRecommendationsForUserSession(
        userId: String,
        sessionId: String,
    ): Flow<List<ExerciseRecommendationEntity>> {
        return stored.map { recommendations ->
            recommendations
                .filter { it.userId == userId && it.sessionId == sessionId }
                .sortedByDescending { it.createdAt }
        }
    }

    override suspend fun getRecommendationsForUserSession(
        userId: String,
        sessionId: String,
    ): List<ExerciseRecommendationEntity> {
        return stored.value
            .filter { it.userId == userId && it.sessionId == sessionId }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun markCompletedForUser(
        id: String,
        userId: String,
        completedAt: Long,
    ): Int {
        var updated = 0
        stored.value = stored.value.map { recommendation ->
            if (recommendation.id == id && recommendation.userId == userId) {
                updated += 1
                recommendation.copy(completedAt = completedAt)
            } else {
                recommendation
            }
        }
        return updated
    }

    override suspend fun deleteRecommendationsForUser(userId: String) {
        stored.value = stored.value.filterNot { it.userId == userId }
    }

    override suspend fun deleteAllRecommendations() {
        stored.value = emptyList()
    }
}

private fun chairStandResult(
    userId: String,
    sessionId: String,
    recommendationLevel: String,
): ChairStandResultEntity {
    return ChairStandResultEntity(
        id = "result-$sessionId",
        sessionId = sessionId,
        userId = userId,
        repetitionCount = 12,
        averageRepSeconds = 2.5f,
        fastestRepSeconds = null,
        slowestRepSeconds = null,
        trunkLeanScore = null,
        symmetryScore = null,
        stabilityScore = null,
        recommendationLevel = recommendationLevel,
        createdAt = 1_000L,
    )
}

private fun exerciseRecommendation(
    id: String,
    userId: String,
    sessionId: String,
    title: String,
): ExerciseRecommendationEntity {
    return ExerciseRecommendationEntity(
        id = id,
        userId = userId,
        sessionId = sessionId,
        title = title,
        description = "Description",
        safetyNote = "Move slowly and safely.",
        durationSeconds = 30,
        createdAt = if (userId == "user-a") 2_000L else 1_000L,
        completedAt = null,
    )
}
