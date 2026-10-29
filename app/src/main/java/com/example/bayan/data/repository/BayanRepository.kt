package com.example.bayan.data.repository

import android.content.Context
import com.example.R
import com.example.bayan.data.model.Bookmark
import com.example.bayan.data.model.MindMap
import com.example.bayan.data.model.Note
import com.example.bayan.data.model.OperationType
import com.example.bayan.data.model.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BayanRepository(
    private val db: FirebaseFirestore? = null,
    private val context: Context? = null
) {

    // Resolves named database ID from string resource
    constructor(context: Context) : this(
        try {
            FirebaseFirestore.getInstance(
                context.applicationContext.getString(R.string.firestore_database_id)
            )
        } catch (_: Exception) {
            null
        },
        context
    )

    constructor(db: FirebaseFirestore) : this(db, null)

    private val auth = Firebase.auth

    val isGuestMode: Boolean
        get() = auth.currentUser == null

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    companion object {
        // Persistent in-memory state for guest exploration session
        private val _guestNotes = MutableStateFlow<List<Note>>(
            listOf(
                Note(
                    id = "guest_welcome_note",
                    userId = "guest",
                    title = "Reflections on Surah Al-Fatihah",
                    category = "tadabbur",
                    surahNumber = 1L,
                    ayahStart = 1L,
                    ayahEnd = 7L,
                    arabicSnippet = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    translationSnippet = "شروع الله کا نام لے کر جو بڑا مہربان نہایت رحم والا ہے",
                    content = "Observations & Tafsir Analysis:\n\nSurah Al-Fatihah (The Opening) encapsulates the foundational pillars of the Quran: Divine Praise (Hamd), Absolute Lordship (Rububiyyah), Enduring Mercy (Rahmah), Sovereign Accountability (Malik Yawm al-Din), and Exclusive Devotion with Supplication (Iyyaka Na'budu wa Iyyaka Nasta'in).",
                    tags = listOf("fatihah", "tawhid", "mercy")
                ),
                Note(
                    id = "guest_kursi_note",
                    userId = "guest",
                    title = "Ontological Depths of Ayat al-Kursi",
                    category = "theology",
                    surahNumber = 2L,
                    ayahStart = 255L,
                    ayahEnd = 255L,
                    arabicSnippet = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
                    translationSnippet = "خدا (وہ معبود برحق ہے کہ) اس کے سوا کوئی معبود نہیں زندہ ہمیشہ رہنے والا",
                    content = "Key Theological Themes:\n\n1. Al-Hayy (The Ever-Living): Self-subsisting vitality that never decays or sleeps.\n2. Al-Qayyum (The Sustainer): The Originator upon whom all existence is dependent.\n3. The Kursi: Metaphor for transcendent authority, encompassing cosmic knowledge without burden.",
                    tags = listOf("kursi", "asma_al_husna", "theology")
                )
            )
        )

        private val _guestMindMaps = MutableStateFlow<List<MindMap>>(
            listOf(
                MindMap(
                    id = "guest_sample_map",
                    userId = "guest",
                    title = "Structural Arc of Surah Al-Mulk",
                    description = "Exploring themes of cosmic creation, divine sovereignty, and moral accountability.",
                    surahNumber = 67L,
                    nodesJson = """[{"id":"root","label":"Surah Al-Mulk (Dominion)","category":"core","x":250,"y":50,"description":"Themes of sovereignty and cosmic signposts"},{"id":"n1","label":"Flawless Creation","category":"concept","x":100,"y":180,"description":"Ayah 3-4: Look again, do you see any rift?"},{"id":"n2","label":"Accountability","category":"theology","x":400,"y":180,"description":"Ayah 6-12: The warning and the unseen reverence"}]""",
                    edgesJson = """[{"id":"e1","sourceId":"root","targetId":"n1","relationship":"manifests in"},{"id":"e2","sourceId":"root","targetId":"n2","relationship":"demands"}]"""
                )
            )
        )

        private val _guestBookmarks = MutableStateFlow<List<Bookmark>>(
            listOf(
                Bookmark(
                    id = "guest_bm_1",
                    userId = "guest",
                    surahNumber = 1L,
                    ayahNumber = 1L,
                    surahName = "Al-Fatihah",
                    note = "The foundational invocation of grace"
                ),
                Bookmark(
                    id = "guest_bm_2",
                    userId = "guest",
                    surahNumber = 2L,
                    ayahNumber = 255L,
                    surahName = "Al-Baqarah",
                    note = "Ayat al-Kursi (The Greatest Verse)"
                )
            )
        )
    }

    // --- Notes CRUD & Realtime Stream ---

    fun observeNotes(): Flow<List<Note>> = flow {
        if (isGuestMode || db == null) {
            emitAll(_guestNotes.asStateFlow())
        } else {
            val uid = requireUserId()
            val path = "users/$uid/notes"
            emitAll(
                db.collection("users").document(uid).collection("notes")
                    .snapshots()
                    .map { snapshot -> snapshot.toObjects(Note::class.java) }
                    .catch { error ->
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
            )
        }
    }

    suspend fun getNoteById(noteId: String): Result<Note> {
        if (isGuestMode || db == null) {
            val note = _guestNotes.value.find { it.id == noteId }
            return if (note != null) Result.success(note)
            else Result.failure(NoSuchElementException("Note not found"))
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        return try {
            val snapshot = docRef.get().await()
            val note = snapshot.toObject(Note::class.java)
                ?: throw NoSuchElementException("Note not found")
            Result.success(note)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createNote(note: Note): Result<String> {
        val noteId = if (note.id.isNotBlank()) note.id else UUID.randomUUID().toString()

        if (isGuestMode || db == null) {
            val guestNote = note.copy(id = noteId, userId = "guest")
            _guestNotes.update { listOf(guestNote) + it.filter { n -> n.id != noteId } }
            return Result.success(noteId)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        val payload = note.copy(id = noteId, userId = uid).toCreateMap()
        return try {
            docRef.set(payload).await()
            Result.success(noteId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note): Result<Unit> {
        require(note.id.isNotBlank()) { "Note ID cannot be blank for update" }

        if (isGuestMode || db == null) {
            _guestNotes.update { list ->
                list.map { if (it.id == note.id) note else it }
            }
            return Result.success(Unit)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(note.id)
        return try {
            docRef.update(note.toUpdateMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        if (isGuestMode || db == null) {
            _guestNotes.update { list -> list.filter { it.id != noteId } }
            return Result.success(Unit)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Mind Maps CRUD & Realtime Stream ---

    fun observeMindMaps(): Flow<List<MindMap>> = flow {
        if (isGuestMode || db == null) {
            emitAll(_guestMindMaps.asStateFlow())
        } else {
            val uid = requireUserId()
            val path = "users/$uid/mindmaps"
            emitAll(
                db.collection("users").document(uid).collection("mindmaps")
                    .snapshots()
                    .map { snapshot -> snapshot.toObjects(MindMap::class.java) }
                    .catch { error ->
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
            )
        }
    }

    suspend fun createMindMap(mindMap: MindMap): Result<String> {
        val mapId = if (mindMap.id.isNotBlank()) mindMap.id else UUID.randomUUID().toString()

        if (isGuestMode || db == null) {
            val guestMap = mindMap.copy(id = mapId, userId = "guest")
            _guestMindMaps.update { listOf(guestMap) + it.filter { m -> m.id != mapId } }
            return Result.success(mapId)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("mindmaps").document(mapId)
        val payload = mindMap.copy(id = mapId, userId = uid).toCreateMap()
        return try {
            docRef.set(payload).await()
            Result.success(mapId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateMindMap(mindMap: MindMap): Result<Unit> {
        require(mindMap.id.isNotBlank()) { "MindMap ID cannot be blank for update" }

        if (isGuestMode || db == null) {
            _guestMindMaps.update { list ->
                list.map { if (it.id == mindMap.id) mindMap else it }
            }
            return Result.success(Unit)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("mindmaps").document(mindMap.id)
        return try {
            docRef.update(mindMap.toUpdateMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteMindMap(mapId: String): Result<Unit> {
        if (isGuestMode || db == null) {
            _guestMindMaps.update { list -> list.filter { it.id != mapId } }
            return Result.success(Unit)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("mindmaps").document(mapId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Bookmarks CRUD & Realtime Stream ---

    fun observeBookmarks(): Flow<List<Bookmark>> = flow {
        if (isGuestMode || db == null) {
            emitAll(_guestBookmarks.asStateFlow())
        } else {
            val uid = requireUserId()
            val path = "users/$uid/bookmarks"
            emitAll(
                db.collection("users").document(uid).collection("bookmarks")
                    .snapshots()
                    .map { snapshot -> snapshot.toObjects(Bookmark::class.java) }
                    .catch { error ->
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
            )
        }
    }

    suspend fun addBookmark(bookmark: Bookmark): Result<String> {
        val bookmarkId = if (bookmark.id.isNotBlank()) bookmark.id else UUID.randomUUID().toString()

        if (isGuestMode || db == null) {
            val guestBm = bookmark.copy(id = bookmarkId, userId = "guest")
            _guestBookmarks.update { listOf(guestBm) + it.filter { b -> b.id != bookmarkId } }
            return Result.success(bookmarkId)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(bookmarkId)
        val payload = bookmark.copy(id = bookmarkId, userId = uid).toCreateMap()
        return try {
            docRef.set(payload).await()
            Result.success(bookmarkId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateBookmark(bookmark: Bookmark): Result<Unit> {
        require(bookmark.id.isNotBlank()) { "Bookmark ID cannot be blank for update" }

        if (isGuestMode || db == null) {
            _guestBookmarks.update { list ->
                list.map { if (it.id == bookmark.id) bookmark else it }
            }
            return Result.success(Unit)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(bookmark.id)
        return try {
            docRef.update(bookmark.toUpdateMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteBookmark(bookmarkId: String): Result<Unit> {
        if (isGuestMode || db == null) {
            _guestBookmarks.update { list -> list.filter { it.id != bookmarkId } }
            return Result.success(Unit)
        }

        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("bookmarks").document(bookmarkId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
