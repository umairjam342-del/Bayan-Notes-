package com.example.bayan

import com.example.bayan.data.model.SurahOrderMode
import com.example.bayan.data.quran.QuranData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SurahOrderTest {

    @Test
    fun testAllSurahsCountAndRevelationOrderUniqueness() {
        val allSurahs = QuranData.surahs
        assertEquals(114, allSurahs.size)

        // Check each surah has a valid number 1..114
        val surahNumbers = allSurahs.map { it.number }.toSet()
        assertEquals(114, surahNumbers.size)
        assertTrue(surahNumbers.containsAll((1..114).toList()))

        // Check each surah has a unique revelation order 1..114
        val revelationOrders = allSurahs.map { it.revelationOrder }.toSet()
        assertEquals(114, revelationOrders.size)
        assertTrue(revelationOrders.containsAll((1..114).toList()))
    }

    @Test
    fun testFirstAndLastRevealedSurahs() {
        val byRevelation = QuranData.surahsByRevelation
        assertEquals(114, byRevelation.size)

        // #1 Revealed Surah is Al-'Alaq (Surah 96)
        val firstRevealed = byRevelation.first()
        assertEquals(96, firstRevealed.number)
        assertEquals(1, firstRevealed.revelationOrder)
        assertEquals("Al-'Alaq", firstRevealed.nameTransliterated)
        assertEquals("Meccan", firstRevealed.revelationType)

        // #2 Revealed Surah is Al-Qalam (Surah 68)
        val secondRevealed = byRevelation[1]
        assertEquals(68, secondRevealed.number)
        assertEquals(2, secondRevealed.revelationOrder)
        assertEquals("Al-Qalam", secondRevealed.nameTransliterated)

        // #3 Revealed Surah is Al-Muzzammil (Surah 73)
        val thirdRevealed = byRevelation[2]
        assertEquals(73, thirdRevealed.number)
        assertEquals(3, thirdRevealed.revelationOrder)

        // #4 Revealed Surah is Al-Muddaththir (Surah 74)
        val fourthRevealed = byRevelation[3]
        assertEquals(74, fourthRevealed.number)
        assertEquals(4, fourthRevealed.revelationOrder)

        // #5 Revealed Surah is Al-Fatihah (Surah 1)
        val fifthRevealed = byRevelation[4]
        assertEquals(1, fifthRevealed.number)
        assertEquals(5, fifthRevealed.revelationOrder)

        // #114 (Last) Revealed Surah is An-Nasr (Surah 110)
        val lastRevealed = byRevelation.last()
        assertEquals(110, lastRevealed.number)
        assertEquals(114, lastRevealed.revelationOrder)
        assertEquals("An-Nasr", lastRevealed.nameTransliterated)
        assertEquals("Medinan", lastRevealed.revelationType)
    }

    @Test
    fun testSurahsByRevelationListSorted() {
        val byRevelation = QuranData.surahsByRevelation
        for (i in 0 until byRevelation.size - 1) {
            assertTrue(
                "Surah at index $i (order ${byRevelation[i].revelationOrder}) should be <= index ${i + 1} (order ${byRevelation[i + 1].revelationOrder})",
                byRevelation[i].revelationOrder <= byRevelation[i + 1].revelationOrder
            )
            assertEquals(i + 1, byRevelation[i].revelationOrder)
        }
    }

    @Test
    fun testGetSurahsOrderModeHelper() {
        val mushafList = QuranData.getSurahs(SurahOrderMode.MUSHAF)
        assertEquals(114, mushafList.size)
        assertEquals(1, mushafList.first().number)
        assertEquals(114, mushafList.last().number)

        val revList = QuranData.getSurahs(SurahOrderMode.REVELATION)
        assertEquals(114, revList.size)
        assertEquals(1, revList.first().revelationOrder)
        assertEquals(114, revList.last().revelationOrder)
        assertEquals(96, revList.first().number)
        assertEquals(110, revList.last().number)
    }

    @Test
    fun testSearchAndTypeFiltering() {
        val revList = QuranData.getSurahs(SurahOrderMode.REVELATION)

        // Filter by Meccan in revelation order
        val meccanRev = revList.filter { it.revelationType.equals("Meccan", ignoreCase = true) }
        assertEquals(86, meccanRev.size)

        // Filter by Medinan in revelation order
        val medinanRev = revList.filter { it.revelationType.equals("Medinan", ignoreCase = true) }
        assertEquals(28, medinanRev.size)

        // Search for "Yusuf" in revelation order
        val yusuf = revList.find { it.nameTransliterated.contains("Yusuf", ignoreCase = true) }
        assertNotNull(yusuf)
        assertEquals(12, yusuf!!.number)
        assertEquals(53, yusuf.revelationOrder)
    }
}
