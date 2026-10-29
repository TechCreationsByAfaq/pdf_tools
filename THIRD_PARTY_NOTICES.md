# Third-party notices

This project uses third-party software under its own licenses; it is not a claim that the app is copyright-free or that dependencies are public domain.

- **AndroidX, Jetpack Compose and Material Components** — Apache License 2.0. https://android.googlesource.com/platform/frameworks/support/+/androidx-main/LICENSE.txt
- **Kotlin and kotlinx.coroutines** — Apache License 2.0. https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt ; https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt
- **PDFBox Android** — Apache License 2.0, based on Apache PDFBox; retain upstream dependency notices when distributing. https://github.com/TomRoush/PdfBox-Android/blob/master/LICENSE ; https://pdfbox.apache.org/
- **Google ML Kit Text Recognition** — Google SDK terms and applicable model terms, not an Apache/public-domain assumption. https://developers.google.com/ml-kit/terms
- **JUnit** (test only) — Eclipse Public License 1.0. https://github.com/junit-team/junit4/blob/main/LICENSE-junit.txt
- **Gradle wrapper/build tooling** — Apache License 2.0. https://github.com/gradle/gradle/blob/master/LICENSE

Review transitive dependencies and include the relevant license/NOTICE files in a release distribution. Version 0.2.0 uses a small original vector icon set in `AppIcons.kt`; it does not package Material's extended icon collection. Design reference PNGs are development references only and are not bundled into the APK.
