package com.steply.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_recommendations",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ScreeningSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("userId"),
        Index("sessionId"),
    ],
)
data class ExerciseRecommendationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val sessionId: String?,
    val title: String,
    val description: String,
    val safetyNote: String,
    val durationSeconds: Int,
    val createdAt: Long,
    val completedAt: Long?,
)
