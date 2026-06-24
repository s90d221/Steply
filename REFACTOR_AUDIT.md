# Steply Refactor Audit

This audit is a behavior-preserving cleanup plan for the current Steply MVP. It focuses on reducing duplication, removing unused code, centralizing UI/copy/business mappings, and tightening test coverage without changing the product behavior, local-first architecture, or demo flow.

## 1. Summary of Current Code Quality

The app is in a good MVP state: the UI is cohesive and warm, the data model is local-first, navigation is straightforward, and the core chair-stand flow is implemented without backend, auth, cloud sync, Firebase, Internet permission, CameraX, or MediaPipe. Room SQLite is used for app data, DataStore is used for selected profile/onboarding settings, and user-facing flows are scoped around the selected profile.

The codebase is also carrying expected MVP growth artifacts:

- Some use cases and route helpers are no longer used by the current screens.
- UI polish introduced reusable components, but a few legacy aliases and one-off layout styles remain.
- Product copy and status mappings are partially centralized, but some feedback, disclaimer, status, date, duration, and confidence formatting logic is still scattered.
- Some DAO/repository methods expose session-only or id-only operations that are not user-scoped. Current UI flows mostly use user-scoped methods, but the extra APIs increase future misuse risk.
- Business rules are mostly centralized, especially recommendation level and history summary calculations, but UI-level copy for those rules is duplicated.
- Existing tests cover recommendation rules, recommendation generation, history summary, and mock analysis behavior. Missing coverage is mostly around Room-backed user isolation, export/delete behavior, and selected-profile flows.

Audit checks found no authored Korean UI text in `app/src/main` or `app/src/test`. Broad repository searches may find Korean strings inside generated build outputs or dependency resources under `app/build` and `.gradle`; those are not app-authored UI strings and should not be edited.

The Android manifest has no app permissions, including no `INTERNET` permission. Gradle dependencies do not include Firebase, auth, backend, cloud sync, CameraX, MediaPipe, or network clients.

## 2. Safe Cleanup Opportunities

### Project Structure

- Remove or consolidate unused use cases:
  - `CreateUserProfileUseCase`
  - `SelectUserProfileUseCase`
  - `RecordChairStandCheckUseCase`
- Review wrapper screens that only delegate to another implementation:
  - `SafetySetupScreen` wraps `SafetyScreen`
  - `ChairStandCheckScreen` wraps `ChairCheckScreen`
- Keep the wrapper names if they improve route readability; otherwise, collapse them in a small navigation-safe change.
- Keep `MockChairStandAnalyzer` and camera/pose TODOs as intentional MVP placeholders. Do not replace them yet.
- Consider moving shared mapping functions out of private repository/screen code so domain/data/UI conversion has one source of truth.

### Dependencies

- Keep current core dependencies: Compose, Material 3, Navigation Compose, Room, DataStore, Lifecycle, Coroutines, and tests.
- Review Android UI test dependencies because there are currently no `androidTest` files:
  - `androidx.compose.ui:ui-test-junit4`
  - `androidx.test.ext:junit`
  - `androidx.test.espresso:espresso-core`
  - `androidx.compose.ui:ui-test-manifest`
- Either add a small smoke UI test later or remove unused UI test dependencies in a separate dependency-only cleanup.
- No cleanup is needed for Firebase/auth/backend/network dependencies because none were found.
- No manifest permission cleanup is needed because no permissions were found.

### UI Components

- Split the large shared component file into smaller files by responsibility:
  - `SteplyScaffold.kt`
  - `SteplyTopBar.kt`
  - `SteplyButtons.kt`
  - `SteplyCards.kt`
  - `SteplyMetrics.kt`
  - `SteplyNotices.kt`
  - `SteplyForms.kt`
  - `SteplyTokens.kt`
- Remove unused or legacy component aliases after confirming no call sites:
  - `SteplyHeroCard`
  - `PrimaryActionButton`
  - `NoticeBox`
- Continue using reusable components for cards, buttons, empty states, safety notices, local-data notices, status pills, profile avatars, metric cards, and exercise cards.
- Centralize repeated layout constants:
  - screen padding
  - card padding
  - button height
  - card corner radius
  - button corner radius
  - small chip radius
  - common vertical spacing
- Reduce hardcoded `Color(...)`, `RoundedCornerShape(...)`, `PaddingValues(...)`, and repeated `.dp` values in screens where they represent shared design tokens.
- Keep local one-off dimensions when they are truly screen-specific, such as timer circle size or hero illustration proportions.

### UI Copy

- Add a small copy/constants layer for repeated product language:
  - medical disclaimer
  - safety reminder
  - local-only storage notice
  - selected-profile missing message
  - generic retry/error text
  - delete confirmation text
  - recommendation feedback text
- Keep all user-visible copy in English.
- Continue avoiding alarming terms such as "risk score", "failure", "danger", "bad result", "unsafe person", and "abnormal".
- Preserve these required safety messages:
  - "Steply is not a medical diagnosis tool."
  - "Stop if you feel pain, dizziness, or discomfort."
  - "Use support or ask a caregiver to help if needed."
- Avoid moving all strings to Android resources unless localization is planned. A Kotlin copy object is simpler for this MVP and keeps Compose call sites readable.

### Navigation

- Review the unused result-specific recommendation route:
  - `Routes.RecommendationForResult`
  - `Routes.recommendation(resultId)`
- Current result flow navigates to `recommendationForSession(sessionId)`. If the result-id route is not needed for a near-term deep link or alternate entry point, remove it with associated graph handling.
- Keep graceful selected-profile handling in Home, History, Recommendations, and Settings.
- Add a small shared helper or UI state convention for "missing selected profile" so every screen handles it consistently.
- Keep route argument shapes simple. Do not introduce larger navigation arguments or serialized models.

### Data Layer

- Keep Room and DataStore as the only persistence layers.
- Preserve the existing transaction for saving chair-stand session and result together.
- Preserve userId/sessionId validation when writing results.
- Review and remove or restrict DAO/repository methods that are not user-scoped:
  - `ExerciseRecommendationDao.observeRecommendationsForSession(sessionId)`
  - `ExerciseRecommendationDao.getRecommendationsForSession(sessionId)`
  - `ExerciseRecommendationDao.markCompleted(id, completedAt)`
- Prefer user-scoped methods for recommendations and completion updates:
  - session plus userId queries
  - recommendation id plus userId updates
- Review repository alias methods and keep one naming style:
  - `observeProfiles` versus `observeAllProfiles`
  - `observeProfile` versus `observeProfileById`
  - `createProfile` versus `addProfile`
  - `deleteProfile` versus `deleteProfileById`
- Do not remove nullable/future chair-stand analysis fields yet. They are part of the Room schema and are likely placeholders for future camera/pose work.
- Keep backup/data-extraction exclusions for local app data.

### State Management

- Consolidate repeated UI state fields where it stays simple:
  - `isLoading`
  - `errorMessage`
  - `message`
  - `isSaving`
  - `shouldChooseProfile`
- Do not introduce a heavy generic state framework. Small local data classes are still appropriate for a Compose MVP.
- Move repeated "selected profile missing" handling into a shared pattern or helper.
- Reduce private ViewModel formatting if it is display-only and repeated across screens.
- Consider centralizing `ViewModelProvider.Factory` creation patterns only if the duplication remains obvious after other cleanup. Avoid dependency injection churn for this MVP.

### Business Logic

- Keep `ChairStandRecommendationRules` as the source of truth for recommendation level.
- Keep `HistorySummaryCalculator` as the source of truth for summary metrics.
- Centralize UI mapping for recommendation/status levels:
  - level key
  - user-facing label
  - status pill color intent
  - result feedback title
  - result feedback body
  - guidance text
- Move duration and confidence formatting to a small formatter utility if reused:
  - duration minutes text
  - confidence percentage text
  - date display text
- Avoid duplicating recommendation generation rules in screens or repositories.

### Tests and QA

- Keep current unit tests for:
  - recommendation rules
  - recommendation templates
  - recommendation generation
  - history summary
  - mock chair-stand analyzer
- Add Room-backed repository or DAO tests for user separation:
  - history only returns selected user's results
  - recommendations only return selected user's recommendations
  - completion update cannot mark another user's recommendation complete
  - result report cannot cross user/session boundaries
- Add tests for destructive/local data operations:
  - delete current profile removes that profile's sessions/results/recommendations only
  - delete all local data clears all profiles, results, recommendations, and selected profile
  - export selected profile data contains only selected user's records
- Add tests for first-launch and selected-profile behavior if Compose UI tests are kept.

## 3. Risky Changes to Avoid

- Do not redesign the architecture or add a new dependency injection framework during this cleanup.
- Do not add login, signup, Firebase, backend APIs, cloud sync, analytics, or Internet permission.
- Do not add CameraX, MediaPipe, or real camera detection yet.
- Do not remove safety disclaimers or soften them so much that product safety becomes unclear.
- Do not remove the "not a medical diagnosis" language.
- Do not remove local data backup exclusions without a product decision.
- Do not remove Room fields only because they are currently null; schema changes need migration planning.
- Do not replace user-scoped data access with session-only or result-only access.
- Do not change navigation behavior for the core flow:
  - onboarding
  - add/select profile
  - home
  - safety setup
  - 30-second chair-stand demo
  - manual `+1 Stand` count
  - save result
  - result
  - recommendations
  - history
  - settings/export/delete
- Do not make destructive actions visually or behaviorally easier to trigger.
- Do not change recommendation thresholds, generated exercise logic, confidence calculation, or history summary math without explicit product approval.
- Do not remove the mock analyzer placeholders until the real camera/pose implementation is ready.

## 4. Proposed Refactoring Steps in Priority Order

1. Add regression tests for data isolation and business rules before touching data access.
   - Focus on selected-user history, recommendations, completion updates, delete current profile, delete all data, and export scoping.

2. Centralize user-facing copy and recommendation/status UI mapping.
   - Create a small `UiCopy` or `SteplyCopy` object.
   - Create a status/recommendation UI mapper for labels, tones, and feedback text.
   - Replace duplicated disclaimer, safety, status, and feedback strings.

3. Split shared UI components into focused files without changing public component behavior.
   - Keep names stable where screens already use them.
   - Move tokens, buttons, cards, notices, metrics, forms, and scaffolding into separate files.

4. Remove unused UI component aliases.
   - Delete `SteplyHeroCard`, `PrimaryActionButton`, and `NoticeBox` only after confirming no references.

5. Consolidate shared design tokens.
   - Move repeated padding, corner radius, button height, spacing, and common colors into a token file.
   - Leave screen-specific dimensions local.

6. Clean up unused use cases and repository aliases.
   - Remove unused use cases if no screen or container needs them.
   - Standardize repository method names.
   - Keep changes mechanical and covered by compile/test runs.

7. Tighten DAO/repository user scoping.
   - Remove or hide session-only/id-only recommendation methods that are not used by UI flows.
   - Keep explicitly user-scoped methods as the default path.

8. Simplify navigation constants.
   - Remove unused route helpers only after verifying there are no call sites and no planned deep-link need.

9. Review test dependencies.
   - Either add a minimal Compose smoke test or remove unused Android UI test dependencies in a dependency cleanup.

10. Run final QA.
   - Search for Korean characters in source.
   - Search for forbidden dependencies/permissions.
   - Run unit tests.
   - Build the debug APK.
   - Manually walk the core flow.

## 5. Files Likely to Be Changed

### UI Components and Theme

- `app/src/main/java/com/steply/app/ui/components/SteplyComponents.kt`
- `app/src/main/java/com/steply/app/ui/theme/Theme.kt`
- New component split files under `app/src/main/java/com/steply/app/ui/components/`

### Screens

- `app/src/main/java/com/steply/app/ui/screens/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/profile/ProfileListScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/profile/AddEditProfileScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/home/HomeScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/safety/SafetyScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/safety/SafetySetupScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/chaircheck/ChairCheckScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/chaircheck/ChairStandCheckScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/result/ResultScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/recommendation/RecommendationScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/history/HistoryScreen.kt`
- `app/src/main/java/com/steply/app/ui/screens/settings/SettingsScreen.kt`

### Navigation

- `app/src/main/java/com/steply/app/ui/navigation/Routes.kt`
- `app/src/main/java/com/steply/app/ui/navigation/SteplyApp.kt`

### Domain and Business Logic

- `app/src/main/java/com/steply/app/domain/usecase/ChairStandRecommendationRules.kt`
- `app/src/main/java/com/steply/app/domain/usecase/ExerciseRecommendationTemplates.kt`
- `app/src/main/java/com/steply/app/domain/usecase/GenerateExerciseRecommendationsUseCase.kt`
- `app/src/main/java/com/steply/app/domain/usecase/HistorySummaryCalculator.kt`
- Potential new file: `app/src/main/java/com/steply/app/domain/model/RecommendationLevelUi.kt` or UI-layer equivalent

### Data Layer

- `app/src/main/java/com/steply/app/data/local/dao/ExerciseRecommendationDao.kt`
- `app/src/main/java/com/steply/app/data/local/dao/ScreeningDao.kt`
- `app/src/main/java/com/steply/app/data/repository/ExerciseRecommendationRepository.kt`
- `app/src/main/java/com/steply/app/data/repository/ScreeningRepository.kt`
- `app/src/main/java/com/steply/app/data/repository/UserProfileRepository.kt`
- `app/src/main/java/com/steply/app/di/AppContainer.kt`

### Utilities and Copy

- `app/src/main/java/com/steply/app/util/DateFormatUtil.kt`
- Potential new file: `app/src/main/java/com/steply/app/ui/text/SteplyCopy.kt`
- Potential new file: `app/src/main/java/com/steply/app/ui/text/SteplyFormatters.kt`

### Tests

- `app/src/test/java/com/steply/app/domain/usecase/ChairStandRecommendationRulesTest.kt`
- `app/src/test/java/com/steply/app/domain/usecase/ExerciseRecommendationTemplatesTest.kt`
- `app/src/test/java/com/steply/app/domain/usecase/GenerateExerciseRecommendationsUseCaseTest.kt`
- `app/src/test/java/com/steply/app/domain/usecase/HistorySummaryCalculatorTest.kt`
- New tests under `app/src/test/java/com/steply/app/data/`
- Optional UI smoke tests under `app/src/androidTest/` if Android UI test dependencies are kept

### Build Files

- `app/build.gradle.kts`
- `build.gradle.kts`
- `app/src/main/AndroidManifest.xml`

## 6. Acceptance Criteria for the Refactor

- Product behavior is unchanged.
- First launch, onboarding, profile creation, profile selection, home, safety setup, chair-stand demo, result, recommendations, history, export, delete current profile data, and delete all local data still work.
- No login, signup, Firebase, backend, cloud sync, analytics, Internet permission, CameraX, or MediaPipe implementation is added.
- Room SQLite and DataStore remain the only persistence mechanisms.
- One physical device still supports multiple local profiles.
- Results, history, recommendations, completion state, export, and deletion remain scoped to the selected `userId`.
- Saving chair-stand session and result remains atomic.
- No raw camera video is stored.
- Required safety copy remains visible:
  - "Steply is not a medical diagnosis tool."
  - "Stop if you feel pain, dizziness, or discomfort."
  - "Use support or ask a caregiver to help if needed."
- All authored visible UI text remains English.
- Source search for Korean Hangul in `app/src/main` and `app/src/test` returns no app-authored UI text.
- Destructive actions remain visually separated and confirmation-protected.
- Shared UI components preserve the warm, premium, older-adult-friendly design language.
- Unused code removal does not remove intentional MVP placeholders for future real movement analysis.
- Unit tests pass.
- Debug build succeeds.
- Any new tests for user isolation, delete/export behavior, and business rules pass.
