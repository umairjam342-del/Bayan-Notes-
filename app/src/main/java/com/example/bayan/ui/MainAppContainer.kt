package com.example.bayan.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bayan.data.model.Bookmark
import com.example.bayan.data.model.MindMap
import com.example.bayan.data.model.Note
import com.example.bayan.data.repository.BayanRepository
import com.example.bayan.util.QuranAudioPlayer
import com.example.bayan.util.QuranAudioState
import com.example.bayan.ui.mindmap.MindMapStudioScreen
import com.example.bayan.ui.notes.NotesScreen
import com.example.bayan.ui.profile.ProfileScreen
import com.example.bayan.ui.quran.FullScreenRecitationLayout
import com.example.bayan.ui.quran.QuranBrowserScreen
import com.example.bayan.ui.theology.TheologyResearchScreen
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    repository: BayanRepository,
    isGuestMode: Boolean = false,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val audioPlayer = remember { QuranAudioPlayer.getInstance(context) }
    val audioState by audioPlayer.audioState.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) } // 0: Notes, 1: Quran, 2: MindMap, 3: Theology, 4: Profile
    var prefilledNoteToEdit by remember { mutableStateOf<Note?>(null) }
    var isQuranReaderActive by remember { mutableStateOf(false) }
    var isMindMapEditorActive by remember { mutableStateOf(false) }
    var targetSurahFromBookmark by remember { mutableStateOf<Int?>(null) }
    var targetAyahFromBookmark by remember { mutableStateOf<Int?>(null) }

    var isFullScreenRecitation by rememberSaveable { mutableStateOf(false) }
    var autoFullScreenOnRecitation by rememberSaveable { mutableStateOf(true) }

    // When audio recitation starts playing, automatically enter full screen layout if enabled
    var wasPlaying by remember { mutableStateOf(false) }
    LaunchedEffect(audioState.isPlaying) {
        if (audioState.isPlaying && !wasPlaying && autoFullScreenOnRecitation) {
            isFullScreenRecitation = true
        }
        wasPlaying = audioState.isPlaying
    }

    // Auto-exit fullscreen if recitation is stopped completely
    LaunchedEffect(audioState.isActive) {
        if (!audioState.isActive) {
            isFullScreenRecitation = false
        }
    }

    // Back navigation: exit full screen first if active
    BackHandler(enabled = isFullScreenRecitation) {
        isFullScreenRecitation = false
    }

    // Back navigation to Home (Notes tab) when not inside a sub-screen or full screen
    BackHandler(enabled = !isFullScreenRecitation && currentTab != 0 && !isQuranReaderActive && !isMindMapEditorActive) {
        currentTab = 0
    }

    // Realtime observers
    val notes by remember {
        repository.observeNotes().catch { emit(emptyList()) }
    }.collectAsState(initial = emptyList())

    val mindMaps by remember {
        repository.observeMindMaps().catch { emit(emptyList()) }
    }.collectAsState(initial = emptyList())

    val bookmarks by remember {
        repository.observeBookmarks().catch { emit(emptyList()) }
    }.collectAsState(initial = emptyList())

    val bookmarkedAyahKeys = remember(bookmarks) {
        bookmarks.map { "${it.surahNumber}:${it.ayahNumber}" }.toSet()
    }

    val onUpdateBookmark: (Bookmark) -> Unit = { bookmark ->
        coroutineScope.launch {
            val res = repository.updateBookmark(bookmark)
            if (res.isSuccess) {
                snackbarHostState.showSnackbar("Bookmark reflection updated")
            } else {
                snackbarHostState.showSnackbar("Failed to update bookmark: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Prevent top bar when Quran reader, Mind Map canvas editor, or full screen recitation is open
            if (!isQuranReaderActive && !isMindMapEditorActive && !isFullScreenRecitation) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentTab) {
                                0 -> "Bayan Study Notes"
                                1 -> "Quran Reader & Browser"
                                2 -> "Thematic Mind Maps"
                                3 -> "Theological Research"
                                4 -> "Research Profile"
                                else -> "Bayan Notes"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            // Autohide bottom navigation tabs when full screen recitation is active
            if (!isFullScreenRecitation) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.EditNote, contentDescription = "Notes", modifier = Modifier.size(24.dp)) },
                        label = { Text("Notes") },
                        modifier = Modifier.testTag("nav_notes")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Quran", modifier = Modifier.size(24.dp)) },
                        label = { Text("Quran") },
                        modifier = Modifier.testTag("nav_quran")
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Default.Hub, contentDescription = "Mind Map", modifier = Modifier.size(24.dp)) },
                        label = { Text("Mind Map") },
                        modifier = Modifier.testTag("nav_mindmap")
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Theology", modifier = Modifier.size(24.dp)) },
                        label = { Text("Theology") },
                        modifier = Modifier.testTag("nav_theology")
                    )
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { currentTab = 4 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullScreenRecitation) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                0 -> NotesScreen(
                    notes = notes,
                    mindMaps = mindMaps,
                    isLoading = false,
                    activeEditingNote = prefilledNoteToEdit,
                    onClearActiveEditingNote = { prefilledNoteToEdit = null },
                    onSaveNote = { note ->
                        coroutineScope.launch {
                            val res = if (note.id.isBlank()) repository.createNote(note) else repository.updateNote(note)
                            if (res.isSuccess) {
                                snackbarHostState.showSnackbar("Note saved successfully")
                            } else {
                                snackbarHostState.showSnackbar("Failed to save note: ${res.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    onDeleteNote = { noteId ->
                        coroutineScope.launch {
                            val res = repository.deleteNote(noteId)
                            if (res.isSuccess) {
                                snackbarHostState.showSnackbar("Note deleted")
                            } else {
                                snackbarHostState.showSnackbar("Failed to delete: ${res.exceptionOrNull()?.message}")
                            }
                        }
                    }
                )

                1 -> QuranBrowserScreen(
                    bookmarkedAyahs = bookmarkedAyahKeys,
                    bookmarks = bookmarks,
                    onUpdateBookmark = onUpdateBookmark,
                    onDeleteBookmark = { bId ->
                        coroutineScope.launch {
                            val res = repository.deleteBookmark(bId)
                            if (res.isSuccess) {
                                snackbarHostState.showSnackbar("Bookmark removed")
                            }
                        }
                    },
                    initialSurahNumber = targetSurahFromBookmark,
                    initialAyahNumber = targetAyahFromBookmark,
                    onClearInitialTarget = {
                        targetSurahFromBookmark = null
                        targetAyahFromBookmark = null
                    },
                    audioPlayer = audioPlayer,
                    isFullScreenRecitation = isFullScreenRecitation,
                    onToggleFullScreenRecitation = { isFullScreenRecitation = it },
                    onReaderActiveChange = { isQuranReaderActive = it },
                    onOpenNoteCreator = { surahNum, ayahNum, arabic, translation ->
                        prefilledNoteToEdit = Note(
                            title = "Reflections on Surah $surahNum, Ayah $ayahNum",
                            category = "tadabbur",
                            surahNumber = surahNum.toLong(),
                            ayahStart = ayahNum.toLong(),
                            ayahEnd = ayahNum.toLong(),
                            arabicSnippet = arabic,
                            translationSnippet = translation,
                            content = "Observations & Tafsir Analysis:\n\n"
                        )
                        currentTab = 0 // Switch to Notes tab with editor open
                    },
                    onAddBookmark = { surahNum, ayahNum, surahName ->
                        coroutineScope.launch {
                            val existing = bookmarks.find { it.surahNumber.toInt() == surahNum && it.ayahNumber.toInt() == ayahNum }
                            if (existing != null) {
                                repository.deleteBookmark(existing.id)
                                snackbarHostState.showSnackbar("Bookmark removed")
                            } else {
                                val b = Bookmark(
                                    surahNumber = surahNum.toLong(),
                                    ayahNumber = ayahNum.toLong(),
                                    surahName = surahName,
                                    note = ""
                                )
                                repository.addBookmark(b)
                                snackbarHostState.showSnackbar("Ayah $surahNum:$ayahNum bookmarked")
                            }
                        }
                    }
                )

                2 -> MindMapStudioScreen(
                    userMindMaps = mindMaps,
                    onEditorActiveChange = { isMindMapEditorActive = it },
                    onSaveMindMap = { map ->
                        coroutineScope.launch {
                            val res = if (map.id.isBlank() || map.id.startsWith("template_")) {
                                repository.createMindMap(map.copy(id = ""))
                            } else {
                                repository.updateMindMap(map)
                            }
                            if (res.isSuccess) {
                                snackbarHostState.showSnackbar("Mind Map saved to your studio")
                            } else {
                                snackbarHostState.showSnackbar("Failed to save map: ${res.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    onDeleteMindMap = { mapId ->
                        coroutineScope.launch {
                            val res = repository.deleteMindMap(mapId)
                            if (res.isSuccess) {
                                snackbarHostState.showSnackbar("Mind Map deleted")
                            }
                        }
                    }
                )

                3 -> TheologyResearchScreen(
                    onCreateNoteWithSnippet = { title, cat, commentary, arabic ->
                        prefilledNoteToEdit = Note(
                            title = title,
                            category = cat,
                            arabicSnippet = arabic,
                            content = commentary
                        )
                        currentTab = 0
                    }
                )

                4 -> ProfileScreen(
                    notesCount = notes.size,
                    mindMapsCount = mindMaps.size,
                    bookmarks = bookmarks,
                    isGuestMode = isGuestMode,
                    onDeleteBookmark = { bId ->
                        coroutineScope.launch {
                            repository.deleteBookmark(bId)
                            snackbarHostState.showSnackbar("Bookmark removed")
                        }
                    },
                    onUpdateBookmark = onUpdateBookmark,
                    onNavigateToAyah = { sNum, aNum ->
                        targetSurahFromBookmark = sNum
                        targetAyahFromBookmark = aNum
                        currentTab = 1
                    },
                    onSignOutSuccess = onSignOut
                )
            }

            // Floating Persistent Recitation Mini-Player
            // Visible when recitation is active and user is on another tab (Notes, Mind Maps, Theology, Profile) and NOT in full screen
            if (!isFullScreenRecitation && currentTab != 1 && audioState.isActive) {
                FloatingRecitationMiniPlayer(
                    state = audioState,
                    onTapPlayer = {
                        targetSurahFromBookmark = audioState.currentSurah
                        targetAyahFromBookmark = audioState.currentAyah
                        currentTab = 1
                    },
                    onEnterFullScreen = {
                        isFullScreenRecitation = true
                    },
                    onPlayPause = {
                        if (audioState.isPlaying) {
                            audioPlayer.pause()
                        } else {
                            audioPlayer.resume()
                        }
                    },
                    onNext = { audioPlayer.nextAyah() },
                    onClose = { audioPlayer.stop() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )
            }

            // Immersive Full Screen Recitation Layout
            // Shows full screen verse presentation, autohiding audio player controls and hiding tabs
            if (isFullScreenRecitation && audioState.isActive) {
                val bookmarkedThisAyah = remember(bookmarks, audioState.currentSurah, audioState.currentAyah) {
                    bookmarks.any { it.surahNumber.toInt() == audioState.currentSurah && it.ayahNumber.toInt() == audioState.currentAyah }
                }
                FullScreenRecitationLayout(
                    audioState = audioState,
                    audioPlayer = audioPlayer,
                    onExitFullScreen = { isFullScreenRecitation = false },
                    isBookmarked = bookmarkedThisAyah,
                    onBookmark = { sNum, aNum, sName ->
                        coroutineScope.launch {
                            val existing = bookmarks.find { it.surahNumber.toInt() == sNum && it.ayahNumber.toInt() == aNum }
                            if (existing != null) {
                                repository.deleteBookmark(existing.id)
                                snackbarHostState.showSnackbar("Bookmark removed")
                            } else {
                                val b = Bookmark(
                                    surahNumber = sNum.toLong(),
                                    ayahNumber = aNum.toLong(),
                                    surahName = sName,
                                    note = ""
                                )
                                repository.addBookmark(b)
                                snackbarHostState.showSnackbar("Ayah $sNum:$aNum bookmarked")
                            }
                        }
                    },
                    onCreateNote = { sNum, aNum, arabic, translation ->
                        prefilledNoteToEdit = Note(
                            title = "Reflections on Surah $sNum, Ayah $aNum",
                            category = "tadabbur",
                            surahNumber = sNum.toLong(),
                            ayahStart = aNum.toLong(),
                            ayahEnd = aNum.toLong(),
                            arabicSnippet = arabic,
                            translationSnippet = translation,
                            content = "Observations & Tafsir Analysis:\n\n"
                        )
                        currentTab = 0
                        isFullScreenRecitation = false
                    }
                )
            }
        }
    }
}

/**
 * Sleek, persistent floating mini-player shown when Quran recitation is playing in the background
 * while user navigates to Notes, Mind Map Studio, Theological Research, or Profile.
 */
@Composable
fun FloatingRecitationMiniPlayer(
    state: QuranAudioState,
    onTapPlayer: () -> Unit,
    onEnterFullScreen: () -> Unit = {},
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTapPlayer)
            .testTag("persistent_audio_mini_player"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mishary",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (state.skipSilenceGap) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• ⚡Gapless",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (state.isOfflineAudio) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• ⚡Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    IconButton(
                        onClick = onEnterFullScreen,
                        modifier = Modifier.size(30.dp).testTag("mini_player_fullscreen")
                    ) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = "Full Screen Recitation",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(34.dp).testTag("mini_player_play_pause")
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (state.isPlaying) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(30.dp).testTag("mini_player_next")
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next Ayah",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp).testTag("mini_player_close")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop recitation",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
