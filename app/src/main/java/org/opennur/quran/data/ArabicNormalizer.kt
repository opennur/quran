package org.opennur.quran.data

import java.util.Locale

object ArabicNormalizer {
    private val marks = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

    fun normalize(text: String): String {
        return text
            .lowercase(Locale.ROOT)
            .replace(marks, "")
            .replace('\u0640'.toString(), "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace('ة', 'ه')
            .trim()
    }
}
