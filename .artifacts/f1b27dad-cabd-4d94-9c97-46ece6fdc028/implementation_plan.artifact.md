# Fix Render Issue in EventDetailScreen Preview

The Compose Preview for `EventDetailScreen` is failing with `java.lang.NoSuchMethodException`. This is commonly caused by the `@Preview` function being marked as `private`, which prevents the Android Studio preview tool from accessing it via reflection.

## Proposed Changes

### [app](file:///C:/Users/tebog/Documents/School/mobile application/Group assignment/CampusPulse.apk/CampusPulseApp/app)

#### [MODIFY] [EventDetailScreen.kt](file:///C:/Users/tebog/Documents/School/mobile application/Group assignment/CampusPulse.apk/CampusPulseApp/app/src/main/java/com/campuspulse/app/ui/screens/EventDetailScreen.kt)

- Change the visibility of `EventDetailScreenPreview` from `private` to default (public) to ensure it can be found by the Compose Preview tool.

## Verification Plan

### Manual Verification
- Render the `EventDetailScreenPreview` using the `render_compose_preview` tool to confirm the fix.
