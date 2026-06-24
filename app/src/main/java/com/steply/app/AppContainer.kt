package com.steply.app

import android.content.Context
import com.steply.app.data.local.SettingsDataStore
import com.steply.app.data.local.database.SteplyDatabase
import com.steply.app.data.repository.ExerciseRecommendationRepository
import com.steply.app.data.repository.ScreeningRepository
import com.steply.app.data.repository.SettingsRepository
import com.steply.app.data.repository.UserProfileRepository
import com.steply.app.domain.usecase.GenerateExerciseRecommendationsUseCase
import com.steply.app.domain.usecase.ObserveChairStandHistoryUseCase
import com.steply.app.domain.usecase.ObserveChairStandResultReportUseCase
import com.steply.app.domain.usecase.ObserveSelectedProfileUseCase

class AppContainer(context: Context) {
    private val database = SteplyDatabase.getInstance(context)
    private val settingsDataStore = SettingsDataStore(context.applicationContext)

    val settingsRepository = SettingsRepository(settingsDataStore)
    val userProfileRepository = UserProfileRepository(database.userProfileDao())
    val screeningRepository = ScreeningRepository(
        database = database,
        settingsRepository = settingsRepository,
    )
    val exerciseRecommendationRepository = ExerciseRecommendationRepository(
        database.exerciseRecommendationDao(),
    )
    val generateExerciseRecommendationsUseCase = GenerateExerciseRecommendationsUseCase(
        exerciseRecommendationRepository = exerciseRecommendationRepository,
    )

    val observeSelectedProfileUseCase = ObserveSelectedProfileUseCase(
        settingsRepository = settingsRepository,
        userProfileRepository = userProfileRepository,
    )
    val observeChairStandHistoryUseCase = ObserveChairStandHistoryUseCase(
        settingsRepository = settingsRepository,
        screeningRepository = screeningRepository,
    )
    val observeChairStandResultReportUseCase = ObserveChairStandResultReportUseCase(
        settingsRepository = settingsRepository,
        screeningRepository = screeningRepository,
    )
}
