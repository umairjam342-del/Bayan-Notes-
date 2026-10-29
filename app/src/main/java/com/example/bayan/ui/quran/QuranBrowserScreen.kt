package com.example.bayan.ui.quran

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bayan.data.model.Ayah
import com.example.bayan.data.model.Bookmark
import com.example.bayan.data.model.Surah
import com.example.bayan.data.model.SurahOrderMode
import com.example.bayan.data.quran.QuranData
import com.example.bayan.ui.bookmarks.BookmarkEditDialog
import com.example.bayan.ui.bookmarks.SavedBookmarksBottomSheet
import com.example.bayan.util.DownloadState
import com.example.bayan.util.QuranAudioDownloadManager
import com.example.bayan.util.QuranAudioPlayer
import com.example.bayan.util.QuranAudioState
import com.example.bayan.util.SurahDownloadStatus
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranBrowserScreen(
    onOpenNoteCreator: (surahNumber: Int, ayahNumber: Int, arabicSnippet: String, translationSnippet: String) -> Unit,
    onAddBookmark: (surahNumber: Int, ayahNumber: Int, surahName: String) -> Unit,
    bookmarkedAyahs: Set<String> = emptySet(),
    bookmarks: List<Bookmark> = emptyList(),
    onUpdateBookmark: (Bookmark) -> Unit = {},
    onDeleteBookmark: (String) -> Unit = {},
    initialAyahNumber: Int? = null,
    initialSurahNumber: Int? = null,
    onClearInitialTarget: () -> Unit = {},
    onReaderActiveChange: (Boolean) -> Unit = {},
    audioPlayer: QuranAudioPlayer? = null,
    isFullScreenRecitation: Boolean = false,
    onToggleFullScreenRecitation: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val effectiveAudioPlayer = remember(audioPlayer) {
        audioPlayer ?: QuranAudioPlayer.getInstance(context)
    }

    val downloadStatuses by QuranAudioDownloadManager.downloadStatuses.collectAsState()

    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var revelationFilter by remember { mutableStateOf("All") } // All, Meccan, Medinan
    var orderMode by rememberSaveable { mutableStateOf(SurahOrderMode.MUSHAF) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var bookmarkToEdit by remember { mutableStateOf<Bookmark?>(null) }
    var targetAyahToScroll by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            onReaderActiveChange(false)
        }
    }

    LaunchedEffect(selectedSurah) {
        onReaderActiveChange(selectedSurah != null)
    }

    LaunchedEffect(initialSurahNumber, initialAyahNumber) {
        if (initialSurahNumber != null && initialSurahNumber in 1..114) {
            selectedSurah = QuranData.surahs.firstOrNull { it.number == initialSurahNumber }
            targetAyahToScroll = initialAyahNumber
            onClearInitialTarget()
        }
    }

    if (selectedSurah != null) {
        BackHandler {
            selectedSurah = null
            onReaderActiveChange(false)
        }
        SurahReaderView(
            surah = selectedSurah!!,
            audioPlayer = effectiveAudioPlayer,
            onBack = {
                selectedSurah = null
                onReaderActiveChange(false)
            },
            onSelectSurah = { newSurah ->
                selectedSurah = newSurah
            },
            onCreateNote = onOpenNoteCreator,
            onBookmark = onAddBookmark,
            bookmarkedAyahs = bookmarkedAyahs,
            bookmarks = bookmarks,
            onEditBookmark = { bookmarkToEdit = it },
            onOpenBookmarksSheet = { showBookmarksSheet = true },
            initialTargetAyah = targetAyahToScroll,
            onClearTargetAyah = { targetAyahToScroll = null },
            isFullScreenRecitation = isFullScreenRecitation,
            onToggleFullScreenRecitation = onToggleFullScreenRecitation
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & Filter header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surah_search_input"),
                    placeholder = { Text("Search Surah by name, meaning, or number...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Surah Order Mode Tabs: Mushaf Order vs Revelation Order
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isMushaf = orderMode == SurahOrderMode.MUSHAF
                        Surface(
                            onClick = { orderMode = SurahOrderMode.MUSHAF },
                            shape = RoundedCornerShape(9.dp),
                            color = if (isMushaf) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (isMushaf) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("surah_order_tab_mushaf")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isMushaf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mushaf Order",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isMushaf) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isMushaf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isRevelation = orderMode == SurahOrderMode.REVELATION
                        Surface(
                            onClick = { orderMode = SurahOrderMode.REVELATION },
                            shape = RoundedCornerShape(9.dp),
                            color = if (isRevelation) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (isRevelation) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("surah_order_tab_revelation")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isRevelation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Revelation Order (نزول)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isRevelation) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isRevelation) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("All", "Meccan", "Medinan").forEach { filter ->
                            FilterChip(
                                selected = revelationFilter == filter,
                                onClick = { revelationFilter = filter },
                                label = { Text(filter) }
                            )
                        }
                    }

                    // Quick Saved Bookmarks Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier
                            .clickable { showBookmarksSheet = true }
                            .testTag("view_saved_bookmarks_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bookmarks (${bookmarks.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Revelation Order Info Banner
                if (orderMode == SurahOrderMode.REVELATION) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("revelation_order_info_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Chronological sequence: #1 Al-'Alaq → #114 An-Nasr (Tanzil standard)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            val filteredSurahs = remember(searchQuery, revelationFilter, orderMode) {
                val baseList = when (orderMode) {
                    SurahOrderMode.MUSHAF -> QuranData.surahs
                    SurahOrderMode.REVELATION -> QuranData.surahsByRevelation
                }
                baseList.filter { s ->
                    val matchesSearch = searchQuery.isBlank() ||
                        s.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                        s.nameTransliterated.contains(searchQuery, ignoreCase = true) ||
                        s.nameArabic.contains(searchQuery) ||
                        s.number.toString() == searchQuery.trim() ||
                        s.revelationOrder.toString() == searchQuery.trim()

                    val matchesFilter = when (revelationFilter) {
                        "Meccan" -> s.revelationType.equals("Meccan", ignoreCase = true)
                        "Medinan" -> s.revelationType.equals("Medinan", ignoreCase = true)
                        else -> true
                    }
                    matchesSearch && matchesFilter
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    val status = downloadStatuses[surah.number]
                    SurahListItemCard(
                        surah = surah,
                        orderMode = orderMode,
                        downloadStatus = status,
                        onDownloadClick = {
                            if (status?.state == DownloadState.DOWNLOADING) {
                                QuranAudioDownloadManager.cancelDownload(surah.number)
                            } else if (status?.state == DownloadState.DOWNLOADED) {
                                selectedSurah = surah
                            } else {
                                QuranAudioDownloadManager.startSurahDownload(context, surah.number, surah.versesCount)
                            }
                        },
                        onClick = { selectedSurah = surah }
                    )
                }
            }
        }
    }

    // Saved Bookmarks Sheet
    if (showBookmarksSheet) {
        SavedBookmarksBottomSheet(
            bookmarks = bookmarks,
            onDismiss = { showBookmarksSheet = false },
            onNavigateToAyah = { sNum, aNum ->
                showBookmarksSheet = false
                selectedSurah = QuranData.surahs.firstOrNull { it.number == sNum }
                targetAyahToScroll = aNum
            },
            onEditBookmark = { b ->
                bookmarkToEdit = b
            },
            onDeleteBookmark = { bId ->
                onDeleteBookmark(bId)
            }
        )
    }

    // Interactive Edit Bookmark Dialog
    bookmarkToEdit?.let { targetBm ->
        BookmarkEditDialog(
            bookmark = targetBm,
            onDismiss = { bookmarkToEdit = null },
            onSave = { updated ->
                onUpdateBookmark(updated)
                bookmarkToEdit = null
            },
            onDelete = { bId ->
                onDeleteBookmark(bId)
                bookmarkToEdit = null
            }
        )
    }
}

@Composable
fun SurahListItemCard(
    surah: Surah,
    orderMode: SurahOrderMode = SurahOrderMode.MUSHAF,
    downloadStatus: SurahDownloadStatus? = null,
    onDownloadClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val isDownloaded = downloadStatus?.state == DownloadState.DOWNLOADED
    val isDownloading = downloadStatus?.state == DownloadState.DOWNLOADING

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("surah_item_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Star-Badge (reflecting order mode)
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (orderMode == SurahOrderMode.REVELATION)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (orderMode == SurahOrderMode.REVELATION)
                                "${surah.revelationOrder}"
                            else
                                "${surah.number}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (orderMode == SurahOrderMode.REVELATION)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (orderMode == SurahOrderMode.REVELATION) {
                            Text(
                                text = "Rev",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = surah.nameTransliterated,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (surah.revelationType == "Meccan")
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = surah.revelationType,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = if (orderMode == SurahOrderMode.REVELATION)
                        "Surah #${surah.number} • ${surah.nameEnglish} • ${surah.versesCount} Verses"
                    else
                        "${surah.nameEnglish} • ${surah.versesCount} Verses • Rev. #${surah.revelationOrder}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arabic Name & Offline Download Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = surah.nameArabic,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${((downloadStatus?.progressFraction ?: 0f) * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (isDownloaded) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = "Offline audio downloaded",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Offline ⚡",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDownloadClick ?: {},
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("download_surah_${surah.number}_button")
                    ) {
                        if (isDownloaded) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = "Surah ${surah.number} downloaded offline",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else if (isDownloading) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancel download for Surah ${surah.number}",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Download Surah ${surah.number} recitation for offline listening",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class QuranViewMode {
    VERSE,
    MUSHAF
}

fun toArabicDigits(number: Int): String {
    val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { if (it in '0'..'9') digits[it - '0'] else it }.joinToString("")
}

fun formatAyahEndMarker(ayahNumber: Int): String {
    return " ﴿${toArabicDigits(ayahNumber)}﴾ "
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderView(
    surah: Surah,
    audioPlayer: QuranAudioPlayer,
    onBack: () -> Unit,
    onSelectSurah: (Surah) -> Unit,
    onCreateNote: (surahNumber: Int, ayahNumber: Int, arabicSnippet: String, translationSnippet: String) -> Unit,
    onBookmark: (surahNumber: Int, ayahNumber: Int, surahName: String) -> Unit,
    bookmarkedAyahs: Set<String>,
    bookmarks: List<Bookmark> = emptyList(),
    onEditBookmark: (Bookmark) -> Unit = {},
    onOpenBookmarksSheet: () -> Unit = {},
    initialTargetAyah: Int? = null,
    onClearTargetAyah: () -> Unit = {},
    isFullScreenRecitation: Boolean = false,
    onToggleFullScreenRecitation: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val audioState by audioPlayer.audioState.collectAsState()

    // Mode switch: Verse View (آیات) vs Mushaf View (مصحف)
    var viewMode by rememberSaveable { mutableStateOf(QuranViewMode.VERSE) }
    var selectedMushafAyah by remember { mutableStateOf<Ayah?>(null) }
    var showSurahIndexSheet by rememberSaveable { mutableStateOf(false) }

    // Load authentic complete ayahs for this Surah from bundled JSON asset store with in-memory caching
    val allAyahs = remember(surah.number) {
        QuranData.getAyahsForSurah(context, surah.number)
    }

    var inSurahSearchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var arabicFontSize by remember { mutableFloatStateOf(26f) }
    var showTranslation by remember { mutableStateOf(true) }

    // Highlighted verse key (e.g., "2:255")
    var highlightedAyahKey by remember { mutableStateOf<String?>(null) }

    // Auto-scroll when navigating to specific Ayah from a bookmark or deep-link
    LaunchedEffect(initialTargetAyah) {
        if (initialTargetAyah != null && initialTargetAyah > 0) {
            val index = allAyahs.indexOfFirst { it.ayahNumber == initialTargetAyah }
            if (index >= 0) {
                val targetPosition = index + if (surah.versesCount > 15) 2 else 1
                try {
                    listState.animateScrollToItem(targetPosition.coerceAtLeast(0))
                    highlightedAyahKey = "${surah.number}:$initialTargetAyah"
                    selectedMushafAyah = allAyahs[index]
                } catch (_: Exception) {}
            }
            onClearTargetAyah()
        }
    }

    // Auto-scroll to currently reciting Ayah
    LaunchedEffect(audioState.currentAyah, audioState.currentSurah) {
        if (audioState.currentSurah == surah.number) {
            audioState.currentAyah?.let { ayahNum ->
                val index = allAyahs.indexOfFirst { it.ayahNumber == ayahNum }
                if (index >= 0) {
                    val targetPosition = index + if (surah.versesCount > 15) 2 else 1
                    try {
                        listState.animateScrollToItem(targetPosition.coerceAtLeast(0))
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // Filter ayahs within current Surah
    val displayAyahs = remember(allAyahs, inSurahSearchQuery) {
        if (inSurahSearchQuery.isBlank()) allAyahs
        else allAyahs.filter {
            it.arabicText.contains(inSurahSearchQuery) ||
            it.translation.contains(inSurahSearchQuery, ignoreCase = true) ||
            it.ayahNumber.toString() == inSurahSearchQuery.trim()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clickable { showSurahIndexSheet = true }
                            .testTag("reader_title_index_trigger")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${surah.number}. ${surah.nameTransliterated}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Open Surah Index",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Rev. #${surah.revelationOrder} (${surah.revelationType}) • ${surah.versesCount} Verses • اردو ترجمہ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Surahs")
                    }
                },
                actions = {
                    // Quick Surah Index Sheet Trigger (Mushaf & Revelation Order)
                    IconButton(
                        onClick = { showSurahIndexSheet = true },
                        modifier = Modifier.testTag("open_surah_index_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "Surah Index (Mushaf / Revelation Order)",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Quick View Mode Toggle (Mushaf View <-> Verse View)
                    IconButton(
                        onClick = {
                            viewMode = if (viewMode == QuranViewMode.VERSE) QuranViewMode.MUSHAF else QuranViewMode.VERSE
                        },
                        modifier = Modifier.testTag("quick_view_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (viewMode == QuranViewMode.VERSE) Icons.Default.MenuBook else Icons.Default.ViewAgenda,
                            contentDescription = if (viewMode == QuranViewMode.VERSE) "Switch to Mushaf View" else "Switch to Verse View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // In-Surah Search toggle
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) inSurahSearchQuery = ""
                    }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search in Surah",
                            tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Font size cycle (22 -> 26 -> 30 -> 22)
                    IconButton(onClick = {
                        arabicFontSize = when (arabicFontSize) {
                            22f -> 26f
                            26f -> 30f
                            else -> 22f
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Change Arabic font size",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Translation toggle
                    IconButton(onClick = { showTranslation = !showTranslation }) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Toggle Urdu Translation (اردو ترجمہ)",
                            tint = if (showTranslation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    // View Saved Bookmarks Button
                    IconButton(
                        onClick = onOpenBookmarksSheet,
                        modifier = Modifier.testTag("reader_saved_bookmarks_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Saved Bookmarks",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Full Screen Recitation mode toggle button
                    IconButton(
                        onClick = { onToggleFullScreenRecitation(!isFullScreenRecitation) },
                        modifier = Modifier.testTag("surah_fullscreen_recitation_button")
                    ) {
                        Icon(
                            imageVector = if (isFullScreenRecitation) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullScreenRecitation) "Exit Full Screen Recitation" else "Enter Full Screen Recitation",
                            tint = if (isFullScreenRecitation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Inline in-surah search bar
            AnimatedVisibility(visible = isSearchActive) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inSurahSearchQuery,
                            onValueChange = { inSurahSearchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("in_surah_search_input"),
                            placeholder = { Text("Search verse number, Arabic text or Urdu translation (اردو ترجمہ)...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        if (inSurahSearchQuery.isNotBlank()) {
                            IconButton(onClick = { inSurahSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    }
                }
            }

            // View Mode Selector Segmented Bar (Verse View vs Mushaf View)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_view_mode_toggle")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = viewMode == QuranViewMode.VERSE,
                        onClick = { viewMode = QuranViewMode.VERSE },
                        label = { Text("Verse View (آیات)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ViewAgenda,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("mode_verse_view")
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    FilterChip(
                        selected = viewMode == QuranViewMode.MUSHAF,
                        onClick = { viewMode = QuranViewMode.MUSHAF },
                        label = { Text("Mushaf View (مصحف)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("mode_mushaf_view")
                    )
                }
            }

            // Audio Stream / Offline Download Bar for this Surah
            val readerDownloadStatuses by QuranAudioDownloadManager.downloadStatuses.collectAsState()
            val currentSurahDownloadStatus = readerDownloadStatuses[surah.number]

            LaunchedEffect(surah.number) {
                QuranAudioDownloadManager.syncSurahStatus(context, surah.number, surah.versesCount)
            }

            SurahAudioDownloadBar(
                surah = surah,
                downloadStatus = currentSurahDownloadStatus,
                onDownload = {
                    QuranAudioDownloadManager.startSurahDownload(context, surah.number, surah.versesCount)
                },
                onCancelDownload = {
                    QuranAudioDownloadManager.cancelDownload(surah.number)
                },
                onDeleteOfflineAudio = {
                    QuranAudioDownloadManager.deleteSurahAudio(context, surah.number)
                },
                onPlayFullSurah = {
                    audioPlayer.playVerse(surah.number, 1, surah.nameTransliterated, surah.versesCount)
                }
            )

            if (viewMode == QuranViewMode.VERSE) {
                // Highlighted verse action banner (when a verse is selected)
                AnimatedVisibility(
                visible = highlightedAyahKey != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                highlightedAyahKey?.let { key ->
                    val parts = key.split(":")
                    if (parts.size == 2) {
                        val sNum = parts[0].toIntOrNull() ?: surah.number
                        val aNum = parts[1].toIntOrNull() ?: 1
                        val isThisVersePlaying = audioState.isVersePlaying(sNum, aNum)
                        val isThisVerseLoading = audioState.isVerseLoading(sNum, aNum)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Selected Ayah $sNum:$aNum",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Recitation: Mishary Alafasy",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = {
                                            audioPlayer.togglePlayPause(sNum, aNum, surah.nameTransliterated, surah.versesCount)
                                        },
                                        modifier = Modifier.testTag("highlighted_verse_recite_button")
                                    ) {
                                        if (isThisVerseLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Loading...", style = MaterialTheme.typography.labelMedium)
                                        } else if (isThisVersePlaying) {
                                            Icon(
                                                Icons.Default.Pause,
                                                contentDescription = "Pause",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Pause", style = MaterialTheme.typography.labelMedium)
                                        } else {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "Listen",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Listen Now", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }

                                    IconButton(
                                        onClick = { highlightedAyahKey = null },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss selection",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (audioState.isActive) 150.dp else 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ornate Surah Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = surah.nameArabic,
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Surah ${surah.nameTransliterated} (${surah.nameEnglish})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "${surah.revelationType} Revelation • ${surah.versesCount} Ayahs • اردو ترجمہ: مولانا فتح محمد جالندھری • تلاوت: مشاری العفاسی",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            )

                            if (surah.number != 9) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .padding(vertical = 14.dp)
                                        .fillMaxWidth(0.5f),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Quick Jump to Verse chips for long Surahs
                if (surah.versesCount > 15 && inSurahSearchQuery.isBlank()) {
                    item {
                        val step = when {
                            surah.versesCount > 200 -> 50
                            surah.versesCount > 100 -> 25
                            surah.versesCount > 50 -> 15
                            else -> 10
                        }
                        val jumpPoints = (1..surah.versesCount step step).toList()

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Jump to Ayah",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                jumpPoints.forEach { targetAyah ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.clickable {
                                            coroutineScope.launch {
                                                val index = (targetAyah - 1).coerceAtLeast(0) + 1
                                                listState.animateScrollToItem(index)
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "$targetAyah",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Complete Verse Items
                items(displayAyahs, key = { "${it.surahNumber}:${it.ayahNumber}" }) { ayah ->
                    val ayahKey = "${ayah.surahNumber}:${ayah.ayahNumber}"
                    val isBookmarked = bookmarkedAyahs.contains(ayahKey)
                    val isHighlighted = highlightedAyahKey == ayahKey
                    val isCurrentlyReciting = audioState.isVersePlaying(ayah.surahNumber, ayah.ayahNumber)
                    val isCurrentlyLoading = audioState.isVerseLoading(ayah.surahNumber, ayah.ayahNumber)
                    val existingBookmark = bookmarks.find { it.surahNumber.toInt() == ayah.surahNumber && it.ayahNumber.toInt() == ayah.ayahNumber }

                    AyahCard(
                        ayah = ayah,
                        isBookmarked = isBookmarked,
                        isHighlighted = isHighlighted,
                        isCurrentlyReciting = isCurrentlyReciting,
                        isCurrentlyLoading = isCurrentlyLoading,
                        arabicFontSize = arabicFontSize,
                        showTranslation = showTranslation,
                        bookmarkNote = existingBookmark?.note ?: "",
                        onCardClick = {
                            highlightedAyahKey = if (highlightedAyahKey == ayahKey) null else ayahKey
                        },
                        onPlayRecitation = {
                            highlightedAyahKey = ayahKey
                            audioPlayer.togglePlayPause(
                                ayah.surahNumber,
                                ayah.ayahNumber,
                                surah.nameTransliterated,
                                surah.versesCount
                            )
                        },
                        onCreateNote = {
                            onCreateNote(ayah.surahNumber, ayah.ayahNumber, ayah.arabicText, ayah.translation)
                        },
                        onBookmark = {
                            onBookmark(ayah.surahNumber, ayah.ayahNumber, surah.nameTransliterated)
                        },
                        onEditBookmark = if (existingBookmark != null) { { onEditBookmark(existingBookmark) } } else null
                    )
                }

                // Surah Navigation Footer
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (surah.number > 1) {
                                val prevSurah = QuranData.surahs[surah.number - 2]
                                TextButton(
                                    onClick = { onSelectSurah(prevSurah) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = prevSurah.nameTransliterated,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            TextButton(
                                onClick = { showSurahIndexSheet = true },
                                modifier = Modifier.testTag("reader_bottom_index_button")
                            ) {
                                Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Index", style = MaterialTheme.typography.labelMedium)
                            }

                            if (surah.number < 114) {
                                val nextSurah = QuranData.surahs[surah.number]
                                TextButton(
                                    onClick = { onSelectSurah(nextSurah) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = nextSurah.nameTransliterated,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            MushafPageView(
                surah = surah,
                ayahs = displayAyahs,
                audioState = audioState,
                selectedAyah = selectedMushafAyah,
                onSelectAyah = { ayah ->
                    selectedMushafAyah = if (selectedMushafAyah?.ayahNumber == ayah.ayahNumber) null else ayah
                },
                arabicFontSize = arabicFontSize,
                showTranslation = showTranslation,
                isBookmarked = { ayah ->
                    bookmarkedAyahs.contains("${ayah.surahNumber}:${ayah.ayahNumber}")
                },
                onBookmark = { ayah ->
                    onBookmark(ayah.surahNumber, ayah.ayahNumber, surah.nameTransliterated)
                },
                onPlayAyah = { ayah ->
                    audioPlayer.togglePlayPause(
                        ayah.surahNumber,
                        ayah.ayahNumber,
                        surah.nameTransliterated,
                        surah.versesCount
                    )
                },
                onCreateNote = { ayah ->
                    onCreateNote(ayah.surahNumber, ayah.ayahNumber, ayah.arabicText, ayah.translation)
                },
                onDismissInspection = { selectedMushafAyah = null },
                onPreviousSurah = if (surah.number > 1) { { onSelectSurah(QuranData.surahs[surah.number - 2]) } } else null,
                onNextSurah = if (surah.number < 114) { { onSelectSurah(QuranData.surahs[surah.number]) } } else null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }

        // Floating Bottom Audio Player Bar for Mishary Alafasy
        AnimatedVisibility(
            visible = audioState.isActive,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            MisharyAudioPlayerBar(
                state = audioState,
                onPlayPause = {
                    if (audioState.isPlaying) {
                        audioPlayer.pause()
                    } else {
                        audioPlayer.resume()
                    }
                },
                onPrevious = { audioPlayer.previousAyah() },
                onNext = { audioPlayer.nextAyah() },
                onToggleAutoAdvance = { audioPlayer.toggleAutoAdvance() },
                onToggleSkipSilenceGap = { audioPlayer.toggleSkipSilenceGap() },
                onIncreaseSpeed = { audioPlayer.increaseSpeed() },
                onDecreaseSpeed = { audioPlayer.decreaseSpeed() },
                onSetSpeed = { speed -> audioPlayer.setPlaybackSpeed(speed) },
                onToggleFullScreen = { onToggleFullScreenRecitation(!isFullScreenRecitation) },
                onClose = { audioPlayer.stop() }
            )
        }

        // Error message banner if streaming fails
        if (audioState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (audioState.isActive) 80.dp else 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Snackbar(
                    action = {
                        TextButton(onClick = { audioPlayer.stop() }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.onError)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(audioState.errorMessage!!)
                }
            }
        }

        // Surah Index Bottom Sheet (supports Mushaf Order & Revelation Order)
        if (showSurahIndexSheet) {
            SurahIndexBottomSheet(
                currentSurahNumber = surah.number,
                onDismiss = { showSurahIndexSheet = false },
                onSelectSurah = { newSurah ->
                    showSurahIndexSheet = false
                    onSelectSurah(newSurah)
                }
            )
        }
    }
}

/**
 * Audio Stream or Offline Download Controller for each Surah tab.
 */
@Composable
fun SurahAudioDownloadBar(
    surah: Surah,
    downloadStatus: SurahDownloadStatus?,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onDeleteOfflineAudio: () -> Unit,
    onPlayFullSurah: () -> Unit
) {
    val isDownloaded = downloadStatus?.state == DownloadState.DOWNLOADED
    val isDownloading = downloadStatus?.state == DownloadState.DOWNLOADING
    val isError = downloadStatus?.state == DownloadState.ERROR

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.Headphones,
                        contentDescription = null,
                        tint = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mishary Alafasy Recitation",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isDownloaded) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Offline Ready ⚡",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Stream 📶",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isDownloaded) "Zero-delay local audio (${surah.versesCount} verses stored)"
                                   else if (isDownloading) "Downloading (${downloadStatus?.downloadedAyahs ?: 0}/${surah.versesCount} verses - ${((downloadStatus?.progressFraction ?: 0f) * 100).toInt()}%)"
                                   else if (isError) "Download issue: ${downloadStatus?.errorMessage ?: "Connection error"}"
                                   else "Stream online or download for offline recitation",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDownloading) {
                        OutlinedButton(
                            onClick = onCancelDownload,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (isDownloaded) {
                        TextButton(
                            onClick = onPlayFullSurah,
                            modifier = Modifier.testTag("play_full_surah_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Play", style = MaterialTheme.typography.labelSmall)
                        }

                        IconButton(
                            onClick = onDeleteOfflineAudio,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("delete_surah_audio_button")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Offline Audio for Surah ${surah.number}",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onDownload,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("surah_download_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (isDownloading) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { downloadStatus?.progressFraction ?: 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

/**
 * Dedicated Floating Bottom Audio Player Bar displaying Mishary Alafasy Recitation controls.
 * Features:
 *  - Recitation speed adjustments (0.75x to 2.0x) with steppers and chips
 *  - Gapless silence skipping toggle
 *  - Offline vs online streaming status indicator
 */
@Composable
fun MisharyAudioPlayerBar(
    state: QuranAudioState,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleAutoAdvance: () -> Unit,
    onToggleSkipSilenceGap: () -> Unit,
    onIncreaseSpeed: () -> Unit = {},
    onDecreaseSpeed: () -> Unit = {},
    onSetSpeed: (Float) -> Unit = {},
    onToggleFullScreen: () -> Unit = {},
    onClose: () -> Unit
) {
    var showSpeedDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("mishary_audio_player_bar"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Sleek real-time progress indicator at the very top edge
            if (state.progressFraction > 0f) {
                LinearProgressIndicator(
                    progress = { state.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Compact Avatar & Info Column
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (state.isPlaying) {
                                Icon(
                                    Icons.Default.GraphicEq,
                                    contentDescription = "Playing",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Headphones,
                                    contentDescription = "Audio Recitation",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Surah ${state.surahName.ifBlank { "Quran" }} • Ayah ${state.currentAyah ?: 1}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = "Mishary",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Compact Playback Speed pill button
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .testTag("playback_speed_button")
                                    .clickable { showSpeedDialog = true }
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", state.playbackSpeed).trimEnd('0').trimEnd('.')}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            // Compact Skip silence gap (Gapless) toggle
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (state.skipSilenceGap) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .testTag("skip_silence_gap_toggle")
                                    .clickable(onClick = onToggleSkipSilenceGap)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = if (state.skipSilenceGap) "Gapless ON" else "Gapless OFF",
                                        tint = if (state.skipSilenceGap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (state.skipSilenceGap) "Gapless" else "Gapped",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (state.skipSilenceGap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            if (state.isOfflineAudio) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "⚡Offline",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right: Compact Playback Action Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Previous verse
                    IconButton(
                        onClick = onPrevious,
                        enabled = (state.currentAyah ?: 1) > 1 || (state.currentSurah ?: 1) > 1,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous Ayah",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Play / Pause button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    ) {
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("mishary_play_pause_button")
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else if (state.isPlaying) {
                                Icon(
                                    Icons.Default.Pause,
                                    contentDescription = "Pause Recitation",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Resume Recitation",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Next verse
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next Ayah",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Full screen recitation mode toggle
                    IconButton(
                        onClick = onToggleFullScreen,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("fullscreen_recitation_toggle_button")
                    ) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = "Full Screen Recitation",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close audio bar
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("mishary_close_audio_button")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop & Close",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }

    // Interactive Playback Speed Selector Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            modifier = Modifier.testTag("speed_selection_dialog"),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recitation Speed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Adjust playback speed for Sheikh Mishary Alafasy's recitation. Natural pitch is preserved at all speeds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stepper Row (- / Speed / +)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = onDecreaseSpeed,
                            enabled = state.playbackSpeed > 0.75f,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("decrease_speed_button")
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease Recitation Speed",
                                tint = if (state.playbackSpeed > 0.75f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.2f", state.playbackSpeed).trimEnd('0').trimEnd('.')}x",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = onIncreaseSpeed,
                            enabled = state.playbackSpeed < 2.0f,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("increase_speed_button")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase Recitation Speed",
                                tint = if (state.playbackSpeed < 2.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preset speed chips (0.75x, 1.0x, 1.25x, 1.5x, 1.75x, 2.0x)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                            val isSelected = kotlin.math.abs(state.playbackSpeed - speed) < 0.05f
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetSpeed(speed) },
                                label = {
                                    Text(
                                        text = if (speed == 1.0f) "1.0x" else "${speed}x",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("speed_chip_${speed}x")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

/**
 * Authentic continuous Quranic Mushaf page view with decorative frame,
 * calligraphy Bismillah, flowing Arabic script, ornamental verse markers,
 * real-time recitation tracking with auto-scroll, and interactive verse inspection.
 */
@Composable
fun MushafPageView(
    surah: Surah,
    ayahs: List<Ayah>,
    audioState: QuranAudioState,
    selectedAyah: Ayah?,
    onSelectAyah: (Ayah) -> Unit,
    arabicFontSize: Float,
    showTranslation: Boolean,
    isBookmarked: (Ayah) -> Boolean,
    onBookmark: (Ayah) -> Unit,
    onPlayAyah: (Ayah) -> Unit,
    onCreateNote: (Ayah) -> Unit,
    onDismissInspection: () -> Unit,
    onPreviousSurah: (() -> Unit)?,
    onNextSurah: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val mushafScrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Auto-scroll Mushaf to currently reciting ayah
    LaunchedEffect(audioState.currentAyah, audioState.currentSurah) {
        if (audioState.currentSurah == surah.number && audioState.currentAyah != null) {
            val ayahIndex = ayahs.indexOfFirst { it.ayahNumber == audioState.currentAyah }
            if (ayahIndex >= 0 && ayahs.isNotEmpty()) {
                val approximateFraction = ayahIndex.toFloat() / ayahs.size.toFloat()
                val targetScroll = (mushafScrollState.maxValue * approximateFraction).toInt()
                try {
                    mushafScrollState.animateScrollTo(targetScroll)
                } catch (_: Exception) {}
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(mushafScrollState)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = if (selectedAyah != null || audioState.isActive) 230.dp else 100.dp
                )
                .testTag("mushaf_page_container"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mushaf Ornate Page Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mushaf_page_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Traditional Islamic Decorative Surah Frame
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "سُورَةُ ${surah.nameArabic}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Surah ${surah.nameTransliterated} (${surah.nameEnglish}) • ${surah.versesCount} آیات • ${if (surah.revelationType == "Meccan") "مَکِّیَّہ" else "مَدَنِیَّہ"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Bismillah Calligraphy (for all Surahs except At-Tawbah)
                    if (surah.number != 9) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .width(140.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                        }
                    }

                    // Flowing, Continuous Quranic Script with Highlight Spans
                    val annotatedMushafText = remember(
                        ayahs,
                        audioState.currentAyah,
                        audioState.currentSurah,
                        selectedAyah?.ayahNumber,
                        arabicFontSize
                    ) {
                        buildAnnotatedString {
                            ayahs.forEach { ayah ->
                                val isReciting = audioState.isVersePlaying(ayah.surahNumber, ayah.ayahNumber)
                                val isSelected = selectedAyah?.ayahNumber == ayah.ayahNumber

                                pushStringAnnotation(tag = "AYAH", annotation = "${ayah.ayahNumber}")
                                withStyle(
                                    SpanStyle(
                                        background = when {
                                            isReciting -> primaryColor.copy(alpha = 0.25f)
                                            isSelected -> tertiaryColor.copy(alpha = 0.20f)
                                            else -> Color.Transparent
                                        },
                                        color = when {
                                            isReciting -> primaryColor
                                            isSelected -> tertiaryColor
                                            else -> onSurfaceColor
                                        },
                                        fontWeight = if (isReciting || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = arabicFontSize.sp
                                    )
                                ) {
                                    append(ayah.arabicText)
                                    append(formatAyahEndMarker(ayah.ayahNumber))
                                }
                                pop()
                            }
                        }
                    }

                    ClickableText(
                        text = annotatedMushafText,
                        style = TextStyle(
                            fontSize = arabicFontSize.sp,
                            lineHeight = (arabicFontSize * 2.2f).sp,
                            textAlign = TextAlign.Justify,
                            textDirection = TextDirection.Rtl,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                            .testTag("mushaf_continuous_text"),
                        onClick = { offset ->
                            annotatedMushafText.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    val aNum = annotation.item.toIntOrNull()
                                    val tappedAyah = ayahs.find { it.ayahNumber == aNum }
                                    if (tappedAyah != null) {
                                        onSelectAyah(tappedAyah)
                                    }
                                }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation between Surahs at bottom of Mushaf
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onPreviousSurah != null) {
                    TextButton(onClick = onPreviousSurah) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous Surah")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (onNextSurah != null) {
                    TextButton(onClick = onNextSurah) {
                        Text("Next Surah")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Floating Inspection Card for the Selected / Reciting Ayah in Mushaf View
        AnimatedVisibility(
            visible = selectedAyah != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = if (audioState.isActive) 120.dp else 16.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedAyah?.let { ayah ->
                val isReciting = audioState.isVersePlaying(ayah.surahNumber, ayah.ayahNumber)
                val isBookmarkedAyah = isBookmarked(ayah)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mushaf_selected_ayah_sheet"),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${ayah.ayahNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ayah ${ayah.ayahNumber} • سورة ${surah.nameArabic}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (isReciting) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• Reciting",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismissInspection,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Urdu Translation
                        if (showTranslation) {
                            Text(
                                text = ayah.translation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Action Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Listen / Recitation Button
                            TextButton(
                                onClick = { onPlayAyah(ayah) },
                                modifier = Modifier.testTag("mushaf_play_ayah_button")
                            ) {
                                Icon(
                                    if (isReciting) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isReciting) "Pause" else "Recite")
                            }

                            // Bookmark Button
                            IconButton(
                                onClick = { onBookmark(ayah) },
                                modifier = Modifier.testTag("mushaf_bookmark_button")
                            ) {
                                Icon(
                                    if (isBookmarkedAyah) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarkedAyah) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }

                            // Reflect / Note Button
                            IconButton(
                                onClick = { onCreateNote(ayah) },
                                modifier = Modifier.testTag("mushaf_note_button")
                            ) {
                                Icon(
                                    Icons.Default.EditNote,
                                    contentDescription = "Create Study Note",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    isBookmarked: Boolean,
    isHighlighted: Boolean = false,
    isCurrentlyReciting: Boolean = false,
    isCurrentlyLoading: Boolean = false,
    arabicFontSize: Float = 26f,
    showTranslation: Boolean = true,
    bookmarkNote: String = "",
    onCardClick: () -> Unit = {},
    onPlayRecitation: () -> Unit = {},
    onCreateNote: () -> Unit,
    onBookmark: () -> Unit,
    onEditBookmark: (() -> Unit)? = null
) {
    var showTafsir by remember { mutableStateOf(false) }

    val borderStroke = when {
        isCurrentlyReciting -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        isHighlighted -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    val containerColor = when {
        isCurrentlyReciting -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
        isHighlighted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.20f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("ayah_card_${ayah.surahNumber}_${ayah.ayahNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted || isCurrentlyReciting) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Verse Number, Status & Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrentlyReciting) MaterialTheme.colorScheme.primary
                                else if (isHighlighted) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${ayah.surahNumber}:${ayah.ayahNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentlyReciting) MaterialTheme.colorScheme.onPrimary
                                    else if (isHighlighted) MaterialTheme.colorScheme.onSecondary
                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (isCurrentlyReciting) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reciting (Mishary Alafasy)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (isHighlighted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Selected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Recitation Button (Mishary Alafasy)
                    Surface(
                        shape = CircleShape,
                        color = if (isCurrentlyReciting) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        IconButton(
                            onClick = onPlayRecitation,
                            modifier = Modifier.testTag("play_recitation_${ayah.surahNumber}_${ayah.ayahNumber}")
                        ) {
                            if (isCurrentlyLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (isCurrentlyReciting) {
                                Icon(
                                    Icons.Default.Pause,
                                    contentDescription = "Pause Recitation by Mishary Alafasy",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Listen to Mishary Alafasy",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Bookmark Button
                    IconButton(
                        onClick = onBookmark,
                        modifier = Modifier.size(36.dp).testTag("bookmark_button_${ayah.surahNumber}_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Ayah",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                        )
                    }

                    if (isBookmarked && onEditBookmark != null) {
                        IconButton(
                            onClick = onEditBookmark,
                            modifier = Modifier.size(36.dp).testTag("edit_bookmark_button_${ayah.surahNumber}_${ayah.ayahNumber}")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Bookmark Reflection",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Add Note Button
                    TextButton(onClick = onCreateNote) {
                        Icon(
                            Icons.Default.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Note", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Text
            Text(
                text = ayah.arabicText,
                fontSize = arabicFontSize.sp,
                lineHeight = (arabicFontSize * 1.6f).sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            if (ayah.transliteration.isNotBlank()) {
                Text(
                    text = ayah.transliteration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Urdu Translation (اردو ترجمہ - مولانا فتح محمد جالندھری)
            if (showTranslation && ayah.translation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "اردو ترجمہ (جالندھری)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ayah.translation,
                            fontSize = (arabicFontSize * 0.72f).coerceAtLeast(18f).sp,
                            lineHeight = ((arabicFontSize * 0.72f).coerceAtLeast(18f) * 1.65f).sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Display User Bookmark Reflection if present
            if (isBookmarked && bookmarkNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onEditBookmark != null) { onEditBookmark?.invoke() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your Bookmark Reflection:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = bookmarkNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        if (onEditBookmark != null) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Reflection",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Classical Tafsir & Theological Insights toggle
            val hasTafsir = ayah.tafsirExcerpt.isNotBlank()
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTafsir = !showTafsir }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showTafsir) "Hide Classical Tafsir & Reflection" else "View Classical Tafsir & Reflection",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            AnimatedVisibility(visible = showTafsir) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (hasTafsir) ayah.tafsirExcerpt
                               else "Tadabbur Insight: Ayah ${ayah.ayahNumber} of Surah ${ayah.surahNumber}. Tap 'Add Note' above to record your personal research, linguistic breakdown, or connect this verse to your Mind Maps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
