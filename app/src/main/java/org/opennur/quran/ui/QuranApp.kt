package org.opennur.quran.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.opennur.quran.R
import org.opennur.quran.data.Ayah
import org.opennur.quran.data.AyahRef
import org.opennur.quran.data.SearchResult
import org.opennur.quran.data.Surah
import org.opennur.quran.data.TajwidCategory
import org.opennur.quran.data.TajwidColorizer

private enum class Destination {
    READER,
    SEARCH,
    BOOKMARKS,
    SETTINGS,
}

private enum class ReaderPicker {
    NONE,
    SURAH,
    AYAH,
    PAGE,
    JUZ,
}

private val ArabicReadingTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = true),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

private fun QuranUiState.currentAyahData(): Ayah? {
    return surahs
        .firstOrNull { it.number == selectedSurah }
        ?.ayahs
        ?.firstOrNull { it.number == selectedAyah }
}

private fun QuranUiState.currentPage(): Int = currentAyahData()?.page ?: 1

private fun QuranUiState.currentJuz(): Int = currentAyahData()?.juz ?: 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranApp(viewModel: QuranViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(Destination.READER.name) }
    var readerPicker by rememberSaveable { mutableStateOf(ReaderPicker.NONE.name) }
    val currentDestination = Destination.valueOf(destination)

    QuranTheme(darkMode = state.darkMode) {
        Scaffold(
            topBar = {
                if (currentDestination == Destination.READER) {
                    ReaderTopBar(
                        flowingMode = state.flowingMode,
                        onToggleFlow = { viewModel.setFlowingMode(!state.flowingMode) },
                        onSearch = { destination = Destination.SEARCH.name },
                        onBookmarks = { destination = Destination.BOOKMARKS.name },
                        onSettings = { destination = Destination.SETTINGS.name },
                    )
                }
            },
            bottomBar = {
                if (currentDestination != Destination.SEARCH && currentDestination != Destination.READER) {
                    AppNavigation(
                        destination = currentDestination,
                        onDestinationSelected = { destination = it.name },
                    )
                }
            },
        ) { paddingValues ->
            when (currentDestination) {
                Destination.READER -> ReaderScreen(
                    state = state,
                    viewModel = viewModel,
                    contentPadding = paddingValues,
                    onPickSurah = { readerPicker = ReaderPicker.SURAH.name },
                    onPickAyah = { readerPicker = ReaderPicker.AYAH.name },
                    onPickPage = { readerPicker = ReaderPicker.PAGE.name },
                    onPickJuz = { readerPicker = ReaderPicker.JUZ.name },
                )

                Destination.SEARCH -> SearchScreen(
                    state = state,
                    viewModel = viewModel,
                    contentPadding = paddingValues,
                    onBack = { destination = Destination.READER.name },
                    onOpen = {
                        viewModel.openAyah(AyahRef(it.surah.number, it.ayah.number))
                        destination = Destination.READER.name
                    },
                )

                Destination.BOOKMARKS -> BookmarksScreen(
                    state = state,
                    viewModel = viewModel,
                    contentPadding = paddingValues,
                    onBack = { destination = Destination.READER.name },
                    onOpen = {
                        viewModel.openAyah(it)
                        destination = Destination.READER.name
                    },
                )

                Destination.SETTINGS -> SettingsScreen(
                    state = state,
                    viewModel = viewModel,
                    contentPadding = paddingValues,
                    onBack = { destination = Destination.READER.name },
                )
            }
        }
    }

    if (state.surahs.isNotEmpty()) {
        when (ReaderPicker.valueOf(readerPicker)) {
            ReaderPicker.NONE -> Unit
            ReaderPicker.SURAH -> SurahPicker(
                surahs = state.surahs,
                selectedSurah = state.selectedSurah,
                onDismiss = { readerPicker = ReaderPicker.NONE.name },
                onSelect = {
                    viewModel.selectSurah(it)
                    readerPicker = ReaderPicker.NONE.name
                },
            )
            ReaderPicker.AYAH -> {
                val surah = state.surahs.firstOrNull { it.number == state.selectedSurah }
                if (surah != null) {
                    AyahPicker(
                        surah = surah,
                        selectedAyah = state.selectedAyah,
                        onDismiss = { readerPicker = ReaderPicker.NONE.name },
                        onSelect = {
                            viewModel.openAyah(AyahRef(surah.number, it))
                            readerPicker = ReaderPicker.NONE.name
                        },
                    )
                }
            }
            ReaderPicker.PAGE -> NumberPicker(
                title = "Page",
                numbers = 1..604,
                selected = state.currentPage(),
                onDismiss = { readerPicker = ReaderPicker.NONE.name },
                onSelect = {
                    viewModel.openPage(it)
                    readerPicker = ReaderPicker.NONE.name
                },
            )
            ReaderPicker.JUZ -> NumberPicker(
                title = "Juz",
                numbers = 1..30,
                selected = state.currentJuz(),
                onDismiss = { readerPicker = ReaderPicker.NONE.name },
                onSelect = {
                    viewModel.openJuz(it)
                    readerPicker = ReaderPicker.NONE.name
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon?.invoke()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            actions()
        }
    }
}

@Composable
private fun ReaderTopBar(
    flowingMode: Boolean,
    onToggleFlow: () -> Unit,
    onSearch: () -> Unit,
    onBookmarks: () -> Unit,
    onSettings: () -> Unit,
) {
    CompactTopBar(
        title = "Quran",
        subtitle = "Read offline",
        actions = {
            IconButton(onClick = onToggleFlow) {
                Icon(
                    imageVector = if (flowingMode) Icons.Default.ViewAgenda else Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = if (flowingMode) "Switch to card view" else "Switch to Mushaf view",
                )
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search ayahs")
            }
            IconButton(onClick = onBookmarks) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmarks")
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
    )
}

@Composable
private fun AppNavigation(
    destination: Destination,
    onDestinationSelected: (Destination) -> Unit,
) {
    NavigationBar(modifier = Modifier.navigationBarsPadding()) {
        NavigationBarItem(
            selected = destination == Destination.READER,
            onClick = { onDestinationSelected(Destination.READER) },
            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
            label = { Text("Read") },
        )
        NavigationBarItem(
            selected = destination == Destination.BOOKMARKS,
            onClick = { onDestinationSelected(Destination.BOOKMARKS) },
            icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
            label = { Text("Saved") },
        )
        NavigationBarItem(
            selected = destination == Destination.SETTINGS,
            onClick = { onDestinationSelected(Destination.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
        )
    }
}

@Composable
private fun ReaderScreen(
    state: QuranUiState,
    viewModel: QuranViewModel,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onPickSurah: () -> Unit,
    onPickAyah: () -> Unit,
    onPickPage: () -> Unit,
    onPickJuz: () -> Unit,
) {
    val surah = state.surahs.firstOrNull { it.number == state.selectedSurah }
    if (state.isLoading) {
        LoadingState(contentPadding)
        return
    }
    if (state.error != null || surah == null) {
        ErrorState(state.error ?: "Surah unavailable", contentPadding)
        return
    }

    val arabicFont = FontFamily(Font(R.font.uthmani))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .navigationBarsPadding(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onPickSurah),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(surah.latinName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${surah.meaning}  ·  ${surah.ayahs.size} ayahs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Text(
                    surah.arabicName,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.End,
                )
            }
        }

        ReaderJumpBar(
            ayah = state.selectedAyah,
            page = state.currentPage(),
            juz = state.currentJuz(),
            onPickAyah = onPickAyah,
            onPickPage = onPickPage,
            onPickJuz = onPickJuz,
        )

        if (state.flowingMode) {
            MushafFlowReader(
                surah = surah,
                fontScale = state.fontScale,
                showTranslation = state.showTranslation,
                tajwidEnabled = state.tajwidEnabled,
                arabicFont = arabicFont,
                selectedAyah = state.selectedAyah,
                jumpToken = state.jumpToken,
                onAyahTap = {
                    viewModel.openAyah(it)
                    viewModel.updateLastRead(it)
                },
            )
        } else {
            SeparatedAyahReader(
                surah = surah,
                state = state,
                viewModel = viewModel,
                arabicFont = arabicFont,
                tajwidEnabled = state.tajwidEnabled,
            )
        }
    }
}

@Composable
private fun ReaderJumpBar(
    ayah: Int,
    page: Int,
    juz: Int,
    onPickAyah: () -> Unit,
    onPickPage: () -> Unit,
    onPickJuz: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedButton(
            onClick = onPickAyah,
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
        ) {
            Text("Ayah $ayah", style = MaterialTheme.typography.labelMedium)
        }
        OutlinedButton(
            onClick = onPickPage,
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
        ) {
            Text("Page $page", style = MaterialTheme.typography.labelMedium)
        }
        OutlinedButton(
            onClick = onPickJuz,
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
        ) {
            Text("Juz $juz", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SeparatedAyahReader(
    surah: Surah,
    state: QuranUiState,
    viewModel: QuranViewModel,
    arabicFont: FontFamily,
    tajwidEnabled: Boolean,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.jumpToken) {
        val target = (state.selectedAyah - 1).coerceIn(0, surah.ayahs.lastIndex)
        listState.scrollToItem(target)
    }
    LaunchedEffect(surah.number) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { index ->
                delay(250)
                surah.ayahs.getOrNull(index)?.let {
                    viewModel.updateLastRead(AyahRef(surah.number, it.number))
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 12.dp,
            end = 12.dp,
            bottom = 16.dp,
        ),
    ) {
        itemsIndexed(surah.ayahs, key = { _, ayah -> ayah.number }) { index, ayah ->
            AyahCard(
                surah = surah,
                ayah = ayah,
                fontScale = state.fontScale,
                showTranslation = state.showTranslation,
                arabicFont = arabicFont,
                tajwidEnabled = tajwidEnabled,
                nextAyah = surah.ayahs.getOrNull(index + 1),
                bookmarked = viewModel.isBookmarked(AyahRef(surah.number, ayah.number)),
                onBookmark = { viewModel.toggleBookmark(AyahRef(surah.number, ayah.number)) },
                onOpen = { viewModel.updateLastRead(AyahRef(surah.number, ayah.number)) },
            )
        }
    }
}

private const val AYAH_ANNOTATION = "ayah"
private const val FLOW_CHUNK_SIZE = 24

@Composable
private fun MushafFlowReader(
    surah: Surah,
    fontScale: Float,
    showTranslation: Boolean,
    arabicFont: FontFamily,
    tajwidEnabled: Boolean,
    selectedAyah: Int,
    jumpToken: Long,
    onAyahTap: (AyahRef) -> Unit,
) {
    val chunks = remember(surah.number) { surah.ayahs.chunked(FLOW_CHUNK_SIZE) }
    val listState = rememberLazyListState()
    var targetOffset by remember { mutableStateOf<Float?>(null) }
    var targetChunkReady by remember { mutableStateOf(false) }
    var targetApplied by remember { mutableStateOf(false) }
    val targetChunk = (selectedAyah - 1).coerceAtLeast(0) / FLOW_CHUNK_SIZE

    LaunchedEffect(jumpToken, selectedAyah, surah.number) {
        targetOffset = null
        targetChunkReady = false
        targetApplied = false
        listState.scrollToItem(targetChunk)
        targetChunkReady = true
    }
    LaunchedEffect(jumpToken, targetOffset, targetChunkReady) {
        val offset = targetOffset ?: return@LaunchedEffect
        if (targetChunkReady && !targetApplied) {
            listState.scrollBy((offset - 24f).coerceAtLeast(0f))
            targetApplied = true
        }
    }
    val targetAyah = selectedAyah

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
        ),
    ) {
        itemsIndexed(chunks, key = { index, _ -> "flow-$index" }) { index, chunk ->
            val chunkTargetAyah = targetAyah.takeIf { ayah ->
                chunk.firstOrNull()?.number?.let { first ->
                    ayah in first..(chunk.lastOrNull()?.number ?: first)
                } == true
            }
            FlowingChunk(
                surahNumber = surah.number,
                ayahs = chunk,
                nextAyah = chunks.getOrNull(index + 1)?.firstOrNull(),
                targetAyah = chunkTargetAyah,
                fontScale = fontScale,
                arabicFont = arabicFont,
                tajwidEnabled = tajwidEnabled,
                onAyahTap = onAyahTap,
                onTargetOffset = { offset ->
                    if (chunkTargetAyah != null) targetOffset = offset
                },
            )
        }
        if (showTranslation) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    "Indonesian translation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    surah.ayahs.forEach { ayah ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                toArabicIndic(ayah.number),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                ayah.translation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowingChunk(
    surahNumber: Int,
    ayahs: List<Ayah>,
    nextAyah: Ayah?,
    targetAyah: Int?,
    fontScale: Float,
    arabicFont: FontFamily,
    tajwidEnabled: Boolean,
    onAyahTap: (AyahRef) -> Unit,
    onTargetOffset: (Float?) -> Unit,
) {
    val text = remember(ayahs.first().number, nextAyah?.number, tajwidEnabled) {
        buildMushafText(surahNumber, ayahs, nextAyah, tajwidEnabled)
    }
    val textLayout = remember(ayahs.first().number, tajwidEnabled) {
        mutableStateOf<TextLayoutResult?>(null)
    }
    val targetStart = remember(ayahs.first().number, nextAyah?.number, tajwidEnabled, targetAyah) {
        targetAyah?.let { ayah ->
            text.getStringAnnotations(AYAH_ANNOTATION, 0, text.length)
                .firstOrNull { annotation -> annotation.item == "$surahNumber:$ayah" }
                ?.start
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Mushaf reading passage" }
                .pointerInput(text) {
                    detectTapGestures { position ->
                        val offset = textLayout.value?.getOffsetForPosition(position)
                            ?: return@detectTapGestures
                        text.getStringAnnotations(AYAH_ANNOTATION, offset, offset)
                            .firstOrNull()
                            ?.item
                            ?.split(":")
                            ?.takeIf { it.size == 2 }
                            ?.let { parts ->
                                onAyahTap(
                                    AyahRef(
                                        surah = parts[0].toIntOrNull() ?: return@let,
                                        ayah = parts[1].toIntOrNull() ?: return@let,
                                    ),
                                )
                            }
                    }
                },
            fontFamily = arabicFont,
            fontSize = (27 * fontScale).sp,
            lineHeight = (68 * fontScale).sp,
            style = ArabicReadingTextStyle,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurface,
            onTextLayout = { layout ->
                textLayout.value = layout
                if (targetStart != null) {
                    onTargetOffset(layout.getBoundingBox(targetStart).top)
                }
            },
        )
    }
}

private fun buildMushafText(
    surahNumber: Int,
    ayahs: List<Ayah>,
    nextAyah: Ayah?,
    tajwidEnabled: Boolean,
): AnnotatedString = buildAnnotatedString {
    ayahs.forEachIndexed { index, ayah ->
        val start = length
        append(
            tajwidAnnotatedText(
                text = ayah.arabic.trim(),
                nextText = ayahs.getOrNull(index + 1)?.arabic?.trim()
                    ?: nextAyah?.arabic?.trim(),
                enabled = tajwidEnabled,
            ),
        )
        append(" ")
        append("﴿")
        append(toArabicIndic(ayah.number))
        append("﴾")
        if (index != ayahs.lastIndex) append(" ")
        addStringAnnotation(
            tag = AYAH_ANNOTATION,
            annotation = "$surahNumber:${ayah.number}",
            start = start,
            end = length,
        )
    }
}

private fun toArabicIndic(number: Int): String {
    return number.toString().map { digit ->
        when (digit) {
            '0' -> '٠'
            '1' -> '١'
            '2' -> '٢'
            '3' -> '٣'
            '4' -> '٤'
            '5' -> '٥'
            '6' -> '٦'
            '7' -> '٧'
            '8' -> '٨'
            else -> '٩'
        }
    }.joinToString("")
}

private fun tajwidAnnotatedText(
    text: String,
    nextText: String?,
    enabled: Boolean,
): AnnotatedString {
    if (!enabled) return AnnotatedString(text)
    val spans = TajwidColorizer.spans(text, nextText)
    if (spans.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        var cursor = 0
        spans.forEach { span ->
            if (cursor < span.start) append(text.substring(cursor, span.start))
            withStyle(SpanStyle(color = tajwidColor(span.category))) {
                append(text.substring(span.start, span.end))
            }
            cursor = span.end
        }
        if (cursor < text.length) append(text.substring(cursor))
    }
}

private fun tajwidColor(category: TajwidCategory): Color {
    return when (category) {
        TajwidCategory.MAD -> Color(0xFFE56B6F)
        TajwidCategory.GHUNNAH -> Color(0xFF2EAD72)
        TajwidCategory.IDGHAM -> Color(0xFFFFA24C)
        TajwidCategory.IKHFA -> Color(0xFF9EA8B3)
        TajwidCategory.IQLAB -> Color(0xFFB88CE8)
        TajwidCategory.QALQALAH -> Color(0xFF4F9BFF)
        TajwidCategory.LAM_JALALAH -> Color(0xFF46C4B0)
        TajwidCategory.WAQAF -> Color(0xFFE6B95C)
    }
}

@Composable
private fun AyahCard(
    surah: Surah,
    ayah: Ayah,
    fontScale: Float,
    showTranslation: Boolean,
    arabicFont: FontFamily,
    tajwidEnabled: Boolean,
    nextAyah: Ayah?,
    bookmarked: Boolean,
    onBookmark: () -> Unit,
    onOpen: () -> Unit,
) {
    val arabicText = remember(ayah.arabic, nextAyah?.arabic, tajwidEnabled) {
        tajwidAnnotatedText(
            text = ayah.arabic,
            nextText = nextAyah?.arabic,
            enabled = tajwidEnabled,
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .semantics { contentDescription = "${surah.latinName}, ayah ${ayah.number}" },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            ayah.number.toString(),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                IconButton(onClick = onBookmark) {
                    Icon(
                        imageVector = if (bookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (bookmarked) "Remove bookmark" else "Bookmark ayah",
                        tint = if (bookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = arabicText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    fontFamily = arabicFont,
                    fontSize = (27 * fontScale).sp,
                    lineHeight = (68 * fontScale).sp,
                    style = ArabicReadingTextStyle,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (showTranslation) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = ayah.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    state: QuranUiState,
    viewModel: QuranViewModel,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
    onOpen: (SearchResult) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
    ) {
        CompactTopBar(
            title = "Search",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (state.query.isNotEmpty()) {
                { IconButton(onClick = { viewModel.setQuery("") }) { Icon(Icons.Default.Clear, "Clear") } }
            } else {
                null
            },
            placeholder = { Text("Arabic or Indonesian") },
        )
        if (state.query.isNotBlank()) {
            Text(
                "${state.searchResults.size} results",
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.query.isNotBlank() && state.searchResults.isEmpty()) {
            EmptyState("No ayahs found")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            ) {
                items(
                    state.searchResults,
                    key = { "${it.surah.number}:${it.ayah.number}" },
                ) { result ->
                    SearchResultItem(result, onClick = { onOpen(result) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text("${result.surah.latinName}  ·  ${result.ayah.number}") },
        supportingContent = { Text(result.ayah.translation, maxLines = 2) },
        leadingContent = {
            Text(
                result.surah.arabicName,
                style = MaterialTheme.typography.titleMedium,
            )
        },
    )
    HorizontalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksScreen(
    state: QuranUiState,
    viewModel: QuranViewModel,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
    onOpen: (AyahRef) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        CompactTopBar(
            title = "Saved ayahs",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        val bookmarks = state.bookmarks.mapNotNull { key ->
            val parts = key.split(":")
            val surahNumber = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val ayahNumber = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            val surah = state.surahs.firstOrNull { it.number == surahNumber } ?: return@mapNotNull null
            val ayah = surah.ayahs.firstOrNull { it.number == ayahNumber } ?: return@mapNotNull null
            Triple(AyahRef(surahNumber, ayahNumber), surah, ayah)
        }.sortedWith(compareBy({ it.first.surah }, { it.first.ayah }))

        if (bookmarks.isEmpty()) {
            EmptyState("Bookmarked ayahs will appear here")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(bookmarks, key = { "${it.first.surah}:${it.first.ayah}" }) { item ->
                    val (ref, surah, ayah) = item
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(ref) }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("${surah.latinName}  ·  Ayah ${ayah.number}", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(ayah.translation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { viewModel.toggleBookmark(ref) }) {
                                Icon(Icons.Default.Star, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: QuranUiState,
    viewModel: QuranViewModel,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        CompactTopBar(
            title = "Settings",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        ) {
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Translate, contentDescription = null) },
                    headlineContent = { Text("Show Indonesian translation") },
                    supportingContent = { Text("Keep translations below each ayah") },
                    trailingContent = {
                        Switch(
                            checked = state.showTranslation,
                            onCheckedChange = viewModel::setShowTranslation,
                        )
                    },
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                    headlineContent = { Text("Mushaf flow mode") },
                    supportingContent = {
                        Text("Join ayahs in one RTL passage with Arabic-Indic verse markers")
                    },
                    trailingContent = {
                        Switch(
                            checked = state.flowingMode,
                            onCheckedChange = viewModel::setFlowingMode,
                        )
                    },
                )
            }
            item {
                ListItem(
                    leadingContent = { Text("Aa", style = MaterialTheme.typography.titleMedium) },
                    headlineContent = { Text("Tajweed colors") },
                    supportingContent = { Text("Highlight text-based reading rules") },
                    trailingContent = {
                        Switch(
                            checked = state.tajwidEnabled,
                            onCheckedChange = viewModel::setTajwidEnabled,
                        )
                    },
                )
            }
            if (state.tajwidEnabled) {
                item { TajwidLegend() }
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    headlineContent = { Text("Dark mode") },
                    trailingContent = {
                        Switch(checked = state.darkMode, onCheckedChange = viewModel::setDarkMode)
                    },
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.FormatSize, contentDescription = null) },
                    headlineContent = { Text("Arabic text size") },
                    supportingContent = {
                        Column {
                            Text("${(state.fontScale * 100).toInt()}%")
                            Slider(
                                value = state.fontScale,
                                onValueChange = viewModel::setFontScale,
                                valueRange = 1f..2f,
                                steps = 9,
                            )
                        }
                    },
                )
            }
            item {
                ListItem(
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    headlineContent = { Text("About this app") },
                    supportingContent = {
                        Text(
                            "Offline Quran reader. Arabic text and Indonesian translation are bundled for private reading. " +
                                "Data source: equran.id API. Amiri font: SIL Open Font License.",
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun TajwidLegend() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
        Text(
            "Color legend",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        TajwidLegendRow(
            TajwidCategory.MAD to "Mad",
            TajwidCategory.GHUNNAH to "Ghunnah",
            TajwidCategory.IDGHAM to "Idgham",
            TajwidCategory.IKHFA to "Ikhfa",
        )
        Spacer(Modifier.height(8.dp))
        TajwidLegendRow(
            TajwidCategory.IQLAB to "Iqlab",
            TajwidCategory.QALQALAH to "Qalqalah",
            TajwidCategory.LAM_JALALAH to "Lam Allah",
            TajwidCategory.WAQAF to "Waqaf",
        )
    }
}

@Composable
private fun TajwidLegendRow(vararg entries: Pair<TajwidCategory, String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        entries.forEach { (category, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(tajwidColor(category)),
                )
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AyahPicker(
    surah: Surah,
    selectedAyah: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.heightIn(max = 560.dp)) {
            Text(
                "Select ayah · ${surah.latinName}",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge,
            )
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
                items(surah.ayahs, key = { it.number }) { ayah ->
                    ListItem(
                        modifier = Modifier.clickable { onSelect(ayah.number) },
                        headlineContent = { Text("Ayah ${ayah.number}") },
                        supportingContent = { Text(ayah.translation, maxLines = 1) },
                        leadingContent = {
                            Text(toArabicIndic(ayah.number), style = MaterialTheme.typography.titleMedium)
                        },
                        trailingContent = {
                            if (ayah.number == selectedAyah) {
                                Icon(Icons.Default.Star, contentDescription = "Selected")
                            }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberPicker(
    title: String,
    numbers: IntRange,
    selected: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.heightIn(max = 560.dp)) {
            Text(
                "Select $title",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge,
            )
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
                items(numbers.toList(), key = { it }) { number ->
                    ListItem(
                        modifier = Modifier.clickable { onSelect(number) },
                        headlineContent = { Text("$title $number") },
                        trailingContent = {
                            if (number == selected) {
                                Icon(Icons.Default.Star, contentDescription = "Selected")
                            }
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahPicker(
    surahs: List<Surah>,
    selectedSurah: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.heightIn(max = 560.dp)) {
            Text(
                "Select surah",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge,
            )
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
                items(surahs, key = { it.number }) { surah ->
                    ListItem(
                        modifier = Modifier.clickable { onSelect(surah.number) },
                        headlineContent = { Text("${surah.number}. ${surah.latinName}") },
                        supportingContent = { Text("${surah.meaning}  ·  ${surah.ayahs.size} ayahs") },
                        leadingContent = {
                            Text(surah.arabicName, style = MaterialTheme.typography.titleMedium)
                        },
                        trailingContent = {
                            if (surah.number == selectedSurah) {
                                Icon(Icons.Default.Star, contentDescription = "Selected")
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(contentPadding: androidx.compose.foundation.layout.PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text("Loading Quran…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorState(message: String, contentPadding: androidx.compose.foundation.layout.PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(contentPadding).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
