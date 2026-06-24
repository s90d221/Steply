# Steply

Steply is a local-first Android MVP for older adults to check simple movement patterns and receive gentle exercise recommendations.

This MVP is not a medical diagnosis tool. It helps users understand movement status and practice safely with friendly guidance.

## MVP Description

The first version focuses on a 30-second Chair Stand Check. It uses a demo/manual flow: the app runs a timer, and the user taps `+1 Stand` after each full chair stand repetition.

Real camera-based pose detection is not implemented yet. The current analyzer boundary is designed so a future implementation can replace the mock/manual analyzer with CameraX and MediaPipe Pose Landmarker.

## Local-First Privacy Model

- No signup
- No login
- No Firebase
- No backend API
- No cloud sync
- No `INTERNET` permission
- Multiple local profiles can share one device
- Profile, screening, result, and recommendation data are stored locally with Room SQLite
- App-level settings, including selected profile, are stored with DataStore Preferences
- Raw camera video is not stored in the MVP

If the app is deleted or the device is lost, local records cannot be restored unless a future backup/import feature is added.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- MVVM-friendly ViewModels and repositories
- Room SQLite
- DataStore Preferences
- Coroutines and Flow
- JUnit for JVM unit tests

## Main Features

- English UI text designed for readability
- Onboarding with local-only and medical disclaimer notices
- Multiple local user profiles on one device
- Selected-user home screen
- Safety setup screen before movement checks
- Mock/manual 30-second Chair Stand Check
- Local result report with gentle feedback
- Local exercise recommendation generation
- Recommendation completion tracking
- Selected-user history and summary metrics
- Selected-user JSON export through Android share intent
- Local data deletion controls

## Data Model Summary

Room stores four main entity types:

- `UserProfileEntity`: local profile details such as display name, birth year, height, and notes
- `ScreeningSessionEntity`: one movement check session tied to a `userId`
- `ChairStandResultEntity`: structured chair stand result tied to both `sessionId` and `userId`
- `ExerciseRecommendationEntity`: generated exercise suggestions tied to a `userId` and optionally a `sessionId`

Result queries are filtered by the selected `userId` so records are not mixed between local profiles.

## How To Run

1. Install Android Studio with JDK 17 support.
2. Open this repository in Android Studio.
3. Sync Gradle.
4. Run the `app` configuration on an emulator or Android device.

Command-line build:

```bash
./gradlew :app:assembleDebug
```

Command-line unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

## Manual QA Checklist

- [ ] First launch shows onboarding
- [ ] User can create profile
- [ ] User can create multiple profiles
- [ ] User can switch profile
- [ ] Chair Stand Check saves result
- [ ] History shows selected user only
- [ ] Recommendation appears after result
- [ ] Recommendation completion persists
- [ ] Export selected user data
- [ ] Delete selected user data
- [ ] Delete all local data
- [ ] App works after restart
- [ ] No login/signup
- [ ] No Internet permission

## Current Limitations

- Chair Stand Check is mock/manual only
- CameraX preview is not implemented
- MediaPipe Pose Landmarker is not implemented
- Real automatic repetition counting is not implemented
- Trunk lean, symmetry, and stability metrics are placeholders
- No cloud backup or sync
- No import/restore flow
- Not intended for medical diagnosis

## Future TODO

- Add CameraX preview
- Add MediaPipe Pose Landmarker
- Add full-body visibility check
- Add real repetition counting
- Add trunk lean / symmetry / stability metrics
- Add Big Screen Mode
- Add staff mode for senior centers
- Add weekly report
- Add balance challenge
- Add export/import backup if needed

## Development Notes

Keep the MVP local-first. Do not add authentication, backend clients, cloud sync, Firebase, network permissions, or raw video storage unless the product requirements explicitly change.
