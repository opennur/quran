package org.opennur.quran.data

data class Ayah(
    val number: Int,
    val arabic: String,
    val translation: String,
)

data class Surah(
    val number: Int,
    val arabicName: String,
    val latinName: String,
    val meaning: String,
    val ayahs: List<Ayah>,
)

data class AyahRef(
    val surah: Int,
    val ayah: Int,
)

data class SearchResult(
    val surah: Surah,
    val ayah: Ayah,
)
