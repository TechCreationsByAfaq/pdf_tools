# Verification — 0.3.0, 30 October 2026

## Consolidation

- One installed app, PDF Tools, application ID com.example.pdftools.review, versionCode 2.
- The existing latest app was updated in place using the same development signing key. Its history, preferences and file permission remain attached to that installation.
- The older com.example.pdftools app and instrumentation test package were uninstalled for user 0 with -k, retaining their data. Both apps' library and experience preferences were also backed up under work/app-data-backup-before-consolidation outside the source archive.
- Removed the preview build, design-review activity and manifest, sample screens, unused UI tooling dependencies and unused history-clearing methods.
- Separated tool configuration, result sheet and PDF viewer into dedicated source files. Reused a focused result-file row component and removed obsolete imports and dead labels.

## Checks actually run

- assembleDebug passed using a smaller JVM after the combined build ran out of host memory. Studio stayed open.
- testDebugUnitTest: four tests, zero failures, zero errors.
- lintDebug: zero errors, 18 warnings. Warnings include dependency upgrades, library certificate helpers, backup-rule guidance and Kotlin extension suggestions. The app does not request internet access; dependencies were not upgraded in this cleanup.
- On-device PdfEngineIntegrationTest passed: one test checking merge order, extraction and separate PDF files in isolated app cache storage.
- Updated app installed successfully. MainActivity launched, version 0.3.0 verified, and Home visually checked.
- Only com.example.pdftools.review remains installed among the former app/test packages.

The delivered ARM64 debug APK is 33,022,074 bytes (33.0 MB). It is development-signed and not a production Play Store release. Older optimized APK size measurements apply to older builds, not this artifact. Full release optimization and publishing are separate steps.

## Preserved behavior

All six tools, approved compact layouts, shared result sheet, return to Home, individual split PDFs, fullscreen/swiping viewer, privacy policy, settings and approved exit dialog remain implemented. This cleanup did not repeat every end-to-end user flow or test every document type.

Source archives exclude signing keys, caches, local machine settings, crash logs and private app-data backups.
