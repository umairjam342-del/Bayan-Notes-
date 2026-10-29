package com.example.bayan

import com.example.bayan.util.AudioPlaybackState
import com.example.bayan.util.QuranAudioPlayer
import com.example.bayan.util.QuranAudioState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranAudioPlayerTest {

    @Test
    fun testAudioUrlFormatting_primaryCDN() {
        val alFatihahAyah1 = QuranAudioPlayer.buildAudioUrl(1, 1, fallback = false)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/001001.mp3", alFatihahAyah1)

        val alBaqarahAyatAlKursi = QuranAudioPlayer.buildAudioUrl(2, 255, fallback = false)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/002255.mp3", alBaqarahAyatAlKursi)

        val anNasAyah6 = QuranAudioPlayer.buildAudioUrl(114, 6, fallback = false)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/114006.mp3", anNasAyah6)
    }

    @Test
    fun testAudioUrlFormatting_fallbackCDN() {
        val alFatihahAyah1Fallback = QuranAudioPlayer.buildAudioUrl(1, 1, fallback = true)
        assertEquals("https://verses.quran.com/Alafasy/mp3/001001.mp3", alFatihahAyah1Fallback)

        val yasinAyah1 = QuranAudioPlayer.buildAudioUrl(36, 1, fallback = true)
        assertEquals("https://verses.quran.com/Alafasy/mp3/036001.mp3", yasinAyah1)
    }

    @Test
    fun testQuranAudioState_helperMethods() {
        val idleState = QuranAudioState()
        assertEquals(AudioPlaybackState.IDLE, idleState.playbackState)
        assertFalse(idleState.isPlaying)
        assertFalse(idleState.isLoading)
        assertFalse(idleState.isActive)
        assertEquals("Sheikh Mishary Rashid Alafasy", idleState.reciterName)

        val playingState = QuranAudioState(
            playbackState = AudioPlaybackState.PLAYING,
            currentSurah = 1,
            currentAyah = 2,
            surahName = "Al-Fatihah",
            totalVersesInSurah = 7
        )
        assertTrue(playingState.isPlaying)
        assertTrue(playingState.isActive)
        assertFalse(playingState.isLoading)
        assertTrue(playingState.isVersePlaying(1, 2))
        assertFalse(playingState.isVersePlaying(1, 3))
        assertFalse(playingState.isVersePlaying(2, 2))
        assertTrue(playingState.isVerseActive(1, 2))

        val loadingState = QuranAudioState(
            playbackState = AudioPlaybackState.LOADING,
            currentSurah = 2,
            currentAyah = 255
        )
        assertTrue(loadingState.isLoading)
        assertTrue(loadingState.isActive)
        assertFalse(loadingState.isPlaying)
        assertTrue(loadingState.isVerseLoading(2, 255))
        assertFalse(loadingState.isVerseLoading(2, 254))
    }

    @Test
    fun testQuranAudioPlayer_stateToggles() {
        val player = QuranAudioPlayer()
        val initialState = player.audioState.value
        assertEquals(AudioPlaybackState.IDLE, initialState.playbackState)
        assertTrue(initialState.autoAdvance)

        // Toggle auto advance
        player.toggleAutoAdvance()
        assertFalse(player.audioState.value.autoAdvance)

        player.toggleAutoAdvance()
        assertTrue(player.audioState.value.autoAdvance)

        // Set playback speed
        player.setPlaybackSpeed(1.25f)
        assertEquals(1.25f, player.audioState.value.playbackSpeed, 0.01f)

        // Verify skip silence gap (gapless) default is true
        assertTrue(player.audioState.value.skipSilenceGap)

        // Toggle skip silence gap
        player.toggleSkipSilenceGap()
        assertFalse(player.audioState.value.skipSilenceGap)

        player.toggleSkipSilenceGap()
        assertTrue(player.audioState.value.skipSilenceGap)

        // Stop resets state cleanly
        player.stop()
        assertEquals(AudioPlaybackState.IDLE, player.audioState.value.playbackState)
        assertNull(player.audioState.value.currentSurah)
        assertNull(player.audioState.value.currentAyah)

        player.release()
    }

    @Test
    fun testQuranAudioState_gaplessProperties() {
        val defaultState = QuranAudioState()
        assertTrue(defaultState.skipSilenceGap)
        assertFalse(defaultState.isOfflineAudio)
        assertEquals(0f, defaultState.progressFraction, 0.001f)

        val customState = QuranAudioState(
            skipSilenceGap = false,
            progressFraction = 0.75f,
            isOfflineAudio = true
        )
        assertFalse(customState.skipSilenceGap)
        assertTrue(customState.isOfflineAudio)
        assertEquals(0.75f, customState.progressFraction, 0.001f)
    }

    @Test
    fun testQuranAudioPlayer_speedControls() {
        val player = QuranAudioPlayer()
        assertEquals(1.0f, player.audioState.value.playbackSpeed, 0.01f)

        // Test increaseSpeed
        player.increaseSpeed()
        assertEquals(1.25f, player.audioState.value.playbackSpeed, 0.01f)

        player.increaseSpeed()
        assertEquals(1.5f, player.audioState.value.playbackSpeed, 0.01f)

        player.increaseSpeed()
        assertEquals(1.75f, player.audioState.value.playbackSpeed, 0.01f)

        player.increaseSpeed()
        assertEquals(2.0f, player.audioState.value.playbackSpeed, 0.01f)

        // At max speed, increase stays at 2.0f
        player.increaseSpeed()
        assertEquals(2.0f, player.audioState.value.playbackSpeed, 0.01f)

        // Test decreaseSpeed
        player.decreaseSpeed()
        assertEquals(1.75f, player.audioState.value.playbackSpeed, 0.01f)

        player.decreaseSpeed()
        assertEquals(1.5f, player.audioState.value.playbackSpeed, 0.01f)

        player.decreaseSpeed()
        assertEquals(1.25f, player.audioState.value.playbackSpeed, 0.01f)

        player.decreaseSpeed()
        assertEquals(1.0f, player.audioState.value.playbackSpeed, 0.01f)

        player.decreaseSpeed()
        assertEquals(0.75f, player.audioState.value.playbackSpeed, 0.01f)

        // At min speed, decrease stays at 0.75f
        player.decreaseSpeed()
        assertEquals(0.75f, player.audioState.value.playbackSpeed, 0.01f)

        // Test cycleSpeed: 0.75 -> 1.0 -> 1.25 -> 1.5 -> 1.75 -> 2.0 -> 0.75
        player.cycleSpeed()
        assertEquals(1.0f, player.audioState.value.playbackSpeed, 0.01f)

        player.cycleSpeed()
        assertEquals(1.25f, player.audioState.value.playbackSpeed, 0.01f)

        player.release()
    }
}
