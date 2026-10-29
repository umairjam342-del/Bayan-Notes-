package com.example.bayan

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bayan.data.local.BayanDatabase
import com.example.bayan.data.local.TafsirStudyNoteDao
import com.example.bayan.data.local.TafsirStudyNoteEntity
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
class TafsirStudyNoteDaoTest {

    private lateinit var db: BayanDatabase
    private lateinit var dao: TafsirStudyNoteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BayanDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.tafsirStudyNoteDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveTafsirNote() = runBlocking {
        val note = TafsirStudyNoteEntity(
            title = "The Exegesis of Ayat al-Kursi",
            surahNumber = 2,
            surahName = "Al-Baqarah",
            ayahStart = 255,
            ayahEnd = 255,
            tafsirSource = "Tafsir Ibn Kathir",
            arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            translationText = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence.",
            classicalExegesis = "Ibn Kathir mentions this is the greatest ayah in the Book of Allah.",
            linguisticBreakdown = "Al-Qayyum is on the morphological scale of Fay'ul denoting eternal self-subsistence.",
            personalReflection = "Reflecting on how Al-Hayy and Al-Qayyum encompass all divine active and essential attributes.",
            tags = listOf("tawhid", "ayat_al_kursi", "ibn_kathir"),
            isFavorite = true
        )

        val id = dao.insertTafsirNote(note)
        assertTrue(id > 0)

        val notes = dao.getAllTafsirNotes().first()
        assertEquals(1, notes.size)
        val retrieved = notes.first()
        assertEquals("The Exegesis of Ayat al-Kursi", retrieved.title)
        assertEquals(2, retrieved.surahNumber)
        assertEquals(255, retrieved.ayahStart)
        assertEquals("Tafsir Ibn Kathir", retrieved.tafsirSource)
        assertTrue(retrieved.isFavorite)
        assertEquals(listOf("tawhid", "ayat_al_kursi", "ibn_kathir"), retrieved.tags)
    }

    @Test
    fun queryNotesBySurahAndAyahRange() = runBlocking {
        dao.insertTafsirNote(
            TafsirStudyNoteEntity(
                title = "Al-Fatihah Basmalah Nuances",
                surahNumber = 1,
                surahName = "Al-Fatihah",
                ayahStart = 1,
                ayahEnd = 1,
                tafsirSource = "Al-Qurtubi",
                classicalExegesis = "Jurisprudential discussion on whether Basmalah is an ayah of every Surah."
            )
        )
        dao.insertTafsirNote(
            TafsirStudyNoteEntity(
                title = "Praise belongs to Allah",
                surahNumber = 1,
                surahName = "Al-Fatihah",
                ayahStart = 2,
                ayahEnd = 3,
                tafsirSource = "Al-Tabari"
            )
        )
        dao.insertTafsirNote(
            TafsirStudyNoteEntity(
                title = "Al-Baqarah Opening",
                surahNumber = 2,
                surahName = "Al-Baqarah",
                ayahStart = 1,
                ayahEnd = 5
            )
        )

        val surah1Notes = dao.getNotesBySurah(1).first()
        assertEquals(2, surah1Notes.size)

        // Test querying for ayah 2 inside Surah 1 range (ayahStart=2, ayahEnd=3)
        val ayah2Notes = dao.getNotesForAyah(1, 2).first()
        assertEquals(1, ayah2Notes.size)
        assertEquals("Praise belongs to Allah", ayah2Notes.first().title)
    }

    @Test
    fun searchTafsirNotes() = runBlocking {
        dao.insertTafsirNote(
            TafsirStudyNoteEntity(
                title = "Metaphor of the Olive Tree",
                surahNumber = 24,
                ayahStart = 35,
                classicalExegesis = "Ayat an-Nur explanation by Imam al-Ghazali in Mishkat al-Anwar."
            )
        )
        dao.insertTafsirNote(
            TafsirStudyNoteEntity(
                title = "Prophet Musa at Mount Sinai",
                surahNumber = 20,
                ayahStart = 9,
                ayahEnd = 14,
                classicalExegesis = "Dialogue and the divine call in the valley of Tuwa."
            )
        )

        val results = dao.searchTafsirNotes("Ghazali").first()
        assertEquals(1, results.size)
        assertEquals("Metaphor of the Olive Tree", results.first().title)

        val mosesResults = dao.searchTafsirNotes("Sinai").first()
        assertEquals(1, mosesResults.size)
        assertEquals("Prophet Musa at Mount Sinai", mosesResults.first().title)
    }

    @Test
    fun toggleFavoriteAndDelete() = runBlocking {
        val note = TafsirStudyNoteEntity(
            title = "Temporary Note",
            surahNumber = 114,
            ayahStart = 1,
            isFavorite = false
        )
        val id = dao.insertTafsirNote(note)

        var favNotes = dao.getFavoriteNotes().first()
        assertEquals(0, favNotes.size)

        dao.setFavorite(id, true)
        favNotes = dao.getFavoriteNotes().first()
        assertEquals(1, favNotes.size)
        assertEquals(id, favNotes.first().id)

        dao.deleteTafsirNoteById(id)
        val allAfterDelete = dao.getAllTafsirNotes().first()
        assertEquals(0, allAfterDelete.size)
    }
}
