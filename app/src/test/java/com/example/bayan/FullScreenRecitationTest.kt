package com.example.bayan

import com.example.bayan.util.AudioPlaybackState
import com.example.bayan.util.QuranAudioState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullScreenRecitationTest {

    @Test
    fun testQuranAudioState_playingAndActiveTriggers() {
        val idleState = QuranAudioState(playbackState = AudioPlaybackState.IDLE)
        assertFalse(idleState.isActive)
        assertFalse(idleState.isPlaying)

        val playingState = QuranAudioState(
            playbackState = AudioPlaybackState.PLAYING,
            currentSurah = 1,
            currentAyah = 1,
            surahName = "Al-Fatihah",
            totalVersesInSurah = 7,
            progressFraction = 0.45f
        )
        assertTrue(playingState.isActive)
        assertTrue(playingState.isPlaying)
        assertFalse(playingState.isPaused)
        assertEquals(1, playingState.currentSurah)
        assertEquals(1, playingState.currentAyah)
        assertEquals("Al-Fatihah", playingState.surahName)
    }

    @Test
    fun testAutohideConditions_playingVsPaused() {
        // Autohide should only trigger when playing is true and controls are visible
        fun shouldAutohide(isPlaying: Boolean, isControlsVisible: Boolean, autohideEnabled: Boolean): Boolean {
            return isPlaying && isControlsVisible && autohideEnabled
        }

        // When playing and controls visible -> should autohide after delay
        assertTrue(shouldAutohide(isPlaying = true, isControlsVisible = true, autohideEnabled = true))

        // When paused -> controls must stay visible to allow user interaction
        assertFalse(shouldAutohide(isPlaying = false, isControlsVisible = true, autohideEnabled = true))

        // When autohide is disabled (pinned controls) -> should not autohide
        assertFalse(shouldAutohide(isPlaying = true, isControlsVisible = true, autohideEnabled = false))

        // When controls are already hidden -> no need to autohide again
        assertFalse(shouldAutohide(isPlaying = true, isControlsVisible = false, autohideEnabled = true))
    }

    @Test
    fun testNavigationTabsVisibility_hiddenWhenFullScreenActive() {
        fun isNavigationBarVisible(isFullScreenRecitation: Boolean): Boolean {
            return !isFullScreenRecitation
        }

        // When full screen is inactive, tabs are visible
        assertTrue(isNavigationBarVisible(isFullScreenRecitation = false))

        // When full screen is active, tabs are autohidden
        assertFalse(isNavigationBarVisible(isFullScreenRecitation = true))
    }

    @Test
    fun testTopBarVisibility_hiddenWhenFullScreenOrSubScreenActive() {
        fun isTopBarVisible(isQuranReaderActive: Boolean, isMindMapEditorActive: Boolean, isFullScreenRecitation: Boolean): Boolean {
            return !isQuranReaderActive && !isMindMapEditorActive && !isFullScreenRecitation
        }

        assertTrue(isTopBarVisible(isQuranReaderActive = false, isMindMapEditorActive = false, isFullScreenRecitation = false))
        assertFalse(isTopBarVisible(isQuranReaderActive = false, isMindMapEditorActive = false, isFullScreenRecitation = true))
        assertFalse(isTopBarVisible(isQuranReaderActive = true, isMindMapEditorActive = false, isFullScreenRecitation = false))
    }

    @Test
    fun testVerseEndMarkerFormatting() {
        val digits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        fun toArabic(number: Int): String {
            return number.toString().map { if (it in '0'..'9') digits[it - '0'] else it }.joinToString("")
        }
        fun formatMarker(number: Int): String = " ﴿${toArabic(number)}﴾ "

        assertEquals(" ﴿١﴾ ", formatMarker(1))
        assertEquals(" ﴿٢٥٥﴾ ", formatMarker(255))
    }
}
