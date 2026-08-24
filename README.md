# Quran

A minimal, lightweight Android Quran reader with Arabic text and Indonesian
translation bundled for offline reading.

## Features

- Offline reading for all 114 surahs
- Optional Madani-style Mushaf flow mode with Arabic-Indic ayah markers
- Quick navigation by surah, ayah, Madani page, and juz
- Uthmani Arabic text with bundled Amiri font
- Indonesian translation toggle
- Arabic and Indonesian ayah search
- Bookmarks and last-read position
- Adjustable Arabic text size
- Dark mode

The app uses package `org.opennur.quran`.

## Build

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew detekt
./gradlew assembleDebug
```

The generated APK is under `app/build/outputs/apk/debug/`.

Android smoke tests are included under `app/src/androidTest/` and compile with:

```bash
./gradlew compileDebugAndroidTestKotlin
```

## Release CI/CD

GitHub Actions runs unit tests, Android-test compilation, Android lint, Detekt,
and the debug build on pushes and pull requests. Emulator execution is disabled
because hosted emulator provisioning is not reliable in this environment. The release workflow runs
for `v*` tags or manual dispatch and publishes a signed APK.

Configure these repository secrets before using the release workflow:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The local release keystore and `keystore.properties` are intentionally ignored
by Git. Keep the signing key backed up because future Android updates must use
the same key.

## Content

Run `python3 tools/fetch_quran_data.py` to refresh the bundled data. The script
uses the equran.id API and writes a compact `app/src/main/assets/quran.json`.
Review `CONTENT_PROVENANCE.md` before redistributing a build.
