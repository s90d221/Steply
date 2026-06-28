package com.steply.app.domain.model

data class ChairStandHistoryItem(
    val resultId: String,
    val userId: String,
    val repetitionCount: Int,
    val durationSeconds: Int,
    val recommendationLevel: String,
    val createdAt: Long,
)

data class ChairStandResultReport(
    val resultId: String,
    val sessionId: String,
    val userId: String,
    val userDisplayName: String,
    val repetitionCount: Int,
    val durationSeconds: Int,
    val confidence: Float,
    val averageRepSeconds: Float?,
    val fastestRepSeconds: Float?,
    val slowestRepSeconds: Float?,
    val trunkLeanScore: Float?,
    val symmetryScore: Float?,
    val stabilityScore: Float?,
    val recommendationLevel: String,
    val createdAt: Long,
    val startedAt: Long,
    val completedAt: Long,
    val guidanceText: String,
    val recommendations: List<ExerciseRecommendation>,
)

data class ExerciseRecommendation(
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
