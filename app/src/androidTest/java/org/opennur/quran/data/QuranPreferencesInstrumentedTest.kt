package org.opennur.quran.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuranPreferencesInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val rawPreferences = context.getSharedPreferences("quran_preferences", Context.MODE_PRIVATE)

    @Before
    fun clearPreferences() {
        rawPreferences.edit().clear().commit()
    }

    @After
    fun restoreDefaults() {
        rawPreferences.edit().clear().commit()
    }

    @Test
    fun savesAndRestoresReaderPreferences() {
        val preferences = QuranPreferences(context)

        preferences.saveLastRead(AyahRef(surah = 36, ayah = 12))
        preferences.saveBookmarks(setOf("36:12", "1:1"))
        preferences.saveShowTranslation(false)
        preferences.saveFontScale(1.25f)
        preferences.saveDarkMode(true)
        preferences.saveFlowingMode(true)
        preferences.saveTajwidEnabled(false)

        val restored = QuranPreferences(context)

        assertEquals(AyahRef(36, 12), restored.lastRead())
        assertEquals(setOf("36:12", "1:1"), restored.bookmarks())
        assertFalse(restored.showTranslation())
        assertEquals(1.25f, restored.fontScale())
        assertTrue(restored.darkMode())
        assertTrue(restored.flowingMode())
        assertFalse(restored.tajwidEnabled())
    }
}
