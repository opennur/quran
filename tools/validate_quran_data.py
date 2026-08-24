#!/usr/bin/env python3
"""Validate the exact shape and coverage of the bundled Quran asset."""

import json
import pathlib


ASSET = pathlib.Path(__file__).resolve().parents[1] / "app/src/main/assets/quran.json"


def main() -> None:
    surahs = json.loads(ASSET.read_text(encoding="utf-8"))
    assert len(surahs) == 114, len(surahs)
    assert [surah["number"] for surah in surahs] == list(range(1, 115))

    ayah_count = 0
    for surah in surahs:
        ayahs = surah["ayahs"]
        assert surah["arabicName"].strip()
        assert surah["latinName"].strip()
        assert [ayah["number"] for ayah in ayahs] == list(range(1, len(ayahs) + 1))
        for ayah in ayahs:
            assert ayah["arabic"].strip(), (surah["number"], ayah["number"])
            assert ayah["translation"].strip(), (surah["number"], ayah["number"])
            assert "\u08D6" not in ayah["arabic"], (surah["number"], ayah["number"])
        ayah_count += len(ayahs)

    assert ayah_count == 6236, ayah_count
    print(f"Valid Quran asset: {len(surahs)} surahs, {ayah_count} ayahs")


if __name__ == "__main__":
    main()
