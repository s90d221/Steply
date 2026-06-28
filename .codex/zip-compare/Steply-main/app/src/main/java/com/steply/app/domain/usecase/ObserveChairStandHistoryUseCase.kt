package com.steply.app.domain.usecase

import com.steply.app.data.repository.ScreeningRepository
import com.steply.app.data.repository.SettingsRepository
import com.steply.app.domain.model.ChairStandHistoryItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ObserveChairStandHistoryUseCase(
    private val settingsRepository: SettingsRepository,
    private val screeningRepository: ScreeningRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<ChairStandHistoryItem>> {
        return settingsRepository.selectedUserId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(emptyList())
            } else {
                screeningRepository.observeHistoryForUser(userId)
            }
        }
    }
}
