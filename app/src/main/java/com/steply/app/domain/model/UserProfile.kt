package com.steply.app.domain.model

data class UserProfile(
    val id: String,
    val displayName: String,
    val birthYear: Int?,
    val gender: String?,
    val heightCm: Int?,
    val mobilityNote: String?,
    val emergencyNote: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?,
)
