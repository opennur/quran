package org.opennur.quran.data

import android.content.Context
import androidx.core.content.edit

class QuranPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "quran_preferences",
        Context.MODE_PRIVATE,
    )

    fun lastRead(): AyahRef {
        return AyahRef(
            surah = preferences.getInt(KEY_LAST_SURAH, 1),
            ayah = preferences.getInt(KEY_LAST_AYAH, 1),
        )
    }

    fun saveLastRead(ref: AyahRef) {
        preferences.edit {
            putInt(KEY_LAST_SURAH, ref.surah)
            putInt(KEY_LAST_AYAH, ref.ayah)
        }
    }

    fun bookmarks(): Set<String> {
        return preferences.getStringSet(KEY_BOOKMARKS, emptySet()).orEmpty().toSet()
    }

    fun saveBookmarks(bookmarks: Set<String>) {
        preferences.edit { putStringSet(KEY_BOOKMARKS, bookmarks) }
    }

    fun showTranslation(): Boolean = preferences.getBoolean(KEY_SHOW_TRANSLATION, true)

    fun saveShowTranslation(show: Boolean) {
        preferences.edit { putBoolean(KEY_SHOW_TRANSLATION, show) }
    }

    fun fontScale(): Float = preferences.getFloat(KEY_FONT_SCALE, 1f)

    fun saveFontScale(scale: Float) {
        preferences.edit { putFloat(KEY_FONT_SCALE, scale) }
    }

    fun darkMode(): Boolean = preferences.getBoolean(KEY_DARK_MODE, false)

    fun saveDarkMode(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_DARK_MODE, enabled) }
    }

    fun flowingMode(): Boolean = preferences.getBoolean(KEY_FLOWING_MODE, false)

    fun saveFlowingMode(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_FLOWING_MODE, enabled) }
    }

    fun tajwidEnabled(): Boolean = preferences.getBoolean(KEY_TAJWID_ENABLED, true)

    fun saveTajwidEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(KEY_TAJWID_ENABLED, enabled) }
    }

    companion object {
        private const val KEY_LAST_SURAH = "last_surah"
        private const val KEY_LAST_AYAH = "last_ayah"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_SHOW_TRANSLATION = "show_translation"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_FLOWING_MODE = "flowing_mode"
        private const val KEY_TAJWID_ENABLED = "tajwid_enabled"
    }
}
