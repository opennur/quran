package org.opennur.quran.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicNormalizerTest {
    @Test
    fun removesMarksAndTatweel() {
        assertEquals("محمد", ArabicNormalizer.normalize("مُحَمَّــــد"))
    }

    @Test
    fun normalizesArabicLetterVariants() {
        assertEquals("ا ا ا ا ي و ي ه", ArabicNormalizer.normalize("أ إ آ ٱ ى ؤ ئ ة"))
    }

    @Test
    fun trimsAndLowercasesLatinText() {
        assertEquals("allah", ArabicNormalizer.normalize("  ALLAH  "))
    }
}
