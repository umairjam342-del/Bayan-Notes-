package com.example.bayan

import android.content.Context
import com.example.bayan.util.DownloadState
import com.example.bayan.util.QuranAudioDownloadManager
import com.example.bayan.util.SurahDownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class QuranAudioDownloadManagerTest {

    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    @Test
    fun testFileNamingAndResolution() {
        val file = QuranAudioDownloadManager.getOfflineAudioFile(context, 1, 1)
        assertNotNull(file)
        assertTrue(file.absolutePath.contains("quran_audio/1/001001.mp3"))

        val alBaqarahAyatAlKursiFile = QuranAudioDownloadManager.getOfflineAudioFile(context, 2, 255)
        assertTrue(alBaqarahAyatAlKursiFile.absolutePath.contains("quran_audio/2/002255.mp3"))
    }

    @Test
    fun testInitialStatusNotDownloaded() {
        val status = QuranAudioDownloadManager.syncSurahStatus(context, 114, 6)
        assertEquals(114, status.surahNumber)
        assertEquals(DownloadState.NOT_DOWNLOADED, status.state)
        assertEquals(0, status.downloadedAyahs)
        assertEquals(6, status.totalAyahs)
        assertEquals(0f, status.progressFraction, 0.001f)
    }

    @Test
    fun testSurahDownloadStatusDataClass() {
        val status = SurahDownloadStatus(
            surahNumber = 1,
            state = DownloadState.DOWNLOADED,
            downloadedAyahs = 7,
            totalAyahs = 7,
            progressFraction = 1.0f
        )
        assertEquals(DownloadState.DOWNLOADED, status.state)
        assertEquals(7, status.downloadedAyahs)
        assertEquals(1.0f, status.progressFraction, 0.001f)
    }

    @Test
    fun testDeleteSurahAudio() {
        val surahDir = File(context.filesDir, "quran_audio/1")
        surahDir.mkdirs()
        val dummyFile = File(surahDir, "001001.mp3")
        dummyFile.writeBytes(ByteArray(2048) { 1 })

        assertTrue(dummyFile.exists())
        assertTrue(QuranAudioDownloadManager.isVerseDownloaded(context, 1, 1))

        val deleted = QuranAudioDownloadManager.deleteSurahAudio(context, 1)
        assertTrue(deleted)
        assertFalse(QuranAudioDownloadManager.isVerseDownloaded(context, 1, 1))
    }
}
