package com.steply.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chair_stand_results",
    foreignKeys = [
        ForeignKey(
            entity = ScreeningSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["sessionId"], unique = true),
        Index("userId"),
    ],
)
data class ChairStandResultEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val userId: String,
    val repetitionCount: Int,
    val averageRepSeconds: Float?,
    val fastestRepSeconds: Float?,
    val slowestRepSeconds: Float?,
    val trunkLeanScore: Float?,
    val symmetryScore: Float?,
    val stabilityScore: Float?,
    val recommendationLevel: String,
    val createdAt: Long,
)
