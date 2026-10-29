package com.example.bayan

import com.example.base.FirestoreEmulatorTestBase
import com.example.bayan.data.model.MindMap
import com.example.bayan.data.model.Note
import com.example.bayan.data.repository.BayanRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BayanRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createNote_validPayload_createsDocumentAndReturnsId() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = BayanRepository(firestore)

    val note = Note(
      id = "note_${UUID.randomUUID()}",
      title = "Tafsir on Surah Al-Fatihah",
      content = "Detailed commentary on ayat 1 to 7.",
      category = "tafsir",
      surahNumber = 1L,
      ayahStart = 1L,
      ayahEnd = 7L
    )

    val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createNote(note) }
    assertTrue(createResult.isSuccess)
    val noteId = createResult.getOrThrow()
    assertNotNull(noteId)
  }

  @Test
  fun getNoteById_authenticatedOwner_returnsMatchingNote() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = BayanRepository(firestore)

    val noteId = "note_${UUID.randomUUID()}"
    val note = Note(
      id = noteId,
      title = "Linguistic Miracles in Surah Yusuf",
      content = "Analysis of rhetorical parallels.",
      category = "linguistics",
      surahNumber = 12L
    )

    withTimeout(DEFAULT_TIMEOUT_MS) { repository.createNote(note).getOrThrow() }
    val retrievedResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getNoteById(noteId) }
    assertTrue(retrievedResult.isSuccess)
    val retrieved = retrievedResult.getOrThrow()
    assertEquals(noteId, retrieved.id)
    assertEquals("Linguistic Miracles in Surah Yusuf", retrieved.title)
  }

  @Test
  fun observeNotes_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = BayanRepository(firestore)

    val noteId = "note_${UUID.randomUUID()}"
    val note = Note(
      id = noteId,
      title = "Realtime Tadabbur Log",
      content = "Observing flow updates.",
      category = "tadabbur"
    )

    withTimeout(DEFAULT_TIMEOUT_MS) { repository.createNote(note).getOrThrow() }

    val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeNotes().first { list -> list.any { it.id == noteId } }
    }
    assertTrue(emittedList.any { it.id == noteId })
  }

  @Test
  fun getNoteById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = BayanRepository(firestore)

    val noteId = "note_${UUID.randomUUID()}"
    val note = Note(
      id = noteId,
      title = "Alice Confidential Note",
      content = "Theology thesis drafts.",
      category = "theology"
    )

    withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.createNote(note).getOrThrow() }

    // Bob signs in and tries to read Alice's note at Alice's collection path
    signInTestUser(BOB_EMAIL)

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("users").document(aliceUid).collection("notes").document(noteId).get().await()
      }
      org.junit.Assert.fail("Expected FirebaseFirestoreException for cross-user read")
    } catch (exception: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception.code)
    }
  }

  @Test
  fun queryNotes_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()

    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        firestore.collection("users").document("anyone").collection("notes").get().await()
      }
      org.junit.Assert.fail("Expected FirebaseFirestoreException for unauthenticated query")
    } catch (exception: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception.code)
    }
  }

  @Test
  fun createMindMap_validPayload_createsDocument() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = BayanRepository(firestore)

    val mapId = "map_${UUID.randomUUID()}"
    val mindMap = MindMap(
      id = mapId,
      title = "Covenant Themes in Surah Al-Baqarah",
      description = "Mind map tracking covenant references and obligations",
      surahNumber = 2L,
      nodesJson = "[]",
      edgesJson = "[]"
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createMindMap(mindMap) }
    assertTrue(result.isSuccess)
    assertEquals(mapId, result.getOrThrow())
  }

  private companion object {
    const val ALICE_EMAIL = "alice@bayan.test"
    const val BOB_EMAIL = "bob@bayan.test"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
