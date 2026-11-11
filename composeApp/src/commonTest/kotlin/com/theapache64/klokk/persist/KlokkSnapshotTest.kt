package com.theapache64.klokk.persist

import com.theapache64.klokk.movement.SaverShow
import com.theapache64.klokk.state.City
import com.theapache64.klokk.state.FocusSession
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.state.ThemeMode
import com.theapache64.klokk.state.defaultAlarms
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KlokkSnapshotTest {

    @Test
    fun aMissingFileIsTheSameClockTheAppStartsWith() {
        val decoded = decodeSnapshot(null)
        val state = KlokkState()
        assertEquals(state.capture(), decoded)
        assertEquals(defaultAlarms(), decoded.alarms)
        assertFalse(decoded.plus)
    }

    @Test
    fun roundTripKeepsAlarmsCitiesThemeSaverFocusAndPlus() {
        val original = KlokkSnapshot(
            alarms = defaultAlarms(),
            nextAlarmId = 8,
            cities = listOf(City("Hanoi", "Asia/Bangkok")),
            themeMode = ThemeMode.DARK,
            saverShow = SaverShow.RIPPLE,
            focusSessions = listOf(FocusSession(1_000L, 25 * 60_000L)),
            plus = true,
        )
        assertEquals(original, decodeSnapshot(encodeSnapshot(original)))
    }

    @Test
    fun aNewerSchemaKeepsTheFieldsThisVersionUnderstands() {
        val raw = """
            {"schema":9,"plus":true,"themeMode":"LIGHT","cities":[{"name":"Oslo","tz":"Europe/Oslo"}],"extra":"ignored"}
        """.trim()
        val decoded = decodeSnapshot(raw)
        assertTrue(decoded.plus)
        assertEquals(ThemeMode.LIGHT, decoded.themeMode)
        assertEquals(listOf(City("Oslo", "Europe/Oslo")), decoded.cities)
        assertEquals(defaultAlarms(), decoded.alarms)
    }

    @Test
    fun corruptJsonFallsBackToDefaults() {
        assertEquals(KlokkSnapshot.fresh(), decodeSnapshot("{"))
        assertEquals(KlokkSnapshot.fresh(), decodeSnapshot(""))
    }

    @Test
    fun applyAndCaptureDoNotTouchARunningTimer() {
        val state = KlokkState().also {
            it.tmRunning = true
            it.tmRemaining = 12
            it.tab = com.theapache64.klokk.state.KlokkTab.TIMERS
        }
        KlokkSnapshot(plus = true, themeMode = ThemeMode.DARK).applyTo(state)
        assertTrue(state.plus)
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertTrue(state.tmRunning)
        assertEquals(12, state.tmRemaining)
        assertEquals(com.theapache64.klokk.state.KlokkTab.TIMERS, state.tab)
        assertTrue(state.capture().plus)
    }

    @Test
    fun memoryRepositoryReturnsWhatWasSaved() = runBlocking {
        val repo = MemoryKlokkRepository()
        val snapshot = KlokkSnapshot(plus = true, saverShow = SaverShow.WAVE)
        repo.save(snapshot)
        assertEquals(snapshot, repo.load())
    }
}
