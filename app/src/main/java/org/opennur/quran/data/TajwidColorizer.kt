package org.opennur.quran.data

/** Rules that can be identified from the bundled, fully vocalized text. */
enum class TajwidCategory {
    MAD,
    GHUNNAH,
    IDGHAM,
    IKHFA,
    IQLAB,
    QALQALAH,
    LAM_JALALAH,
    WAQAF,
}

data class TajwidSpan(
    val start: Int,
    val end: Int,
    val category: TajwidCategory,
)

/**
 * A deterministic text colorizer. It is a visual learning aid, not a
 * replacement for a qualified teacher or audio-based pronunciation analysis.
 */
object TajwidColorizer {
    private const val FATHA = '\u064E'
    private const val DAMMA = '\u064F'
    private const val KASRA = '\u0650'
    private const val SHADDAH = '\u0651'
    private const val SUKUN = '\u0652'
    private const val DAGGER_ALIF = '\u0670'

    private val TANWIN = setOf('\u064B', '\u064C', '\u064D')
    private val VOWEL_MARKS = setOf(FATHA, DAMMA, KASRA) + TANWIN
    private val IDGHAM_LETTERS = "ينمو".toSet()
    private val IDGHAM_WITHOUT_GHUNNAH = "لر".toSet()
    private val IKHFA_LETTERS = "تثجدذزسشصضطظفقك".toSet()
    private val IZHAR_LETTERS = "ءهعحغخ".toSet()
    private val QALQALAH_LETTERS = "قطبجد".toSet()
    private val WAQAF_SIGNS = setOf(
        '\u06D6', // صلي
        '\u06D7', // قلي
        '\u06D8', // مـ
        '\u06D9', // لا
        '\u06DA', // ج
        '\u06DB', // معانقة
    )

    private val categoryPriority = mapOf(
        TajwidCategory.MAD to 1,
        TajwidCategory.GHUNNAH to 2,
        TajwidCategory.IDGHAM to 3,
        TajwidCategory.IKHFA to 3,
        TajwidCategory.IQLAB to 3,
        TajwidCategory.QALQALAH to 3,
        TajwidCategory.LAM_JALALAH to 4,
        TajwidCategory.WAQAF to 5,
    )

    /**
     * Returns character ranges for [text]. [nextText] supplies the first
     * letter of the next ayah for rules that cross an ayah boundary.
     */
    fun spans(text: String, nextText: String? = null): List<TajwidSpan> {
        if (text.isEmpty()) return emptyList()

        val categories = mutableMapOf<Int, TajwidCategory>()
        fun applyRange(start: Int, end: Int, category: TajwidCategory) {
            for (index in start until end) {
                val current = categories[index]
                if (current == null || categoryPriority.getValue(category) >= categoryPriority.getValue(current)) {
                    categories[index] = category
                }
            }
        }

        for (index in text.indices) {
            val letter = text[index]
            if (!isArabicLetter(letter)) continue

            val marks = marksAfter(text, index)
            val nextLetter = nextLetterAfter(text, index)
                ?: nextText?.firstOrNull(::isArabicLetter)
            val rangeEnd = endOfLetterMarks(text, index)

            val nunSakinOrTanwin = (letter == 'ن' && SUKUN in marks) || marks.any { it in TANWIN }
            if (nunSakinOrTanwin && nextLetter != null) {
                when {
                    nextLetter in IDGHAM_LETTERS || nextLetter in IDGHAM_WITHOUT_GHUNNAH -> {
                        applyRange(index, rangeEnd, TajwidCategory.IDGHAM)
                    }
                    nextLetter == 'ب' -> applyRange(index, rangeEnd, TajwidCategory.IQLAB)
                    nextLetter in IKHFA_LETTERS -> applyRange(index, rangeEnd, TajwidCategory.IKHFA)
                    nextLetter in IZHAR_LETTERS -> Unit
                }
            }

            if (marks.contains(SHADDAH) && (letter == 'ن' || letter == 'م')) {
                applyRange(index, rangeEnd, TajwidCategory.GHUNNAH)
            }

            if (letter == 'م' && SUKUN in marks && nextLetter == 'م') {
                applyRange(index, rangeEnd, TajwidCategory.GHUNNAH)
            }

            if (letter in QALQALAH_LETTERS && SUKUN in marks) {
                applyRange(index, rangeEnd, TajwidCategory.QALQALAH)
            }

            val previousVowel = vowelBefore(text, index)
            val madLetter = when (letter) {
                'ا' -> previousVowel == FATHA
                'ي' -> previousVowel == KASRA && marks.none { it in VOWEL_MARKS }
                'و' -> previousVowel == DAMMA && marks.none { it in VOWEL_MARKS }
                else -> false
            }
            if (DAGGER_ALIF in marks || madLetter || letter == 'آ') {
                applyRange(index, rangeEnd, TajwidCategory.MAD)
            }

            if (letter == 'ل' && SHADDAH in marks && isAllahWord(text, index)) {
                applyRange(index, rangeEnd, TajwidCategory.LAM_JALALAH)
            }
        }

        text.forEachIndexed { index, character ->
            if (character in WAQAF_SIGNS) {
                applyRange(index, index + 1, TajwidCategory.WAQAF)
            }
        }

        return categories.toSortedMap().entries
            .fold(mutableListOf()) { ranges, entry ->
                val last = ranges.lastOrNull()
                if (last != null && last.end == entry.key && last.category == entry.value) {
                    ranges[ranges.lastIndex] = last.copy(end = entry.key + 1)
                } else {
                    ranges += TajwidSpan(entry.key, entry.key + 1, entry.value)
                }
                ranges
            }
    }

    fun isArabicLetter(character: Char): Boolean {
        val code = character.code
        return code in 0x0600..0x06FF && Character.getType(character) == Character.OTHER_LETTER.toInt()
    }

    private fun marksAfter(text: String, index: Int): Set<Char> {
        val marks = mutableSetOf<Char>()
        var cursor = index + 1
        while (cursor < text.length && !isArabicLetter(text[cursor]) && !text[cursor].isWhitespace()) {
            marks += text[cursor]
            cursor++
        }
        return marks
    }

    private fun endOfLetterMarks(text: String, index: Int): Int {
        var cursor = index + 1
        while (cursor < text.length && !isArabicLetter(text[cursor]) && !text[cursor].isWhitespace()) {
            cursor++
        }
        return cursor
    }

    private fun nextLetterAfter(text: String, index: Int): Char? {
        for (cursor in index + 1 until text.length) {
            if (isArabicLetter(text[cursor])) return text[cursor]
        }
        return null
    }

    private fun vowelBefore(text: String, index: Int): Char? {
        for (cursor in index - 1 downTo 0) {
            if (text[cursor] in VOWEL_MARKS || text[cursor] == SUKUN || text[cursor] == SHADDAH) {
                if (text[cursor] in VOWEL_MARKS) return text[cursor]
            }
            if (isArabicLetter(text[cursor])) return null
        }
        return null
    }

    private fun isAllahWord(text: String, index: Int): Boolean {
        var start = index
        while (start > 0 && !text[start - 1].isWhitespace()) start--
        var end = index
        while (end < text.length && !text[end].isWhitespace()) end++
        val base = text.substring(start, end)
            .filter(::isArabicLetter)
            .replace('ٱ', 'ا')
        return base == "الله" || base.endsWith("الله")
    }
}
