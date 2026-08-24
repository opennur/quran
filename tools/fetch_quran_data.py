#!/usr/bin/env python3
"""Download and compact the offline Quran asset used by the Android app."""

import json
import pathlib
import sys
import time
import urllib.request


API_TEMPLATE = "https://equran.id/api/v2/surat/{number}"
OUTPUT = pathlib.Path(__file__).resolve().parents[1] / "app/src/main/assets/quran.json"
UNSUPPORTED_ANNOTATION = "\u08D6"


def fetch(number: int) -> dict:
    request = urllib.request.Request(
        API_TEMPLATE.format(number=number),
        headers={"User-Agent": "org.opennur.quran/1.0"},
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        payload = json.load(response)
    if payload.get("code") != 200 or "data" not in payload:
        raise RuntimeError(f"Unexpected response for surah {number}: {payload}")
    return payload["data"]


def compact(surah: dict) -> dict:
    return {
        "number": surah["nomor"],
        "arabicName": surah["nama"],
        "latinName": surah["namaLatin"],
        "meaning": surah["arti"],
        "ayahs": [
            {
                "number": ayah["nomorAyat"],
                "arabic": clean_arabic(ayah["teksArab"]),
                "translation": ayah["teksIndonesia"],
            }
            for ayah in surah["ayat"]
        ],
    }


def clean_arabic(text: str) -> str:
    """Remove the source's unsupported high ligature that renders as a tofu box."""
    return text.replace(UNSUPPORTED_ANNOTATION, "").strip()


def main() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    surahs = []
    for number in range(1, 115):
        print(f"Fetching surah {number}/114", flush=True)
        surahs.append(compact(fetch(number)))
        time.sleep(0.05)

    ayah_count = sum(len(surah["ayahs"]) for surah in surahs)
    if len(surahs) != 114 or ayah_count != 6236:
        raise RuntimeError(f"Validation failed: {len(surahs)} surahs, {ayah_count} ayahs")

    OUTPUT.write_text(
        json.dumps(surahs, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    print(f"Wrote {OUTPUT} ({ayah_count} ayahs)")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(error, file=sys.stderr)
        raise
