package com.steply.app.domain.usecase

import com.steply.app.data.repository.SettingsRepository
import com.steply.app.data.repository.UserProfileRepository
import com.steply.app.domain.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ObserveSelectedProfileUseCase(
    private val settingsRepository: SettingsRepository,
    private val userProfileRepository: UserProfileRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<UserProfile?> {
        return settingsRepository.selectedUserId.flatMapLatest { profileId ->
            if (profileId == null) {
                flowOf(null)
            } else {
                userProfileRepository.observeProfileById(profileId).map { profile ->
                    profile?.takeIf { it.archivedAt == null }
                }
            }
        }
    }
}
