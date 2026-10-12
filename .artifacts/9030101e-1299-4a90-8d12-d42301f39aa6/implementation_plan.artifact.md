# Implementation Plan - HomeFeedScreen UI Adjustments

The user wants to remove the bottom navigation bar from the Home Feed screen, add the month to the days of the week selector, and adjust the width of post cards to be closer to the screen edges.

## User Review Required

> [!IMPORTANT]
> The `CampusPulseBottomNav` component is currently **not used** in the `HomeFeedScreen.kt` code, although it is imported. I will remove the import and the unused `onNavigate` parameter to "remove" it from the screen's logic. If you intended to see a visible bottom bar that is currently hidden by some other means, please clarify.

> [!NOTE]
> The project currently has a compilation error in `CampusPulseNavGraph.kt` because a required parameter `onNavigateToClubHome` is not being passed to `HomeFeedScreen`. I will fix this as part of the changes.

## Proposed Changes

### [HomeFeedScreen UI](file:///C:/Users/tebog/Documents/School/mobile application/Group assignment/CampusPulse.apk/CampusPulseApp/app/src/main/java/com/campuspulse/app/ui/screens/HomeFeedScreen.kt)

#### [MODIFY] [HomeFeedScreen.kt](file:///C:/Users/tebog/Documents/School/mobile application/Group assignment/CampusPulse.apk/CampusPulseApp/app/src/main/java/com/campuspulse/app/ui/screens/HomeFeedScreen.kt)

- **Remove Bottom Nav Logic**:
    - Remove `import com.campuspulse.app.ui.components.CampusPulseBottomNav`.
    - Remove `onNavigate: (NavDestination) -> Unit` from `HomeFeedScreen` parameters.
- **Add Month to Week Days**:
    - Modify the `weekDays` selector to display the month (e.g., "Sep") alongside the day and date.
- **Adjust Card Width**:
    - Reduce horizontal padding from `20.dp` to `8.dp` in the `LazyColumn` items containing `EventCard`.

### [Navigation](file:///C:/Users/tebog/Documents/School/mobile application/Group assignment/CampusPulse.apk/CampusPulseApp/app/src/main/java/com/campuspulse/app/navigation/CampusPulseNavGraph.kt)

#### [MODIFY] [CampusPulseNavGraph.kt](file:///C:/Users/tebog/Documents/School/mobile application/Group assignment/CampusPulse.apk/CampusPulseApp/app/src/main/java/com/campuspulse/app/navigation/CampusPulseNavGraph.kt)

- **Fix Compilation & Cleanup**:
    - Provide an implementation for `onNavigateToClubHome` in the `HomeFeedScreen` call (navigating to the club account route).
    - Remove the `onNavigate` argument since it will be removed from the `HomeFeedScreen` signature.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to ensure the project compiles successfully.

### Manual Verification
- Verify the UI changes in the Compose Preview `HomeFeedScreenPreview`.
- Confirm the cards are wider and the month is visible in the day selector.
