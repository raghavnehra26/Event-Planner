# FocusForge

FocusForge is an Android productivity app built with Kotlin, Jetpack Compose, Room, DataStore, Hilt, WorkManager, and Android services. The included debug build supports habit check-ins, streaks, a Pomodoro timer, analytics, settings, local data export, and a Pro demo flow.

This folder was prepared from the supplied `FocusForge.zip`. Machine-specific IDE/Gradle caches and `local.properties` were left out so Android Studio can configure the local SDK on this computer. The app source, Gradle wrapper, unit test, and supplied Viva/Play Store reference files are included.

## Open and run in Android Studio

1. Open Android Studio.
2. Select **Open** and choose this folder: `C:\Users\ragha\Documents\FinClarity\FocusForge_Ready`.
3. If prompted, trust the project and select the Android SDK location. The SDK installed on this computer is `C:\Users\ragha\AppData\Local\Android\Sdk`.
4. Let Gradle sync finish. In **Settings → Build, Execution, Deployment → Build Tools → Gradle**, select Android Studio's embedded JDK (JDK 17 or newer; this project was built with JDK 21).
5. Open **Tools → Device Manager**, start the `Pixel_9` virtual device if it is listed, then select it in the device menu.
6. Select the `app` run configuration and press **Run ▶**.

The first sync may download Gradle and dependencies, so allow internet access and wait for it to finish. If Android Studio asks for an SDK platform, install Android SDK Platform 34 and Android SDK Build Tools 34.0.0.

## Build and test from PowerShell

From this project folder:

```powershell
$env:JAVA_HOME = "$env:ProgramFiles\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

The checked debug APK is included at `outputs\FocusForge-debug.apk`. A fresh build writes it to:

```text
app\build\outputs\apk\debug\app-debug.apk
```

To install it on a connected emulator or Android device:

```powershell
.\gradlew.bat :app\installDebug
```

The debug application ID is `com.focusforge.app.debug`. To start it from an Android SDK command prompt after installation:

```powershell
& "$env:ANDROID_HOME\platform-tools\adb.exe" shell monkey -p com.focusforge.app.debug 1
```

## What is implemented

- First-launch onboarding and saved user preferences.
- Persistent habit creation, completion check-ins, streak calculations, and a three-active-habit free tier.
- Focus, short-break, long-break, and deep-work timer modes.
- A foreground timer service with an ongoing notification; completed focus sessions are written by the service so switching away from the timer screen does not discard a finished session.
- Analytics and a 7-day free / 365-day Pro heatmap.
- Settings for timer intervals and sound/vibration preferences.
- CSV/JSON export using Android's share sheet (Pro-gated in the current app).
- Optional reminder notifications, AdMob test placements, and Play Billing integration points.

## Demo billing and ads

The debug build may simulate a Pro purchase when Play Billing products are not configured. The paywall labels this as a demo; it does not charge money. Release builds do not use the simulated-purchase fallback. Restore purchases requires a working Google Play Billing connection.

AdMob IDs in the source are Google's sample test IDs. A reward is granted only after a rewarded ad reports that the user earned it. If no ad is available, the app offers a retry and does not unlock the heatmap. Configure real AdMob IDs and Play Console product IDs before publishing; none are configured by this project.

## Project structure

```text
FocusForge_Ready/
├── app/                     Android app module and unit tests
├── gradle/wrapper/           Pinned Gradle 8.7 wrapper
├── gradlew.bat               Windows Gradle entry point
├── build.gradle.kts          Android and Kotlin plugin versions
├── settings.gradle.kts       Repositories and included modules
├── PLAY_STORE_DEPLOYMENT.md  Release setup checklist
└── FocusForge_Viva_Guide.*   Supplied project presentation references
```

## Verification performed

On this machine, `:app:testDebugUnitTest` and `:app:assembleDebug` completed successfully (3 unit tests passed). The resulting debug APK was installed on the configured `Pixel_9` emulator and launched without a startup crash. The build emitted two Kotlin deprecation warnings and an Android SDK XML version warning; they did not prevent the build. The core screens were not exhaustively manually exercised.

## Important release notes

- The debug app uses the package suffix `.debug`; the release package ID is `com.focusforge.app`.
- A release signing key and Play Console products are not included. The deployment guide is a checklist, not evidence that a Play Store release is ready.
- Local build artifacts, machine-specific SDK paths, credentials, and emulator data should not be committed.
