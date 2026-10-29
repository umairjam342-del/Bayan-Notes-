package com.example.bayan

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.bayan.data.model.Note
import com.example.bayan.util.NoteExporter
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NoteExporterTest {

    private val sampleNote = Note(
        id = "test_note_1",
        title = "Linguistic Depths of Ayat al-Kursi",
        content = "The throne (kursi) symbolizes comprehensive divine knowledge and dominion. Al-Hayy and Al-Qayyum form the supreme foundation of divine names.",
        category = "theology",
        surahNumber = 2L,
        ayahStart = 255L,
        ayahEnd = 255L,
        arabicSnippet = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
        translationSnippet = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence.",
        tags = listOf("kursi", "tawhid", "names_of_allah")
    )

    private val secondNote = Note(
        id = "test_note_2",
        title = "Reflection on Divine Mercy in Al-Fatihah",
        content = "Ar-Rahman denotes the vast, all-encompassing mercy, while Ar-Rahim denotes the continuous, specialized mercy for believers.",
        category = "linguistics",
        surahNumber = 1L,
        ayahStart = 1L,
        ayahEnd = 3L,
        arabicSnippet = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        translationSnippet = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
        tags = listOf("mercy", "fatihah", "rahmah")
    )

    @Test
    fun generateMarkdown_containsAllEssentialFields() {
        val markdown = NoteExporter.generateMarkdown(sampleNote, "Al-Baqarah")

        assertTrue(markdown.contains("# Linguistic Depths of Ayat al-Kursi"))
        assertTrue(markdown.contains("Surah Al-Baqarah (2:255)"))
        assertTrue(markdown.contains("THEOLOGY") || markdown.contains("Theology"))
        assertTrue(markdown.contains("#kursi") && markdown.contains("#tawhid"))
        assertTrue(markdown.contains("اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ"))
        assertTrue(markdown.contains("Ever-Living, the Sustainer"))
        assertTrue(markdown.contains("The throne (kursi) symbolizes comprehensive"))
        assertTrue(markdown.contains("Bayan Notes"))
    }

    @Test
    fun generateCompendiumMarkdown_containsTableOfContentsAndNotes() {
        val notes = listOf(sampleNote, secondNote)
        val compendium = NoteExporter.generateCompendiumMarkdown(notes, "Test Compendium")

        assertTrue(compendium.contains("# Test Compendium"))
        assertTrue(compendium.contains("Table of Contents"))
        assertTrue(compendium.contains("1. [Linguistic Depths of Ayat al-Kursi]"))
        assertTrue(compendium.contains("2. [Reflection on Divine Mercy in Al-Fatihah]"))
        assertTrue(compendium.contains(sampleNote.content))
        assertTrue(compendium.contains(secondNote.content))
    }

    @Test
    fun generatePdf_producesValidNonEmptyFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        try {
            val file: File = NoteExporter.generatePdf(context, sampleNote, "Al-Baqarah")
            assertNotNull(file)
            assertTrue(file.exists())
            assertTrue("PDF file size should be greater than 100 bytes", file.length() > 100)
        } catch (e: IllegalStateException) {
            // Android's native libpdfium engine is unshadowed in headless JVM Robolectric
            org.junit.Assume.assumeTrue("Robolectric unshadowed native PdfDocument; skipping on JVM", false)
        }
    }

    @Test
    fun generatePdfCompendium_producesValidNonEmptyFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val notes = listOf(sampleNote, secondNote)
        try {
            val file: File = NoteExporter.generatePdfCompendium(context, notes, "Test Compendium")
            assertNotNull(file)
            assertTrue(file.exists())
            assertTrue("Compendium PDF file size should be greater than 100 bytes", file.length() > 100)
        } catch (e: IllegalStateException) {
            // Android's native libpdfium engine is unshadowed in headless JVM Robolectric
            org.junit.Assume.assumeTrue("Robolectric unshadowed native PdfDocument; skipping on JVM", false)
        }
    }

    @Test
    fun saveMarkdownFile_writesToExportsDirectory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val content = NoteExporter.generateMarkdown(sampleNote)
        val file = NoteExporter.saveMarkdownFile(context, content, "Test_Note")

        assertTrue(file.exists())
        assertTrue(file.name.endsWith(".md"))
        val readBack = file.readText()
        assertTrue(readBack.contains("Linguistic Depths of Ayat al-Kursi"))
    }
}
