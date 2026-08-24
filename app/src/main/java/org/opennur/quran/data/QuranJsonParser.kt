package org.opennur.quran.data

import org.json.JSONArray

object QuranJsonParser {
    fun parse(json: String): List<Surah> {
        val root = JSONArray(json)
        return buildList(root.length()) {
            for (surahIndex in 0 until root.length()) {
                val sourceSurah = root.getJSONObject(surahIndex)
                val sourceAyahs = sourceSurah.getJSONArray("ayahs")
                val ayahs = buildList(sourceAyahs.length()) {
                    for (ayahIndex in 0 until sourceAyahs.length()) {
                        val sourceAyah = sourceAyahs.getJSONObject(ayahIndex)
                        add(
                            Ayah(
                                number = sourceAyah.getInt("number"),
                                arabic = sourceAyah.getString("arabic"),
                                translation = sourceAyah.getString("translation"),
                                page = sourceAyah.getInt("page"),
                                juz = sourceAyah.getInt("juz"),
                            ),
                        )
                    }
                }
                add(
                    Surah(
                        number = sourceSurah.getInt("number"),
                        arabicName = sourceSurah.getString("arabicName"),
                        latinName = sourceSurah.getString("latinName"),
                        meaning = sourceSurah.getString("meaning"),
                        ayahs = ayahs,
                    ),
                )
            }
        }
    }
}
