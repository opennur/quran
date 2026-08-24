package org.opennur.quran.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.opennur.quran.R
import org.opennur.quran.data.Ayah
import org.opennur.quran.data.AyahRef
import org.opennur.quran.data.SearchResult
import org.opennur.quran.data.Surah

private enum class Destination {
    READER,
    SEARCH,
    BOOKMARKS,
    SETTINGS,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranApp(viewModel: QuranViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(Destination.READER.name) }
    var showSurahPicker by rememberSaveable { mutableStateOf(false) }
    val currentDestination = Destination.valueOf(destination)

    QuranTheme(darkMode = state.darkMode) {
        Scaffold(
            topBar = {
                if (currentDestination == Destination.READER) {
                    ReaderTopBar(
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
                    onPickSurah = { showSurahPicker = true },
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
                    onOpen = {
                        viewModel.openAyah(it)
                        destination = Destination.READER.name
                    },
                )

                Destination.SETTINGS -> SettingsScreen(
                    state = state,
                    viewModel = viewModel,
                    contentPadding = paddingValues,
                )
            }
        }
    }

    if (showSurahPicker && state.surahs.isNotEmpty()) {
        SurahPicker(
            surahs = state.surahs,
            selectedSurah = state.selectedSurah,
            onDismiss = { showSurahPicker = false },
            onSelect = {
                viewModel.selectSurah(it)
                showSurahPicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(
    onSearch: () -> Unit,
    onBookmarks: () -> Unit,
    onSettings: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Quran", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Read offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
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
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
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

    val listState = rememberLazyListState()
    val arabicFont = FontFamily(Font(R.font.uthmani))

    LaunchedEffect(state.jumpToken) {
        val target = (state.selectedAyah - 1).coerceIn(0, surah.ayahs.lastIndex)
        listState.animateScrollToItem(target)
    }
    LaunchedEffect(surah.number) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                surah.ayahs.getOrNull(index)?.let {
                    viewModel.updateLastRead(AyahRef(surah.number, it.number))
                }
            }
    }

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

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = 16.dp,
            ),
        ) {
            items(surah.ayahs, key = { it.number }) { ayah ->
                AyahCard(
                    surah = surah,
                    ayah = ayah,
                    fontScale = state.fontScale,
                    showTranslation = state.showTranslation,
                    arabicFont = arabicFont,
                    bookmarked = viewModel.isBookmarked(AyahRef(surah.number, ayah.number)),
                    onBookmark = { viewModel.toggleBookmark(AyahRef(surah.number, ayah.number)) },
                    onOpen = { viewModel.updateLastRead(AyahRef(surah.number, ayah.number)) },
                )
            }
        }
    }
}

@Composable
private fun AyahCard(
    surah: Surah,
    ayah: Ayah,
    fontScale: Float,
    showTranslation: Boolean,
    arabicFont: FontFamily,
    bookmarked: Boolean,
    onBookmark: () -> Unit,
    onOpen: () -> Unit,
) {
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
                    text = ayah.arabic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    fontFamily = arabicFont,
                    fontSize = (27 * fontScale).sp,
                    lineHeight = (62 * fontScale).sp,
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
        CenterAlignedTopAppBar(
            title = { Text("Search") },
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
    onOpen: (AyahRef) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        CenterAlignedTopAppBar(title = { Text("Saved ayahs") })
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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        CenterAlignedTopAppBar(title = { Text("Settings") })
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
