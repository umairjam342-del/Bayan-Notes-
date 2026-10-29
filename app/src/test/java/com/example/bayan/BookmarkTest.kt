package com.example.bayan

import com.example.bayan.data.model.Bookmark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkTest {

    @Test
    fun testBookmarkToCreateMap() {
        val bookmark = Bookmark(
            id = "bm_123",
            userId = "user_456",
            surahNumber = 2L,
            ayahNumber = 255L,
            surahName = "Al-Baqarah",
            note = "Ayat al-Kursi, the greatest verse in the Quran"
        )

        val map = bookmark.toCreateMap()
        assertEquals("user_456", map["userId"])
        assertEquals(2L, map["surahNumber"])
        assertEquals(255L, map["ayahNumber"])
        assertEquals("Al-Baqarah", map["surahName"])
        assertEquals("Ayat al-Kursi, the greatest verse in the Quran", map["note"])
        assertTrue(map.containsKey("createdAt"))
    }

    @Test
    fun testBookmarkToUpdateMap() {
        val bookmark = Bookmark(
            id = "bm_123",
            userId = "user_456",
            surahNumber = 18L,
            ayahNumber = 10L,
            surahName = "Al-Kahf",
            note = "Du'a of the companions of the cave"
        )

        val map = bookmark.toUpdateMap()
        // Ensure affectedKeys strictly conforms to firestore.rules: surahNumber, ayahNumber, surahName, note
        assertEquals(18L, map["surahNumber"])
        assertEquals(10L, map["ayahNumber"])
        assertEquals("Al-Kahf", map["surahName"])
        assertEquals("Du'a of the companions of the cave", map["note"])
        // Must NOT contain userId, id, or createdAt
        assertTrue(!map.containsKey("userId"))
        assertTrue(!map.containsKey("id"))
        assertTrue(!map.containsKey("createdAt"))
    }

    @Test
    fun testBookmarkCopyForEditing() {
        val initial = Bookmark(
            id = "bm_1",
            userId = "guest",
            surahNumber = 1L,
            ayahNumber = 1L,
            surahName = "Al-Fatihah",
            note = ""
        )

        val updated = initial.copy(note = "🌟 Favorite Ayah | Bismillah reflection")
        assertEquals("bm_1", updated.id)
        assertEquals("🌟 Favorite Ayah | Bismillah reflection", updated.note)
    }
}
