package com.steply.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.steply.app.data.local.entities.ExerciseRecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseRecommendationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecommendations(recommendations: List<ExerciseRecommendationEntity>)

    @Query(
        """
        SELECT * FROM exercise_recommendations
        WHERE userId = :userId
        ORDER BY createdAt DESC
        """,
    )
    fun observeRecommendationsForUser(userId: String): Flow<List<ExerciseRecommendationEntity>>

    @Query(
        """
        SELECT * FROM exercise_recommendations
        WHERE userId = :userId AND sessionId = :sessionId
        ORDER BY createdAt DESC
        """,
    )
    fun observeRecommendationsForUserSession(
        userId: String,
        sessionId: String,
    ): Flow<List<ExerciseRecommendationEntity>>

    @Query(
        """
        SELECT * FROM exercise_recommendations
        WHERE userId = :userId AND sessionId = :sessionId
        ORDER BY createdAt DESC
        """,
    )
    suspend fun getRecommendationsForUserSession(
        userId: String,
        sessionId: String,
    ): List<ExerciseRecommendationEntity>

    @Query(
        """
        UPDATE exercise_recommendations
        SET completedAt = :completedAt
        WHERE id = :id AND userId = :userId
        """,
    )
    suspend fun markCompletedForUser(
        id: String,
        userId: String,
        completedAt: Long,
    ): Int

    @Query("DELETE FROM exercise_recommendations WHERE userId = :userId")
    suspend fun deleteRecommendationsForUser(userId: String)

    @Query("DELETE FROM exercise_recommendations")
    suspend fun deleteAllRecommendations()
}
