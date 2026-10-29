# PDF Tools

One native Kotlin / Jetpack Compose app for Android 8.0+ (API 26), target API 35. Version 0.3.0.

## Build

Use JDK 17 and Android SDK 35. Configure local.properties or ANDROID_HOME.

    .\gradlew.bat :app:assembleDebug
    .\gradlew.bat :app:testDebugUnitTest :app:lintDebug

Phone APK: app/build/outputs/apk/debug/app-arm64-v8a-debug.apk. Install the APK for your device architecture. Release builds retain code/resource shrinking; publishing requires separate release signing.

The app is named PDF Tools. Its application ID stays com.example.pdftools.review to preserve the latest installation's history and permissions. There is no separate review build or sample activity. Debug and release use the same app identity. The source namespace is com.example.pdftools.

Local development signing uses .gradle/development.keystore when present; otherwise Android's default debug key. Keep the same signing key for future updates. Private keys and machine-specific settings are excluded from archives and version control.

## Behavior

Merge, split, compress, OCR, PDF to Image and Image to PDF run locally. Split saves one PDF or separate PDFs listed individually in the result sheet. PDF to Image exports a ZIP of PNG/JPG images. Compression can flatten searchable text and interactive elements. OCR uses a bundled Latin-script model.

Originals are preserved. New outputs receive unique filenames in Documents/PDF Tools. The user grants All files access through Android Settings when entering a tool or library. The app does not request internet access.

Results open from a compact bottom sheet; returning after viewing a result goes to Home. The shared viewer supports page swipes, pinch/double-tap zoom and fullscreen. Settings has History, Privacy policy, Share app, Review app and version. App sharing opens Android's chooser with the installed APK. Review app explains availability until a real store listing exists. Home Back shows the approved compact Stay/Exit dialog.

## Source layout

- MainActivity.kt: navigation, permission requests, sharing and exit handling.
- HomeScreens.kt: splash, onboarding, home and file library.
- ToolScreen.kt: tool configuration and selected files.
- ResultSheet.kt: shared result sheet and output rows.
- PdfViewerScreen.kt: rendering, paging, zoom and fullscreen.
- SettingsScreens.kt: settings and the local privacy policy.
- DesignComponents.kt, AppIcons.kt, Theme.kt: shared visual components.
- AppViewModel.kt, DocumentStore.kt: state, scanning and history.
- PdfEngine.kt, Models.kt: processing and validation.

JVM tests cover page ranges and filenames. The Android integration test checks merge order, extraction and separate PDF outputs in isolated cache storage without broad file permission. Run with :app:connectedDebugAndroidTest. See VERIFICATION.md for checks actually run, and THIRD_PARTY_NOTICES.md for library notices.
