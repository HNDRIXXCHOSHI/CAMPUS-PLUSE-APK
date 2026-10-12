# Fix Unresolved Reference 'EventCategory' in EventDetailScreen.kt

The build error `Unresolved reference 'EventCategory'` occurs because the `EventCategory` model does not exist in the `com.campuspulse.app.model` package. Instead, the project uses a `Category` enum defined in `Models.kt`. Additionally, the `EventItem` instantiation in the preview is missing required fields.

## Proposed Changes

### UI Layer

#### [MODIFY] [EventDetailScreen.kt](file:///C:/Users/tebog/Documents/School/mobile%20application/Group%20assignment/CampusPulse.apk/CampusPulseApp/app/src/main/java/com/campuspulse/app/ui/screens/EventDetailScreen.kt)

- Replace `import com.campuspulse.app.model.EventCategory` with `import com.campuspulse.app.model.Category`.
- Update `EventDetailScreenPreview` to use `Category.CULTURE` instead of the non-existent `EventCategory` class.
- Add missing `rating` and `going` fields to the `EventItem` instantiation in the preview to prevent subsequent build errors.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference error is resolved and the code compiles successfully.

### Manual Verification
- Use `render_compose_preview` to verify that `EventDetailScreenPreview` renders correctly in the IDE.
