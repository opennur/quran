package org.opennur.quran.data

import org.junit.Assert.assertTrue
import org.junit.Test

class TajwidColorizerTest {
    @Test
    fun classifiesNunSakinRules() {
        val idgham = TajwidColorizer.spans("مِنْ يَعْمَلْ")
        val iqlab = TajwidColorizer.spans("مِنْ بَعْدِ")
        val ikhfa = TajwidColorizer.spans("مِنْ قَبْلِ")

        assertTrue(idgham.any { it.category == TajwidCategory.IDGHAM })
        assertTrue(iqlab.any { it.category == TajwidCategory.IQLAB })
        assertTrue(ikhfa.any { it.category == TajwidCategory.IKHFA })
    }

    @Test
    fun classifiesMadQalqalahLamAndWaqaf() {
        val spans = TajwidColorizer.spans("قَالَ يَقْطَعُ اللّٰهِۗ")
            .map { it.category }

        assertTrue(TajwidCategory.MAD in spans)
        assertTrue(TajwidCategory.QALQALAH in spans)
        assertTrue(TajwidCategory.LAM_JALALAH in spans)
        assertTrue(TajwidCategory.WAQAF in spans)
    }
}
