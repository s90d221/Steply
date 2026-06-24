package com.steply.app.data.repository

import com.steply.app.data.local.dao.UserProfileDao
import com.steply.app.data.local.entities.UserProfileEntity
import com.steply.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
) {
    fun observeActiveProfiles(): Flow<List<UserProfile>> {
        return userProfileDao.observeActiveProfiles().map { profiles ->
            profiles.map { it.toDomain() }
        }
    }

    suspend fun getProfileById(id: String): UserProfile? {
        return userProfileDao.getProfileById(id)?.toDomain()
    }

    fun observeProfileById(id: String): Flow<UserProfile?> {
        return userProfileDao.observeProfileById(id).map { it?.toDomain() }
    }

    suspend fun create(
        displayName: String,
        birthYear: Int? = null,
        gender: String? = null,
        heightCm: Int? = null,
        mobilityNote: String? = null,
        emergencyNote: String? = null,
    ): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        userProfileDao.insertProfile(
            UserProfileEntity(
                id = id,
                displayName = displayName.trim(),
                birthYear = birthYear,
                gender = gender?.trim()?.takeIf { it.isNotBlank() },
                heightCm = heightCm,
                mobilityNote = mobilityNote?.trim()?.takeIf { it.isNotBlank() },
                emergencyNote = emergencyNote?.trim()?.takeIf { it.isNotBlank() },
                createdAt = now,
                updatedAt = now,
                archivedAt = null,
            ),
        )
        return id
    }

    suspend fun edit(profile: UserProfile) {
        userProfileDao.updateProfile(profile.toEntity(updatedAt = System.currentTimeMillis()))
    }

    suspend fun archive(id: String, archivedAt: Long = System.currentTimeMillis()) {
        userProfileDao.archiveProfile(id = id, archivedAt = archivedAt)
    }

}

private fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        displayName = displayName,
        birthYear = birthYear,
        gender = gender,
        heightCm = heightCm,
        mobilityNote = mobilityNote,
        emergencyNote = emergencyNote,
        createdAt = createdAt,
        updatedAt = updatedAt,
        archivedAt = archivedAt,
    )
}

private fun UserProfile.toEntity(updatedAt: Long): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        displayName = displayName.trim(),
        birthYear = birthYear,
        gender = gender?.trim()?.takeIf { it.isNotBlank() },
        heightCm = heightCm,
        mobilityNote = mobilityNote?.trim()?.takeIf { it.isNotBlank() },
        emergencyNote = emergencyNote?.trim()?.takeIf { it.isNotBlank() },
        createdAt = createdAt,
        updatedAt = updatedAt,
        archivedAt = archivedAt,
    )
}
