package com.example.bayan.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.example.bayan.util.NoteExporter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bayan.data.model.MindMap
import com.example.bayan.data.model.Note
import com.example.bayan.data.quran.QuranData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesScreen(
    notes: List<Note> = emptyList(),
    mindMaps: List<MindMap> = emptyList(),
    isLoading: Boolean = false,
    onSaveNote: (Note) -> Unit = {},
    onDeleteNote: (String) -> Unit = {},
    activeEditingNote: Note? = null,
    onClearActiveEditingNote: () -> Unit = {},
    viewModel: NotesViewModel? = null
) {
    val vmUiState = viewModel?.uiState?.collectAsStateWithLifecycle()?.value
    val vmFilteredNotes = viewModel?.filteredNotes?.collectAsStateWithLifecycle()?.value
    val vmSearchQuery = viewModel?.searchQuery?.collectAsStateWithLifecycle()?.value ?: ""
    val vmCategory = viewModel?.selectedCategory?.collectAsStateWithLifecycle()?.value ?: "All"

    var localSearchQuery by remember { mutableStateOf("") }
    var localCategory by remember { mutableStateOf("All") }
    var editingNote by remember(activeEditingNote) { mutableStateOf(activeEditingNote) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var exportingNote by remember { mutableStateOf<Note?>(null) }
    var showCompendiumExport by remember { mutableStateOf(false) }

    val searchQuery = if (viewModel != null) vmSearchQuery else localSearchQuery
    val selectedCategory = if (viewModel != null) vmCategory else localCategory

    val categories = listOf("All", "Tafsir", "Tadabbur", "Linguistics", "Theology", "Asbab al-Nuzul", "General")

    val effectiveLoading = if (viewModel != null) (vmUiState is NotesUiState.Loading) else isLoading

    val filteredNotes = if (viewModel != null && vmFilteredNotes != null) {
        vmFilteredNotes
    } else {
        remember(notes, searchQuery, selectedCategory) {
            notes.filter { note ->
                val matchesCategory = if (selectedCategory == "All") true
                else note.category.equals(selectedCategory.lowercase().replace(" ", "_"), ignoreCase = true) ||
                     note.category.equals(selectedCategory, ignoreCase = true)

                val matchesSearch = searchQuery.isBlank() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                    note.arabicSnippet.contains(searchQuery)

                matchesCategory && matchesSearch
            }
        }
    }

    var selectedDesktopNote by remember { mutableStateOf<Note?>(null) }
    var isNewDesktopDraft by remember { mutableStateOf(false) }

    LaunchedEffect(activeEditingNote) {
        if (activeEditingNote != null) {
            selectedDesktopNote = activeEditingNote
            isNewDesktopDraft = activeEditingNote.id.isBlank()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktopLayout = maxWidth >= 760.dp

        if (isDesktopLayout) {
            // Split-Screen Desktop Layout (Canonical List-Detail)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Left Pane: Navigation & Note Master List
                Column(
                    modifier = Modifier
                        .widthIn(min = 340.dp, max = 420.dp)
                        .weight(0.38f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Study Notes & Tafsir",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${filteredNotes.size} ${if (filteredNotes.size == 1) "entry" else "entries"} recorded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedButton(
                                onClick = { showCompendiumExport = true },
                                shape = RoundedCornerShape(12.dp),
                                enabled = filteredNotes.isNotEmpty(),
                                modifier = Modifier.testTag("export_compendium_button")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export All", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Create New Note Desktop Button
                        Button(
                            onClick = {
                                selectedDesktopNote = Note(category = "tafsir")
                                isNewDesktopDraft = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .testTag("create_note_desktop_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("New Study Note", fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                if (viewModel != null) {
                                    viewModel.onSearchQueryChanged(it)
                                } else {
                                    localSearchQuery = it
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notes_search_input"),
                            placeholder = { Text("Search tafsir, tags...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        if (viewModel != null) {
                                            viewModel.onCategorySelected(category)
                                        } else {
                                            localCategory = category
                                        }
                                    },
                                    label = { Text(category, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    if (effectiveLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (filteredNotes.isEmpty()) {
                        EmptyNotesState(
                            hasQuery = searchQuery.isNotBlank() || selectedCategory != "All",
                            onCreateNote = {
                                selectedDesktopNote = Note(category = "tafsir")
                                isNewDesktopDraft = true
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("notes_master_list")
                        ) {
                            items(filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    isSelected = selectedDesktopNote?.id == note.id && !isNewDesktopDraft,
                                    onEdit = {
                                        selectedDesktopNote = note
                                        isNewDesktopDraft = false
                                    },
                                    onDelete = { noteToDelete = note },
                                    onExport = { exportingNote = note }
                                )
                            }
                        }
                    }
                }

                // Split Pane Divider
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                // Right Pane: Detail & Workspace
                Box(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (selectedDesktopNote != null) {
                        DesktopNoteDetailPane(
                            note = selectedDesktopNote!!,
                            isNewDraft = isNewDesktopDraft,
                            availableMindMaps = mindMaps,
                            onSave = { updatedNote ->
                                onSaveNote(updatedNote)
                                selectedDesktopNote = updatedNote
                                isNewDesktopDraft = false
                            },
                            onDelete = { noteToDelete = selectedDesktopNote },
                            onExport = { exportingNote = selectedDesktopNote },
                            onClose = {
                                selectedDesktopNote = null
                                isNewDesktopDraft = false
                            }
                        )
                    } else {
                        DesktopEmptyDetailPane(
                            onCreateNew = {
                                selectedDesktopNote = Note(category = "tafsir")
                                isNewDesktopDraft = true
                            }
                        )
                    }
                }
            }
        } else {
            // Standard Mobile Scaffold Layout
            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            editingNote = Note(category = "tafsir")
                        },
                        modifier = Modifier.testTag("create_note_fab"),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Study Note")
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Mobile Header with Title, Count & Compendium Export
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Study Notes & Tafsir",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${filteredNotes.size} ${if (filteredNotes.size == 1) "entry" else "entries"} recorded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedButton(
                                onClick = { showCompendiumExport = true },
                                shape = RoundedCornerShape(12.dp),
                                enabled = filteredNotes.isNotEmpty(),
                                modifier = Modifier.testTag("export_compendium_button")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export All", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                if (viewModel != null) {
                                    viewModel.onSearchQueryChanged(it)
                                } else {
                                    localSearchQuery = it
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notes_search_input"),
                            placeholder = { Text("Search your tafsir, reflections & tags...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        if (viewModel != null) {
                                            viewModel.onCategorySelected(category)
                                        } else {
                                            localCategory = category
                                        }
                                    },
                                    label = { Text(category) }
                                )
                            }
                        }
                    }

                    if (effectiveLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (filteredNotes.isEmpty()) {
                        EmptyNotesState(
                            hasQuery = searchQuery.isNotBlank() || selectedCategory != "All",
                            onCreateNote = { editingNote = Note(category = "tafsir") }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onEdit = { editingNote = note },
                                    onDelete = { noteToDelete = note },
                                    onExport = { exportingNote = note }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Note Editor Sheet
    editingNote?.let { note ->
        NoteEditorBottomSheet(
            note = note,
            availableMindMaps = mindMaps,
            onDismiss = {
                editingNote = null
                onClearActiveEditingNote()
            },
            onSave = { updatedNote ->
                onSaveNote(updatedNote)
                editingNote = null
                onClearActiveEditingNote()
            },
            onExport = { noteForExport ->
                exportingNote = noteForExport
            }
        )
    }

    // Export Single Note Bottom Sheet
    exportingNote?.let { note ->
        ExportNoteBottomSheet(
            note = note,
            onDismiss = { exportingNote = null }
        )
    }

    // Export Compendium Bottom Sheet
    if (showCompendiumExport) {
        ExportCompendiumBottomSheet(
            notes = filteredNotes,
            onDismiss = { showCompendiumExport = false }
        )
    }

    // Delete Confirmation Dialog
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Study Note") },
            text = { Text("Are you sure you want to delete '${note.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteNote(note.id)
                        noteToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: Note,
    isSelected: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit = {}
) {
    val border = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Category Badge, Surah reference & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val categoryColor = when (note.category.lowercase()) {
                        "tafsir" -> MaterialTheme.colorScheme.primaryContainer
                        "tadabbur" -> MaterialTheme.colorScheme.secondaryContainer
                        "linguistics" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val onCategoryColor = when (note.category.lowercase()) {
                        "tafsir" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "tadabbur" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "linguistics" -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor
                    ) {
                        Text(
                            text = note.category.replace("_", " ").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = onCategoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (note.surahNumber > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val surah = QuranData.surahs.find { it.number == note.surahNumber.toInt() }
                        val ayahRef = if (note.ayahStart > 0) {
                            if (note.ayahEnd > note.ayahStart) "${note.ayahStart}-${note.ayahEnd}"
                            else "${note.ayahStart}"
                        } else "Full"

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${surah?.nameTransliterated ?: "Surah ${note.surahNumber}"}:$ayahRef",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExport,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("export_note_${note.id}")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Export Note",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Note", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Arabic Snippet quote preview if available
            if (note.arabicSnippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = note.arabicSnippet,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content preview
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Tags Flow
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyNotesState(
    hasQuery: Boolean,
    onCreateNote: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasQuery) "No matching study notes" else "No Quran study notes yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (hasQuery) "Try adjusting your search query or category filter."
                       else "Start recording classical tafsir insights, linguistic gems, and personal tadabbur reflections.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            Button(
                onClick = onCreateNote,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Write New Note")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorBottomSheet(
    note: Note,
    availableMindMaps: List<MindMap>,
    onDismiss: () -> Unit,
    onSave: (Note) -> Unit,
    onExport: (Note) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var category by remember { mutableStateOf(note.category) }
    var surahNumber by remember { mutableStateOf(note.surahNumber.toString()) }
    var ayahStart by remember { mutableStateOf(if (note.ayahStart > 0) note.ayahStart.toString() else "") }
    var ayahEnd by remember { mutableStateOf(if (note.ayahEnd > 0) note.ayahEnd.toString() else "") }
    var arabicSnippet by remember { mutableStateOf(note.arabicSnippet) }
    var translationSnippet by remember { mutableStateOf(note.translationSnippet) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(note.tags) }
    var selectedMindMapId by remember { mutableStateOf(note.mindMapId) }

    val categories = listOf("tafsir", "tadabbur", "linguistics", "theology", "asbab_nuzul", "general")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (note.id.isBlank()) "New Study Note" else "Edit Study Note",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val sNum = surahNumber.toLongOrNull() ?: 0L
                            val aStart = ayahStart.toLongOrNull() ?: 0L
                            val aEnd = ayahEnd.toLongOrNull() ?: aStart
                            val noteToExport = note.copy(
                                title = title.ifBlank { "Untitled Note" }.trim(),
                                content = content.trim(),
                                category = category,
                                surahNumber = sNum,
                                ayahStart = aStart,
                                ayahEnd = aEnd,
                                arabicSnippet = arabicSnippet.trim(),
                                translationSnippet = translationSnippet.trim(),
                                tags = tags,
                                mindMapId = selectedMindMapId
                            )
                            onExport(noteToExport)
                        },
                        modifier = Modifier.testTag("export_from_editor_button")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Export Note",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note Title *") },
                placeholder = { Text("e.g., Linguistic Nuances of 'Ar-Rahman' vs 'Ar-Rahim'") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips Row
            Text(
                text = "Discipline / Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.replace("_", " ").capitalizeWords()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Surah & Ayah Citation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = surahNumber,
                    onValueChange = { surahNumber = it.filter { char -> char.isDigit() } },
                    label = { Text("Surah #") },
                    placeholder = { Text("1-114") },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = ayahStart,
                    onValueChange = { ayahStart = it.filter { char -> char.isDigit() } },
                    label = { Text("Ayah Start") },
                    placeholder = { Text("1") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = ayahEnd,
                    onValueChange = { ayahEnd = it.filter { char -> char.isDigit() } },
                    label = { Text("Ayah End") },
                    placeholder = { Text("7") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arabic Verse Snippet
            OutlinedTextField(
                value = arabicSnippet,
                onValueChange = { arabicSnippet = it },
                label = { Text("Arabic Text / Ayah Quote") },
                placeholder = { Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            // Quick Insert Bismillah shortcut button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        arabicSnippet = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                        translationSnippet = "شروع الله کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے"
                    }
                ) {
                    Text("+ Insert Basmalah", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Translation Snippet
            OutlinedTextField(
                value = translationSnippet,
                onValueChange = { translationSnippet = it },
                label = { Text("Translation Quote (اردو ترجمہ)") },
                placeholder = { Text("Urdu translation reference (اردو ترجمہ)...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rich Text Content
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Scholarly Commentary & Reflections *") },
                placeholder = { Text("Write your analysis, grammatical breakdown, classical citations (e.g. Tabari, Qurtubi, Ibn Kathir), and spiritual insights...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("note_content_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags Manager
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("Add Tag") },
                    placeholder = { Text("e.g., mercy, tawhid, covenant") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val clean = tagInput.trim().replace("#", "").lowercase()
                        if (clean.isNotBlank() && !tags.contains(clean)) {
                            tags = tags + clean
                            tagInput = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add")
                }
            }

            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("#$tag", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove tag",
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { tags = tags - tag }
                                )
                            }
                        }
                    }
                }
            }

            // Save Action Button
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        val sNum = surahNumber.toLongOrNull() ?: 0L
                        val aStart = ayahStart.toLongOrNull() ?: 0L
                        val aEnd = ayahEnd.toLongOrNull() ?: aStart

                        val updated = note.copy(
                            title = title.trim(),
                            content = content.trim(),
                            category = category,
                            surahNumber = sNum,
                            ayahStart = aStart,
                            ayahEnd = aEnd,
                            arabicSnippet = arabicSnippet.trim(),
                            translationSnippet = translationSnippet.trim(),
                            tags = tags,
                            mindMapId = selectedMindMapId
                        )
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            onSave(updated)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_note_button"),
                shape = RoundedCornerShape(16.dp),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text(
                    text = if (note.id.isBlank()) "Save Note to Studio" else "Update Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportNoteBottomSheet(
    note: Note,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var isExportingPdf by remember { mutableStateOf(false) }

    val surah = QuranData.surahs.find { it.number == note.surahNumber.toInt() }
    val surahRef = if (note.surahNumber > 0) {
        val ayahRef = if (note.ayahStart > 0) {
            if (note.ayahEnd > note.ayahStart) "${note.ayahStart}–${note.ayahEnd}"
            else "${note.ayahStart}"
        } else "Full Surah"
        "${surah?.nameTransliterated ?: "Surah ${note.surahNumber}"}:$ayahRef"
    } else "General Study"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Export Study Research",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Share this note outside the application as Markdown or PDF",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note Summary Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = note.title.ifBlank { "Untitled Note" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$surahRef • ${note.category.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Markdown (.md) Export Option
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Markdown File (.md)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Full formatting, Arabic verses, quotes & tags",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val mdContent = NoteExporter.generateMarkdown(note, surah?.nameTransliterated)
                                val file = NoteExporter.saveMarkdownFile(context, mdContent, note.title)
                                NoteExporter.shareFile(context, file, "text/markdown", note.title)
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("share_markdown_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share .md File")
                        }

                        OutlinedButton(
                            onClick = {
                                val mdContent = NoteExporter.generateMarkdown(note, surah?.nameTransliterated)
                                NoteExporter.copyToClipboard(context, note.title, mdContent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_markdown_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. PDF Document (.pdf) Export Option
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PDF Document (.pdf)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Publication-ready layout with gold/emerald design",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isExportingPdf = true
                            try {
                                val pdfFile = NoteExporter.generatePdf(context, note, surah?.nameTransliterated)
                                NoteExporter.shareFile(context, pdfFile, "application/pdf", note.title)
                            } finally {
                                isExportingPdf = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isExportingPdf
                    ) {
                        if (isExportingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating PDF...")
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Save PDF Document")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportCompendiumBottomSheet(
    notes: List<Note>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var isExportingPdf by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Export Study Compendium",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Export all ${notes.size} research notes as a unified compilation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Compendium Markdown Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Compendium Markdown (.md)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Complete research booklet with Table of Contents",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val compendiumMd = NoteExporter.generateCompendiumMarkdown(notes)
                                val file = NoteExporter.saveMarkdownFile(context, compendiumMd, "Bayan_Quran_Compendium")
                                NoteExporter.shareFile(context, file, "text/markdown", "Bayan Quran Study Compendium")
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("share_compendium_md_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Compendium")
                        }

                        OutlinedButton(
                            onClick = {
                                val compendiumMd = NoteExporter.generateCompendiumMarkdown(notes)
                                NoteExporter.copyToClipboard(context, "Bayan Compendium", compendiumMd)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_compendium_md_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy All")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compendium PDF Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Compendium PDF Book (.pdf)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Multi-page booklet with Cover page, Index & styled notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isExportingPdf = true
                            try {
                                val pdfFile = NoteExporter.generatePdfCompendium(context, notes)
                                NoteExporter.shareFile(context, pdfFile, "application/pdf", "Bayan Quran Study Compendium")
                            } finally {
                                isExportingPdf = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_compendium_pdf_button"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isExportingPdf
                    ) {
                        if (isExportingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Compendium PDF...")
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate & Share PDF Book")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesktopNoteDetailPane(
    note: Note,
    isNewDraft: Boolean,
    availableMindMaps: List<MindMap>,
    onSave: (Note) -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember(note) { mutableStateOf(note.title) }
    var content by remember(note) { mutableStateOf(note.content) }
    var category by remember(note) { mutableStateOf(note.category) }
    var surahNumber by remember(note) { mutableStateOf(if (note.surahNumber > 0) note.surahNumber.toString() else "") }
    var ayahStart by remember(note) { mutableStateOf(if (note.ayahStart > 0) note.ayahStart.toString() else "") }
    var ayahEnd by remember(note) { mutableStateOf(if (note.ayahEnd > 0) note.ayahEnd.toString() else "") }
    var arabicSnippet by remember(note) { mutableStateOf(note.arabicSnippet) }
    var translationSnippet by remember(note) { mutableStateOf(note.translationSnippet) }
    var tagInput by remember(note) { mutableStateOf("") }
    var tags by remember(note) { mutableStateOf(note.tags) }
    var selectedMindMapId by remember(note) { mutableStateOf(note.mindMapId) }

    val categories = listOf("tafsir", "tadabbur", "linguistics", "theology", "asbab_nuzul", "general")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("desktop_note_detail_pane")
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Desktop Workspace Action Header
        Surface(
            tonalElevation = 1.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isNewDraft) Icons.Default.Add else Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isNewDraft) "New Study Note Draft" else (title.ifBlank { "Tafsir Study Note" }),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isNewDraft) "Draft in progress" else "Category: ${category.replace("_", " ").capitalizeWords()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isNewDraft && note.id.isNotBlank()) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("desktop_delete_note_button")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Note",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    IconButton(
                        onClick = onExport,
                        modifier = Modifier.testTag("desktop_export_note_button")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Export Note",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            val sNum = surahNumber.toLongOrNull() ?: 0L
                            val aStart = ayahStart.toLongOrNull() ?: 0L
                            val aEnd = ayahEnd.toLongOrNull() ?: aStart
                            val updated = note.copy(
                                title = title.trim(),
                                content = content.trim(),
                                category = category,
                                surahNumber = sNum,
                                ayahStart = aStart,
                                ayahEnd = aEnd,
                                arabicSnippet = arabicSnippet.trim(),
                                translationSnippet = translationSnippet.trim(),
                                tags = tags,
                                mindMapId = selectedMindMapId
                            )
                            onSave(updated)
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("desktop_save_note_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNewDraft) "Save Note" else "Update Note", fontWeight = FontWeight.SemiBold)
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("desktop_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Detail View")
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Workspace Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Note Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note Title *") },
                placeholder = { Text("e.g., Theological Context & Nuances of Ayat al-Kursi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("desktop_note_title_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Discipline & Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " ").capitalizeWords()) }
                        )
                    }
                }
            }

            // Surah & Ayah Citation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = surahNumber,
                    onValueChange = { surahNumber = it.filter { char -> char.isDigit() } },
                    label = { Text("Surah #") },
                    placeholder = { Text("1-114") },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("desktop_surah_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = ayahStart,
                    onValueChange = { ayahStart = it.filter { char -> char.isDigit() } },
                    label = { Text("Ayah Start") },
                    placeholder = { Text("1") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("desktop_ayah_start_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = ayahEnd,
                    onValueChange = { ayahEnd = it.filter { char -> char.isDigit() } },
                    label = { Text("Ayah End") },
                    placeholder = { Text("7") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("desktop_ayah_end_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Arabic Verse Snippet
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = arabicSnippet,
                    onValueChange = { arabicSnippet = it },
                    label = { Text("Arabic Text / Ayah Quote") },
                    placeholder = { Text("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("desktop_arabic_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            arabicSnippet = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                            translationSnippet = "شروع الله کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے"
                        }
                    ) {
                        Text("+ Quick Insert Basmalah", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Translation Quote
            OutlinedTextField(
                value = translationSnippet,
                onValueChange = { translationSnippet = it },
                label = { Text("Translation Quote (اردو ترجمہ)") },
                placeholder = { Text("Urdu translation reference (اردو ترجمہ)...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("desktop_translation_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Commentary & Reflections
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Scholarly Commentary & Reflections *") },
                placeholder = { Text("Write your analysis, grammatical breakdown, classical citations (e.g. Tabari, Qurtubi, Ibn Kathir), and spiritual reflections...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("desktop_note_content_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Tags Manager
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Thematic Tags",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        label = { Text("Add Tag") },
                        placeholder = { Text("e.g., mercy, tawhid, covenant") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("desktop_tag_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val clean = tagInput.trim().replace("#", "").lowercase()
                            if (clean.isNotBlank() && !tags.contains(clean)) {
                                tags = tags + clean
                                tagInput = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("desktop_add_tag_button")
                    ) {
                        Text("Add")
                    }
                }

                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("#$tag", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { tags = tags - tag }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DesktopEmptyDetailPane(
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("desktop_empty_detail_pane"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Select a Study Note",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose a note from the master list on the left to view and edit its commentary, or create a brand new tafsir reflection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 420.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateNew,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("desktop_empty_create_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Study Note", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
