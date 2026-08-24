package org.opennur.quran.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.opennur.quran.data.AyahRef
import org.opennur.quran.data.ArabicNormalizer
import org.opennur.quran.data.QuranPreferences
import org.opennur.quran.data.QuranRepository
import org.opennur.quran.data.SearchResult
import org.opennur.quran.data.Surah

data class QuranUiState(
    val surahs: List<Surah> = emptyList(),
    val selectedSurah: Int = 1,
    val selectedAyah: Int = 1,
    val jumpToken: Long = 0,
    val bookmarks: Set<String> = emptySet(),
    val query: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val showTranslation: Boolean = true,
    val fontScale: Float = 1f,
    val darkMode: Boolean = false,
    val flowingMode: Boolean = false,
    val tajwidEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepository(application)
    private val preferences = QuranPreferences(application)
    private val _uiState = MutableStateFlow(
        QuranUiState(
            bookmarks = preferences.bookmarks(),
            showTranslation = preferences.showTranslation(),
            fontScale = preferences.fontScale(),
            darkMode = preferences.darkMode(),
            flowingMode = preferences.flowingMode(),
            tajwidEnabled = preferences.tajwidEnabled(),
        ),
    )
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.load() }
                .onSuccess { surahs ->
                    val last = preferences.lastRead()
                    val surah = surahs.firstOrNull { it.number == last.surah } ?: surahs.first()
                    val ayah = last.ayah.coerceIn(1, surah.ayahs.size)
                    _uiState.update {
                        it.copy(
                            surahs = surahs,
                            selectedSurah = surah.number,
                            selectedAyah = ayah,
                            jumpToken = 1,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unable to load Quran data",
                        )
                    }
                }
        }
    }

    fun selectSurah(number: Int) {
        val surah = _uiState.value.surahs.firstOrNull { it.number == number } ?: return
        _uiState.update {
            it.copy(
                selectedSurah = surah.number,
                selectedAyah = 1,
                jumpToken = it.jumpToken + 1,
            )
        }
    }

    fun openAyah(ref: AyahRef) {
        val surah = _uiState.value.surahs.firstOrNull { it.number == ref.surah } ?: return
        _uiState.update {
            it.copy(
                selectedSurah = surah.number,
                selectedAyah = ref.ayah.coerceIn(1, surah.ayahs.size),
                jumpToken = it.jumpToken + 1,
            )
        }
    }

    fun updateLastRead(ref: AyahRef) {
        preferences.saveLastRead(ref)
        _uiState.update {
            it.copy(
                selectedSurah = ref.surah,
                selectedAyah = ref.ayah,
            )
        }
    }

    fun openPage(page: Int) {
        openFirstAyah { it.page == page }
    }

    fun openJuz(juz: Int) {
        openFirstAyah { it.juz == juz }
    }

    fun toggleBookmark(ref: AyahRef) {
        val key = key(ref)
        val updated = _uiState.value.bookmarks.toMutableSet().apply {
            if (!add(key)) remove(key)
        }.toSet()
        preferences.saveBookmarks(updated)
        _uiState.update { it.copy(bookmarks = updated) }
    }

    fun isBookmarked(ref: AyahRef): Boolean {
        return key(ref) in _uiState.value.bookmarks
    }

    fun setQuery(query: String) {
        val normalizedQuery = ArabicNormalizer.normalize(query)
        val results = if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            _uiState.value.surahs.flatMap { surah ->
                surah.ayahs.mapNotNull { ayah ->
                    val arabicMatch = ArabicNormalizer.normalize(ayah.arabic)
                        .contains(normalizedQuery)
                    val translationMatch = ayah.translation.lowercase().contains(normalizedQuery)
                    if (arabicMatch || translationMatch) SearchResult(surah, ayah) else null
                }
            }
        }
        _uiState.update { it.copy(query = query, searchResults = results) }
    }

    fun setShowTranslation(show: Boolean) {
        preferences.saveShowTranslation(show)
        if (show) {
            preferences.saveFlowingMode(false)
            _uiState.update { it.copy(showTranslation = true, flowingMode = false) }
        } else {
            _uiState.update { it.copy(showTranslation = false) }
        }
    }

    fun setFontScale(scale: Float) {
        preferences.saveFontScale(scale)
        _uiState.update { it.copy(fontScale = scale) }
    }

    fun setDarkMode(enabled: Boolean) {
        preferences.saveDarkMode(enabled)
        _uiState.update { it.copy(darkMode = enabled) }
    }

    fun setFlowingMode(enabled: Boolean) {
        preferences.saveFlowingMode(enabled)
        if (enabled) {
            preferences.saveShowTranslation(false)
            _uiState.update { it.copy(flowingMode = true, showTranslation = false) }
        } else {
            _uiState.update { it.copy(flowingMode = false) }
        }
    }

    fun setTajwidEnabled(enabled: Boolean) {
        preferences.saveTajwidEnabled(enabled)
        _uiState.update { it.copy(tajwidEnabled = enabled) }
    }

    private fun openFirstAyah(predicate: (org.opennur.quran.data.Ayah) -> Boolean) {
        val target = _uiState.value.surahs.asSequence()
            .flatMap { surah ->
                surah.ayahs.asSequence().map { ayah -> AyahRef(surah.number, ayah.number) to ayah }
            }
            .firstOrNull { (_, ayah) -> predicate(ayah) }
            ?.first
        if (target != null) openAyah(target)
    }

    private fun key(ref: AyahRef): String = "${ref.surah}:${ref.ayah}"
}
