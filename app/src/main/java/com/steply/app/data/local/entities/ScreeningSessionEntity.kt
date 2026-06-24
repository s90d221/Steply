package com.steply.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screening_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("userId")],
)
data class ScreeningSessionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: String,
    val startedAt: Long,
    val completedAt: Long,
    val durationSeconds: Int,
    val confidence: Float,
    val notes: String?,
)
