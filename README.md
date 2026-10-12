# Campus Pulse — Android (Kotlin + Jetpack Compose)

A Jetpack Compose implementation of the Campus Pulse walkthrough: all 10 screens,
wired together with Navigation-Compose, using Material3 and the navy/orange brand
palette from the mockup.

## Opening the project

1. Unzip this folder.
2. Open **Android Studio** (Koala/2024.1 or newer recommended) → **Open** → select the
   `CampusPulseApp` folder.
3. Android Studio doesn't find a Gradle wrapper jar in this export — when it asks,
   let it **regenerate the Gradle wrapper**, or run `gradle wrapper` once if you have
   Gradle installed locally. After that, `./gradlew` will work normally.
4. Let Gradle sync (it will download AGP 8.5, Kotlin 2.0, Compose BOM 2024.06, and
   Navigation-Compose — first sync needs an internet connection).
5. Run the `app` configuration on an emulator or device (minSdk 24).

## What's included

- **Screens 1–10** from the mockup, each as its own composable in `ui/screens/`:
  Verify → Interests → Home feed → Directory → Club account → Event detail →
  Profile → Club admin → New event (bottom sheet) → Super admin.
- **Navigation** wired with `navigation-compose` in `navigation/CampusPulseNavGraph.kt`.
- **Theme** (`ui/theme/`) matches the brand colors from your screenshots: dark navy
  (`#151435`) and orange accent (`#F2A93B`), with the app content screens kept light
  (white/cream) as in the original mockup. Category colors (Sport, Culture, Advocacy,
  Identity, Academic) are carried over too.
- **Sample data** in `data/SampleData.kt` — swap this for real API/repository calls
  when you're ready to connect a backend.

## Next steps you'll likely want

- Replace `SampleData` with a real data layer (Retrofit/Room, or your backend of choice).
- Swap the system font for Archivo/Inter: drop the `.ttf` files into `res/font/` and
  point `DisplayFamily`/`BodyFamily` in `ui/theme/Type.kt` at them.
- Add real launcher icon artwork (a placeholder adaptive icon is included).
- Hook up authentication for the Verify screen instead of navigating straight through.
