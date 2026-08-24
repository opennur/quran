package org.opennur.quran.data

import android.content.Context

class QuranRepository(context: Context) {
    private val appContext = context.applicationContext

    fun load(): List<Surah> {
        val json = appContext.assets.open("quran.json").bufferedReader().use { it.readText() }
        return QuranJsonParser.parse(json)
    }
}
