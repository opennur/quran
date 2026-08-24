package org.opennur.quran.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranRepositoryInstrumentedTest {
    @Test
    fun bundledQuranAssetLoadsCompletely() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val surahs = QuranRepository(context).load()

        assertEquals(114, surahs.size)
        assertEquals(6236, surahs.sumOf { it.ayahs.size })
        assertEquals(1, surahs.first().number)
        assertEquals(114, surahs.last().number)
        assertFalse(surahs.any { it.ayahs.isEmpty() })
        assertTrue(surahs.all { surah ->
            surah.ayahs.all { ayah -> ayah.page > 0 && ayah.juz in 1..30 }
        })
    }
}
