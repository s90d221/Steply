package com.steply.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.steply.app.AppContainer
import com.steply.app.ui.screens.chaircheck.ChairCheckViewModel
import com.steply.app.ui.screens.chaircheck.ChairCheckScreen
import com.steply.app.ui.screens.history.HistoryScreen
import com.steply.app.ui.screens.history.HistoryViewModel
import com.steply.app.ui.screens.home.HomeScreen
import com.steply.app.ui.screens.home.HomeViewModel
import com.steply.app.ui.screens.onboarding.OnboardingScreen
import com.steply.app.ui.screens.onboarding.OnboardingViewModel
import com.steply.app.ui.screens.profile.AddEditProfileScreen
import com.steply.app.ui.screens.profile.AddEditProfileViewModel
import com.steply.app.ui.screens.profile.ProfileListScreen
import com.steply.app.ui.screens.profile.ProfileListViewModel
import com.steply.app.ui.screens.recommendation.RecommendationScreen
import com.steply.app.ui.screens.recommendation.RecommendationViewModel
import com.steply.app.ui.screens.result.ResultScreen
import com.steply.app.ui.screens.result.ResultViewModel
import com.steply.app.ui.screens.safety.SafetyScreen
import com.steply.app.ui.screens.settings.SettingsScreen
import com.steply.app.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

@Composable
fun SteplyApp(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    var startRoute by remember(appContainer) { mutableStateOf<String?>(null) }

    LaunchedEffect(appContainer) {
        startRoute = combine(
            appContainer.settingsRepository.onboardingCompleted,
            appContainer.settingsRepository.selectedUserId,
        ) { onboardingCompleted, selectedUserId ->
            when {
                !onboardingCompleted -> Routes.Onboarding
                selectedUserId == null -> Routes.ProfileList
                else -> Routes.Home
            }
        }.first()
    }

    val resolvedStartRoute = startRoute
    if (resolvedStartRoute == null) {
        SteplyLoadingScreen()
        return
    }

    AppNavGraph(
        appContainer = appContainer,
        navController = navController,
        startDestination = resolvedStartRoute,
    )
}

@Composable
fun AppNavGraph(
    appContainer: AppContainer,
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.Onboarding) {
            val viewModel: OnboardingViewModel = viewModel(
                factory = OnboardingViewModel.factory(appContainer),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.completed) {
                if (uiState.completed) {
                    navController.navigate(Routes.ProfileList) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                        launchSingleTop = true
                    }
                    viewModel.onNavigationHandled()
                }
            }

            OnboardingScreen(
                uiState = uiState,
                onContinue = viewModel::completeOnboarding,
            )
        }

        composable(Routes.ProfileList) {
            val viewModel: ProfileListViewModel = viewModel(
                factory = ProfileListViewModel.factory(appContainer),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ProfileListScreen(
                uiState = uiState,
                onSelectProfile = { profileId ->
                    viewModel.selectProfile(profileId) { navController.navigateHome() }
                },
                onAddProfile = { navController.navigate(Routes.addProfile()) },
                onEditProfile = { profileId -> navController.navigate(Routes.editProfile(profileId)) },
                onArchiveProfile = viewModel::requestArchive,
                onConfirmArchive = viewModel::confirmArchive,
                onDismissArchive = viewModel::dismissArchiveDialog,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.AddEditProfile,
            arguments = listOf(
                navArgument("profileId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val profileId = entry.arguments?.getString("profileId")
            val viewModel: AddEditProfileViewModel = viewModel(
                factory = AddEditProfileViewModel.factory(appContainer, profileId),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AddEditProfileScreen(
                uiState = uiState,
                onDisplayNameChanged = viewModel::onDisplayNameChanged,
                onBirthYearChanged = viewModel::onBirthYearChanged,
                onGenderChanged = viewModel::onGenderChanged,
                onHeightCmChanged = viewModel::onHeightCmChanged,
                onMobilityNoteChanged = viewModel::onMobilityNoteChanged,
                onEmergencyNoteChanged = viewModel::onEmergencyNoteChanged,
                onSaveProfile = viewModel::save,
                onCancel = { navController.popBackStack() },
                onSaved = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.ProfileList) {
                            launchSingleTop = true
                        }
                    }
                },
                onSavedHandled = viewModel::onSavedNavigationHandled,
            )
        }

        composable(Routes.Home) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.factory(appContainer),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.shouldChooseProfile) {
                if (uiState.shouldChooseProfile) {
                    navController.navigate(Routes.ProfileList) {
                        popUpTo(Routes.Home) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            HomeScreen(
                uiState = uiState,
                onStartChairCheck = { navController.navigate(Routes.SafetySetup) },
                onOpenHistory = { navController.navigate(Routes.History) },
                onOpenRecommendations = { navController.navigate(Routes.Recommendation) },
                onChangeProfile = { navController.navigate(Routes.ProfileList) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
            )
        }

        composable(Routes.SafetySetup) {
            SafetyScreen(
                onBack = { navController.popBackStack() },
                onStart = { navController.navigate(Routes.ChairStandCheck) },
            )
        }

        composable(Routes.ChairStandCheck) {
            val viewModel: ChairCheckViewModel = viewModel(
                factory = ChairCheckViewModel.factory(appContainer),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ChairCheckScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onStartCountdown = viewModel::startCountdown,
                onIncrement = viewModel::incrementRepetition,
                onComplete = viewModel::complete,
                onRequestStop = viewModel::requestStop,
                onDismissStop = viewModel::dismissStopConfirmation,
                onConfirmStop = viewModel::confirmStop,
                onCancelled = { navController.navigateHome() },
                onSaved = { resultId ->
                    navController.navigate(Routes.result(resultId)) {
                        popUpTo(Routes.ChairStandCheck) { inclusive = true }
                    }
                },
                onSavedHandled = viewModel::onResultNavigationHandled,
            )
        }

        composable(
            route = Routes.Result,
            arguments = listOf(navArgument("resultId") { type = NavType.StringType }),
        ) { entry ->
            val resultId = entry.arguments?.getString("resultId") ?: return@composable
            val viewModel: ResultViewModel = viewModel(
                factory = ResultViewModel.factory(appContainer, resultId),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.shouldChooseProfile) {
                if (uiState.shouldChooseProfile) {
                    navController.navigate(Routes.ProfileList) {
                        launchSingleTop = true
                    }
                }
            }

            ResultScreen(
                uiState = uiState,
                onBackHome = { navController.navigateHome() },
                onOpenRecommendations = { sessionId -> navController.navigate(Routes.recommendationForSession(sessionId)) },
                onOpenHistory = { navController.navigate(Routes.History) },
            )
        }

        composable(Routes.Recommendation) {
            val viewModel: RecommendationViewModel = viewModel(
                factory = RecommendationViewModel.factory(appContainer),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isLoading, uiState.selectedUserName, uiState.errorMessage) {
                if (!uiState.isLoading && uiState.selectedUserName == null && uiState.errorMessage != null) {
                    navController.navigate(Routes.ProfileList) {
                        launchSingleTop = true
                    }
                }
            }

            RecommendationScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onMarkCompleted = viewModel::markCompleted,
                onBackHome = { navController.navigateHome() },
                onOpenHistory = { navController.navigate(Routes.History) },
                onStartChairCheck = { navController.navigate(Routes.SafetySetup) },
            )
        }

        composable(
            route = Routes.RecommendationForSession,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { entry ->
            val sessionId = entry.arguments?.getString("sessionId") ?: return@composable
            val viewModel: RecommendationViewModel = viewModel(
                factory = RecommendationViewModel.factory(
                    appContainer = appContainer,
                    sessionId = sessionId,
                ),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isLoading, uiState.selectedUserName, uiState.errorMessage) {
                if (!uiState.isLoading && uiState.selectedUserName == null && uiState.errorMessage != null) {
                    navController.navigate(Routes.ProfileList) {
                        launchSingleTop = true
                    }
                }
            }

            RecommendationScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onMarkCompleted = viewModel::markCompleted,
                onBackHome = { navController.navigateHome() },
                onOpenHistory = { navController.navigate(Routes.History) },
                onStartChairCheck = { navController.navigate(Routes.SafetySetup) },
            )
        }

        composable(Routes.History) {
            val viewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModel.factory(appContainer),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isLoading, uiState.selectedUser) {
                if (!uiState.isLoading && uiState.selectedUser == null) {
                    navController.navigate(Routes.ProfileList) {
                        launchSingleTop = true
                    }
                }
            }

            HistoryScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onBackHome = { navController.navigateHome() },
                onChangeProfile = { navController.navigate(Routes.ProfileList) },
                onStartChairCheck = { navController.navigate(Routes.SafetySetup) },
                onOpenResult = { resultId -> navController.navigate(Routes.result(resultId)) },
            )
        }

        composable(Routes.Settings) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(appContainer),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isLoading, uiState.selectedUser) {
                if (!uiState.isLoading && uiState.selectedUser == null) {
                    navController.navigate(Routes.ProfileList) {
                        launchSingleTop = true
                    }
                }
            }

            SettingsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onBackHome = { navController.navigateHome() },
                onChangeProfile = { navController.navigate(Routes.ProfileList) },
                onExportSelectedUserData = viewModel::exportSelectedUserData,
                onExportShared = viewModel::onExportShared,
                onDeleteSelectedUserData = {
                    viewModel.deleteSelectedUserData {
                        navController.navigateProfileListAfterDataDeletion()
                    }
                },
                onDeleteAllLocalData = {
                    viewModel.deleteAllLocalData {
                        navController.navigateProfileListAfterDataDeletion()
                    }
                },
            )
        }
    }
}

private fun NavHostController.navigateHome() {
    if (!popBackStack(Routes.Home, inclusive = false)) {
        navigate(Routes.Home) {
            popUpTo(Routes.Onboarding) { inclusive = true }
            launchSingleTop = true
        }
    }
}

private fun NavHostController.navigateProfileListAfterDataDeletion() {
    navigate(Routes.ProfileList) {
        popUpTo(Routes.Home) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun SteplyLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Steply is getting ready.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
