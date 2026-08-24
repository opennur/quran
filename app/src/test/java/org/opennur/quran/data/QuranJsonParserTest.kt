package org.opennur.quran.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONException

class QuranJsonParserTest {
    @Test
    fun parsesSurahAndAyahFields() {
        val json = """
            [{
              "number": 1,
              "arabicName": "الفاتحة",
              "latinName": "Al-Fatihah",
              "meaning": "Pembukaan",
              "ayahs": [{
                "number": 1,
                "arabic": "بِسْمِ اللّٰهِ",
                "translation": "Dengan nama Allah",
                "page": 1,
                "juz": 1
              }]
            }]
        """.trimIndent()

        val surahs = QuranJsonParser.parse(json)

        assertEquals(1, surahs.size)
        assertEquals("Al-Fatihah", surahs.single().latinName)
        assertEquals("بِسْمِ اللّٰهِ", surahs.single().ayahs.single().arabic)
        assertEquals("Dengan nama Allah", surahs.single().ayahs.single().translation)
        assertEquals(1, surahs.single().ayahs.single().page)
        assertEquals(1, surahs.single().ayahs.single().juz)
    }

    @Test
    fun normalizesArabicSearchVariants() {
        val normalized = ArabicNormalizer.normalize("أَلْحَمْدُ لِلَّهِ")

        assertEquals("الحمد لله", normalized)
        assertTrue(normalized.contains("لله"))
    }

    @Test
    fun preservesSurahOrderingAndAyahMetadata() {
        val json = """
            [
              {
                "number": 2,
                "arabicName": "البقرة",
                "latinName": "Al-Baqarah",
                "meaning": "Sapi",
                "ayahs": [{
                  "number": 255,
                  "arabic": "اللّٰهُ",
                  "translation": "Allah",
                  "page": 42,
                  "juz": 3
                }]
              },
              {
                "number": 3,
                "arabicName": "آل عمران",
                "latinName": "Ali Imran",
                "meaning": "Keluarga Imran",
                "ayahs": []
              }
            ]
        """.trimIndent()

        val surahs = QuranJsonParser.parse(json)

        assertEquals(listOf(2, 3), surahs.map { it.number })
        assertEquals(255, surahs.first().ayahs.single().number)
        assertEquals(42, surahs.first().ayahs.single().page)
        assertEquals(3, surahs.first().ayahs.single().juz)
    }

    @Test(expected = JSONException::class)
    fun rejectsNonArrayRoot() {
        QuranJsonParser.parse("{\"number\": 1}")
    }
}
