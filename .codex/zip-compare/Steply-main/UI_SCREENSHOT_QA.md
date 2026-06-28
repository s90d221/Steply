# Steply UI Screenshot QA

Date: 2026-06-25

## Device Setup

- Runtime: Android emulator through adb, `emulator-5554`
- Package: `com.steply.app`
- Main target: 360dp portrait via `wm size 1080x2400`, `wm density 480`
- Additional targets captured:
  - `390dp`: `screenshots/iteration2/390_today.png`, `screenshots/iteration2/390_check.png`
  - `430dp`: `screenshots/iteration2/430_today.png`, `screenshots/iteration2/430_check.png`
  - Font scale 1.3: `screenshots/iteration2/font13_today.png`, `screenshots/iteration2/font13_safety.png`
  - Landscape practical pass: `screenshots/iteration2/landscape_today.png`, `screenshots/iteration2/landscape_check.png`

## Iteration Summary

### Iteration 1

Captured the full app flow in `screenshots/iteration1/`.

Main findings:
- Top bars sat too close to the status bar.
- Onboarding and Add Profile primary actions were too low.
- Today, Safety, Challenge Setup, and Active Challenge pushed primary actions below the first viewport.
- Active Challenge hid the stand counter controls and stop/finish actions.
- Result actions appeared too late in the report.
- Recommendations showed notices before exercises.
- Settings pushed the first useful actions under the bottom bar.
- Landscape bottom navigation consumed too much height.

Fixes applied:
- Added status bar padding and reduced top bar typography/spacing.
- Moved Onboarding and Add Profile primary CTAs higher.
- Added bottom scroll padding.
- Compact Safety, Challenge Setup, Active Challenge, Result, Recommendations, Settings, and Today layouts.

### Iteration 2

Captured updated screens in `screenshots/iteration2/`, inspected them visually, and fixed remaining issues:
- Safety: removed duplicated top notice so checklist, confirmation, and Start Check fit.
- Challenge Setup: shortened title, compacted body setup placeholder, moved repeated checklist into a compact ready note.
- Active Challenge: combined instruction and camera placeholder, reduced metric card height, kept +1 Stand, Finish, and Stop Check visible.
- Result: moved main actions directly after summary.
- Recommendations: moved exercises above local/safety notices.
- Settings: compacted Local Data copy.
- Font scale 1.3: shortened Today copy and moved secondary challenge action below the primary card.
- Landscape: added compact icon-only bottom navigation for short-height screens.

## Screen Report

| Screen | Screenshot path | Issues found | Fixes applied | Remaining concerns |
|---|---|---|---|---|
| Onboarding | `screenshots/iteration2/360_onboarding.png` | Iteration 1 CTA was below fold. | Moved Get Started above informational cards and reduced hero spacing. | None. |
| Profile list empty | `screenshots/iteration2/360_profile_list_empty.png` | Iteration 1 top bar was oversized. | Top bar spacing and typography reduced. | None. |
| Add profile | `screenshots/iteration2/360_add_profile.png` | Save Profile required scrolling. | Moved Save Profile before optional details. | Optional details remain scroll-only by design. |
| Profile list with profiles | `screenshots/iteration2/360_profile_list_with_profiles.png` | Profile actions were usable but needed clearer spacing. | Top bar/card spacing updates carried through. | None. |
| Today/Home | `screenshots/iteration2/360_today_empty.png` | Primary card was cramped and font scale 1.3 clipped secondary action. | Stacked status chip, shortened copy, moved secondary challenge action to its own card. | Secondary card starts below fold at font scale 1.3, but primary action remains visible. |
| Check / challenge selection | `screenshots/iteration2/360_check_selection.png` | Iteration 1 status/CTA placement was tight. | Top bar and spacing fixes; challenge cards remain distinct by color/icon/copy. | Additional challenge cards require scroll, expected. |
| Safety setup | `screenshots/iteration2/360_safety_setup.png` | Start Check was below/under navigation. | Removed duplicated safety note; kept checklist, confirmation, and disclaimer. | Medical disclaimer begins below first viewport after primary action, expected. |
| Challenge setup | `screenshots/iteration2/360_challenge_setup.png` | Begin action was below fold. | Short title, compact body-view placeholder, compact ready note. | None. |
| Chair check prepare | `screenshots/iteration2/360_chair_check_prepare.png` | Prepare screen was readable but tall. | Top bar and spacing fixes. | None. |
| Active challenge | `screenshots/iteration2/360_active_challenge.png` | +1 Stand, Finish, and Stop Check were hidden. | Compact active layout and thinner timer ring. | None. |
| Result screen | `screenshots/iteration2/360_result_top.png` | Main actions were too late and bottom CTA was hidden. | Moved Recommended Exercises, History, and Today actions directly below summary. | Result score is `0` in this QA data because scripted taps finished after timer save. |
| Recommended exercises | `screenshots/iteration2/360_recommended_exercises.png` | Exercise list started too low. | Short title and moved exercises above notices. | None. |
| History empty | `screenshots/iteration2/360_history_empty.png` | Empty state needed final verification after data clear. | Captured with a fresh local profile and no results. | None. |
| History with results | `screenshots/iteration2/360_history_with_results.png`, `screenshots/iteration2/360_history_timeline_bottom.png` | Timeline actions could sit at bottom edge in intermediate scroll positions. | Extra bottom padding and verified further scrolling exposes actions cleanly. | Multiple saved QA results appear because test runs were repeated. |
| Settings | `screenshots/iteration2/360_settings.png`, `screenshots/iteration2/360_settings_lower.png` | Change Profile and lower destructive actions were clipped. | Compacted Local Data section; lower sections verified by scroll. | Delete All remains lower in destructive section by design. |
| Delete confirmation dialog | `screenshots/iteration2/360_delete_confirmation_dialog.png` | Needed destructive confirmation check. | Dialog clearly separates Cancel and Delete Profile Data. | None. |
| Export state | `screenshots/iteration2/360_export_state.png` | Emulator had no compatible share target. | Verified export action reaches Android export flow without adding permissions/backend. | Export depends on installed share targets on the device. |
| 390dp target | `screenshots/iteration2/390_today.png`, `screenshots/iteration2/390_check.png` | Needed responsive verification. | Final layouts captured. | None. |
| 430dp target | `screenshots/iteration2/430_today.png`, `screenshots/iteration2/430_check.png` | Needed responsive verification. | Final layouts captured. | None. |
| Font scale 1.3 | `screenshots/iteration2/font13_today.png`, `screenshots/iteration2/font13_safety.png` | Today secondary action clipped in first pass. | Shortened Today card and moved secondary challenge action below primary card. | Primary CTAs are visible; secondary content may require scroll. |
| Landscape | `screenshots/iteration2/landscape_today.png`, `screenshots/iteration2/landscape_check.png` | Bottom nav covered CTA in landscape. | Added compact icon-only bottom navigation for short-height layouts. | Landscape remains dense but primary Check CTA is visible. |

## Verification

- `./gradlew :app:assembleDebug` passed.
- `./gradlew :app:testDebugUnitTest` passed.
- No backend, login, signup, Firebase, cloud sync, Internet permission, or MediaPipe implementation was added.
- Local-first behavior and multi-profile behavior were preserved.
