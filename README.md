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
./gradlew test
./gradlew assembleDebug
```

The generated APK is under `app/build/outputs/apk/debug/`.

## Content

Run `python3 tools/fetch_quran_data.py` to refresh the bundled data. The script
uses the equran.id API and writes a compact `app/src/main/assets/quran.json`.
Review `CONTENT_PROVENANCE.md` before redistributing a build.
