package org.opennur.quran.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
                "translation": "Dengan nama Allah"
              }]
            }]
        """.trimIndent()

        val surahs = QuranJsonParser.parse(json)

        assertEquals(1, surahs.size)
        assertEquals("Al-Fatihah", surahs.single().latinName)
        assertEquals("بِسْمِ اللّٰهِ", surahs.single().ayahs.single().arabic)
        assertEquals("Dengan nama Allah", surahs.single().ayahs.single().translation)
    }

    @Test
    fun normalizesArabicSearchVariants() {
        val normalized = ArabicNormalizer.normalize("أَلْحَمْدُ لِلَّهِ")

        assertEquals("الحمد لله", normalized)
        assertTrue(normalized.contains("لله"))
    }
}
