package com.example.tysyacha_calc.ui

import com.example.tysyacha_calc.ui.preview.PreviewData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Данные превью строятся настоящим движком — если правила поменяются, этот тест покажет, что превью сломались. */
class PreviewDataTest {

    @Test fun gamesArePlayable() {
        assertEquals(3, PreviewData.game3.rounds.size)
        assertEquals(1, PreviewData.game4.rounds.size)
        assertEquals(1, PreviewData.finished.winnerId)
    }

    @Test fun calculatorStates() {
        assertTrue(PreviewData.calculatorNoTaker.hands.isEmpty())
        assertNotNull(PreviewData.calculatorFilled.preview)
        assertTrue(PreviewData.calculatorTimeline.timeline!!.hasFuture)
        assertNotNull(PreviewData.calculator4.prikup)
        assertTrue(PreviewData.calculatorFinished.game!!.isFinished)
    }

    @Test fun home() {
        assertNotNull(PreviewData.home.current)
        assertEquals(2, PreviewData.home.games.size)
    }
}
