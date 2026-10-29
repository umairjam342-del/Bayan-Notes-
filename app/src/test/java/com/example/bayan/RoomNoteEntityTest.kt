package com.example.bayan

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bayan.data.local.BayanDatabase
import com.example.bayan.data.local.NoteDao
import com.example.bayan.data.local.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RoomNoteEntityTest {

    private lateinit var db: BayanDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BayanDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = db.noteDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveNote() = runBlocking {
        val note = NoteEntity(
            title = "Linguistic Miracles of Surah Al-Ikhlas",
            content = "Detailed analysis of As-Samad and Ahad in divine rhetoric.",
            tags = listOf("tawhid", "linguistics", "ikhlas"),
            surahNumber = 112,
            ayahStart = 1,
            ayahEnd = 4,
            category = "linguistics",
            arabicSnippet = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ",
            translationSnippet = "Say, He is Allah, [who is] One, Allah, the Eternal Refuge."
        )

        val id = noteDao.insertNote(note)
        assertTrue(id > 0)

        val allNotes = noteDao.getAllNotes().first()
        assertEquals(1, allNotes.size)
        val retrieved = allNotes.first()
        assertEquals("Linguistic Miracles of Surah Al-Ikhlas", retrieved.title)
        assertEquals(listOf("tawhid", "linguistics", "ikhlas"), retrieved.tags)
        assertEquals(112, retrieved.surahNumber)
        assertNotNull(retrieved.createdAt)
        assertNotNull(retrieved.updatedAt)
    }

    @Test
    fun searchNotesByQuery() = runBlocking {
        noteDao.insertNote(
            NoteEntity(
                title = "Covenant in Surah Al-Baqarah",
                content = "Bani Isra'il and the divine covenant obligations.",
                tags = listOf("covenant", "baqarah"),
                surahNumber = 2
            )
        )
        noteDao.insertNote(
            NoteEntity(
                title = "Patience of Ayyub",
                content = "Tadabbur on trials and steadfast prayer.",
                tags = listOf("sabr", "prophets"),
                surahNumber = 21
            )
        )

        val covenantResults = noteDao.searchNotes("covenant").first()
        assertEquals(1, covenantResults.size)
        assertEquals("Covenant in Surah Al-Baqarah", covenantResults.first().title)
    }
}
