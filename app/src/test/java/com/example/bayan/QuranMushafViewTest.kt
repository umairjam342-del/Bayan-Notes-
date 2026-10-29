package com.example.bayan

import com.example.bayan.ui.quran.QuranViewMode
import com.example.bayan.ui.quran.formatAyahEndMarker
import com.example.bayan.ui.quran.toArabicDigits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranMushafViewTest {

    @Test
    fun testToArabicDigits_convertsStandardDigitsToArabicNumerals() {
        assertEquals("١", toArabicDigits(1))
        assertEquals("٧", toArabicDigits(7))
        assertEquals("١٠", toArabicDigits(10))
        assertEquals("٤٢", toArabicDigits(42))
        assertEquals("١١٤", toArabicDigits(114))
        assertEquals("٢٥٥", toArabicDigits(255))
        assertEquals("٢٨٦", toArabicDigits(286))
    }

    @Test
    fun testFormatAyahEndMarker_containsOrnateBracketsAndArabicDigits() {
        val marker1 = formatAyahEndMarker(1)
        assertEquals(" ﴿١﴾ ", marker1)

        val marker7 = formatAyahEndMarker(7)
        assertEquals(" ﴿٧﴾ ", marker7)

        val marker255 = formatAyahEndMarker(255)
        assertEquals(" ﴿٢٥٥﴾ ", marker255)
    }

    @Test
    fun testQuranViewMode_enumOptions() {
        val modes = QuranViewMode.values().map { it.name }
        assertTrue(modes.contains("VERSE"))
        assertTrue(modes.contains("MUSHAF"))
        assertEquals(2, modes.size)
    }
}
