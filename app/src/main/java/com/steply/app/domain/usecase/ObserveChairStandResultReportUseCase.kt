package com.steply.app.domain.usecase

import com.steply.app.data.repository.ScreeningRepository
import com.steply.app.data.repository.SettingsRepository
import com.steply.app.domain.model.ChairStandResultReport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ObserveChairStandResultReportUseCase(
    private val settingsRepository: SettingsRepository,
    private val screeningRepository: ScreeningRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(resultId: String): Flow<ChairStandResultReport?> {
        return settingsRepository.selectedUserId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(null)
            } else {
                screeningRepository.observeResultReportForUser(
                    userId = userId,
                    resultId = resultId,
                )
            }
        }
    }
}
