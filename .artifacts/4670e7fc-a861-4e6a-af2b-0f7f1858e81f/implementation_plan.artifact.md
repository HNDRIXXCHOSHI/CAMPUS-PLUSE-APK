# Implementation Plan - Add Preview on All Screens

Add `@Preview` composable functions to the screens that currently lack them, ensuring all 16 screen composables have proper preview support in Android Studio.

## Proposed Changes

We identified that 13 out of 16 screen files already contain preview implementations. The following 3 files lack a preview function and will be updated:

### `:app` Screen Previews

#### [MODIFY] [NotificationsScreen.kt](file:///C:/Users/tebog/Downloads/CampusPulseApp-v2-UMP-PitchReady/CampusPulseApp/app/src/main/java/com/campuspulse/app/ui/screens/NotificationsScreen.kt)
- Add a private `@Preview` composable function `NotificationsScreenPreview` wrapped in `CampusPulseTheme`.

#### [MODIFY] [CreateClubScreen.kt](file:///C:/Users/tebog/Downloads/CampusPulseApp-v2-UMP-PitchReady/CampusPulseApp/app/src/main/java/com/campuspulse/app/ui/screens/CreateClubScreen.kt)
- Add a private `@Preview` composable function `CreateClubScreenPreview` providing stub lambda arguments and wrapped in `CampusPulseTheme`.

#### [MODIFY] [ClubAdminScreen.kt](file:///C:/Users/tebog/Downloads/CampusPulseApp-v2-UMP-PitchReady/CampusPulseApp/app/src/main/java/com/campuspulse/app/ui/screens/ClubAdminScreen.kt)
- Add a private `@Preview` composable function `ClubAdminScreenPreview` providing a sample `Club` model instance and stub lambda arguments wrapped in `CampusPulseTheme`.

---

## Verification Plan

### Automated Tests
- Run Gradle assemble task to ensure there are no compilation errors:
  ```powershell
  ./gradlew :app:assembleDebug
  ```

### Manual Verification
- Render the previews for all three screens via `render_compose_preview` to ensure they compile and render correctly.
