package com.example.bayan.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bayan.data.model.Note
import com.example.bayan.data.repository.BayanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data class Success(val notes: List<Note>) : NotesUiState
    data class Error(val message: String) : NotesUiState
}

class NotesViewModel(
    private val repository: BayanRepository
) : ViewModel() {

    // Primary StateFlow observing the Note repository stream with two-tier error handling
    val uiState: StateFlow<NotesUiState> = repository.observeNotes()
        .map<List<Note>, NotesUiState> { NotesUiState.Success(it) }
        .catch { error ->
            emit(NotesUiState.Error(error.localizedMessage ?: "Failed to observe study notes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = NotesUiState.Loading
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Derived StateFlow providing real-time filtered results for search and categorization
    val filteredNotes: StateFlow<List<Note>> = combine(
        uiState,
        _searchQuery,
        _selectedCategory
    ) { state, query, category ->
        if (state !is NotesUiState.Success) {
            emptyList()
        } else {
            state.notes.filter { note ->
                val matchesCategory = if (category == "All") true
                else note.category.equals(category.lowercase().replace(" ", "_"), ignoreCase = true) ||
                     note.category.equals(category, ignoreCase = true)

                val matchesSearch = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.tags.any { it.contains(query, ignoreCase = true) } ||
                    note.arabicSnippet.contains(query)

                matchesCategory && matchesSearch
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun saveNote(
        note: Note,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = if (note.id.isBlank()) {
                repository.createNote(note)
            } else {
                repository.updateNote(note).map { note.id }
            }
            result.onSuccess { noteId ->
                onSuccess(noteId)
            }.onFailure { exception ->
                onError(exception.localizedMessage ?: "Failed to save note")
            }
        }
    }

    fun deleteNote(
        noteId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.deleteNote(noteId)
            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                onError(exception.localizedMessage ?: "Failed to delete note")
            }
        }
    }

    companion object {
        fun provideFactory(repository: BayanRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                        return NotesViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
    }
}
