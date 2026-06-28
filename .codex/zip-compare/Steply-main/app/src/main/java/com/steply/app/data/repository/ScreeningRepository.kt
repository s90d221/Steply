package com.steply.app.data.repository

import androidx.room.withTransaction
import com.steply.app.data.local.database.SteplyDatabase
import com.steply.app.data.local.entities.ChairStandResultEntity
import com.steply.app.data.local.entities.ExerciseRecommendationEntity
import com.steply.app.data.local.entities.ScreeningSessionEntity
import com.steply.app.data.local.entities.toDomain
import com.steply.app.domain.model.ChairStandHistoryItem
import com.steply.app.domain.model.ChairStandResultReport
import com.steply.app.domain.usecase.ChairStandRecommendationLevelCalculator
import com.steply.app.domain.usecase.RecommendationLevelContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import java.util.UUID

class ScreeningRepository(
    private val database: SteplyDatabase,
    private val settingsRepository: SettingsRepository,
) {
    private val userProfileDao = database.userProfileDao()
    private val screeningSessionDao = database.screeningSessionDao()
    private val chairStandResultDao = database.chairStandResultDao()
    private val recommendationDao = database.exerciseRecommendationDao()

    suspend fun saveChairStandScreening(
        userId: String,
        repetitionCount: Int,
        durationSeconds: Int,
        startedAt: Long? = null,
        completedAt: Long = System.currentTimeMillis(),
        averageRepSeconds: Float? = null,
        fastestRepSeconds: Float? = null,
        slowestRepSeconds: Float? = null,
        trunkLeanScore: Float? = null,
        symmetryScore: Float? = null,
        stabilityScore: Float? = null,
        confidence: Float = 1f,
        recommendationLevel: String? = null,
        notes: String? = null,
    ): String {
        val resolvedStartedAt = startedAt ?: completedAt - durationSeconds * 1_000L
        val sessionId = UUID.randomUUID().toString()
        val resultId = UUID.randomUUID().toString()
        val resolvedRecommendationLevel = recommendationLevel
            ?: ChairStandRecommendationLevelCalculator.calculate(repetitionCount)

        database.withTransaction {
            screeningSessionDao.insertSession(
                ScreeningSessionEntity(
                    id = sessionId,
                    userId = userId,
                    type = CHAIR_STAND_30_SEC,
                    startedAt = resolvedStartedAt,
                    completedAt = completedAt,
                    durationSeconds = durationSeconds,
                    confidence = confidence,
                    notes = notes?.trim()?.takeIf { it.isNotBlank() },
                ),
            )
            chairStandResultDao.insertResult(
                ChairStandResultEntity(
                    id = resultId,
                    sessionId = sessionId,
                    userId = userId,
                    repetitionCount = repetitionCount,
                    averageRepSeconds = averageRepSeconds,
                    fastestRepSeconds = fastestRepSeconds,
                    slowestRepSeconds = slowestRepSeconds,
                    trunkLeanScore = trunkLeanScore,
                    symmetryScore = symmetryScore,
                    stabilityScore = stabilityScore,
                    recommendationLevel = resolvedRecommendationLevel,
                    createdAt = completedAt,
                ),
            )
        }

        return resultId
    }

    suspend fun getSessionById(id: String): ScreeningSessionEntity? {
        return screeningSessionDao.getSessionById(id)
    }

    suspend fun getResultById(id: String): ChairStandResultEntity? {
        return chairStandResultDao.getResultById(id)
    }

    fun observeSessionsForUser(userId: String): Flow<List<ScreeningSessionEntity>> {
        return screeningSessionDao.observeSessionsForUser(userId)
    }

    fun observeResultsForUser(userId: String): Flow<List<ChairStandResultEntity>> {
        return chairStandResultDao.observeResultsForUser(userId)
    }

    fun observeLatestResultForUser(userId: String): Flow<ChairStandResultEntity?> {
        return chairStandResultDao.observeLatestResultForUser(userId)
    }

    fun observeHistoryForUser(userId: String): Flow<List<ChairStandHistoryItem>> {
        return chairStandResultDao.observeResultsForUser(userId).map { results ->
            results.map { result ->
                ChairStandHistoryItem(
                    resultId = result.id,
                    userId = result.userId,
                    repetitionCount = result.repetitionCount,
                    durationSeconds = DEFAULT_CHAIR_STAND_DURATION_SECONDS,
                    recommendationLevel = result.recommendationLevel,
                    createdAt = result.createdAt,
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeResultReportForUser(
        userId: String,
        resultId: String,
    ): Flow<ChairStandResultReport?> {
        return chairStandResultDao.observeResultForUser(userId, resultId).flatMapLatest { result ->
            if (result == null) {
                flowOf(null)
            } else {
                combine(
                    recommendationDao.observeRecommendationsForUserSession(
                        userId = result.userId,
                        sessionId = result.sessionId,
                    ),
                    screeningSessionDao.observeSessionById(result.sessionId),
                    userProfileDao.observeProfileById(result.userId),
                ) { recommendations, session, profile ->
                    if (session == null || profile == null) {
                        null
                    } else {
                        result.toReport(
                            session = session,
                            userDisplayName = profile.displayName,
                            recommendations = recommendations,
                        )
                    }
                }
            }
        }
    }

    suspend fun deleteSelectedUserData(userId: String) {
        database.withTransaction {
            recommendationDao.deleteRecommendationsForUser(userId)
            chairStandResultDao.deleteResultsForUser(userId)
            screeningSessionDao.deleteSessionsForUser(userId)
            userProfileDao.deleteProfile(userId)
        }
        settingsRepository.clearSelectedUserId()
    }

    suspend fun deleteAllLocalData() {
        database.withTransaction {
            recommendationDao.deleteAllRecommendations()
            chairStandResultDao.deleteAllResults()
            screeningSessionDao.deleteAllSessions()
            userProfileDao.deleteAllProfiles()
        }
        settingsRepository.clearSelectedUserId()
    }

    private fun ChairStandResultEntity.toReport(
        session: ScreeningSessionEntity,
        userDisplayName: String,
        recommendations: List<ExerciseRecommendationEntity>,
    ): ChairStandResultReport {
        return ChairStandResultReport(
            resultId = id,
            sessionId = sessionId,
            userId = userId,
            userDisplayName = userDisplayName,
            repetitionCount = repetitionCount,
            durationSeconds = session.durationSeconds,
            confidence = session.confidence,
            averageRepSeconds = averageRepSeconds,
            fastestRepSeconds = fastestRepSeconds,
            slowestRepSeconds = slowestRepSeconds,
            trunkLeanScore = trunkLeanScore,
            symmetryScore = symmetryScore,
            stabilityScore = stabilityScore,
            recommendationLevel = recommendationLevel,
            createdAt = createdAt,
            startedAt = session.startedAt,
            completedAt = session.completedAt,
            guidanceText = RecommendationLevelContent.guidance(recommendationLevel),
            recommendations = recommendations.map { it.toDomain() },
        )
    }

    private companion object {
        const val CHAIR_STAND_30_SEC = "CHAIR_STAND_30_SEC"
        const val DEFAULT_CHAIR_STAND_DURATION_SECONDS = 30
    }
}
