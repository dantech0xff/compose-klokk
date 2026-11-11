package dev.dantech.klokk.persist

import dev.dantech.klokk.movement.SaverShow
import dev.dantech.klokk.state.City
import dev.dantech.klokk.state.ClockDepth
import dev.dantech.klokk.state.FocusSession
import dev.dantech.klokk.state.KlokkState
import dev.dantech.klokk.state.ThemeMode
import dev.dantech.klokk.state.defaultAlarms
import kotlin.time.Clock
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
    fun clockDepthRoundTripsAndASaveWithoutItOpensRaised() {
        val flat = KlokkSnapshot(clockDepth = ClockDepth.FLAT)
        val decoded = decodeSnapshot(encodeSnapshot(flat))
        assertEquals(ClockDepth.FLAT, decoded.clockDepth)
        val state = KlokkState()
        decoded.applyTo(state)
        assertEquals(ClockDepth.FLAT, state.clockDepth)
        assertEquals(ClockDepth.FLAT, state.capture().clockDepth)

        val legacy = """{"schema":1,"plus":false,"themeMode":"DARK"}"""
        assertEquals(ClockDepth.RAISED, decodeSnapshot(legacy).clockDepth)
        assertEquals(ClockDepth.RAISED, KlokkState().clockDepth)
    }

    @Test
    fun corruptJsonFallsBackToDefaults() {
        assertEquals(KlokkSnapshot.fresh(), decodeSnapshot("{"))
        assertEquals(KlokkSnapshot.fresh(), decodeSnapshot(""))
    }

    @Test
    fun applyRestoresARunningTimerAndLeavesTheOpenTabAlone() {
        val end = Clock.System.now().toEpochMilliseconds() + 90_000
        val state = KlokkState().also { it.tab = dev.dantech.klokk.state.KlokkTab.TIMERS }
        KlokkSnapshot(plus = true, timerEndEpochMs = end, timerTotalSeconds = 300).applyTo(state)
        assertTrue(state.plus)
        assertTrue(state.tmRunning)
        assertEquals(end, state.tmEndTs)
        assertEquals(dev.dantech.klokk.state.KlokkTab.TIMERS, state.tab)
        assertEquals(end, state.capture().timerEndEpochMs)
    }

    @Test
    fun memoryRepositoryReturnsWhatWasSaved() = runBlocking {
        val repo = MemoryKlokkRepository()
        val snapshot = KlokkSnapshot(plus = true, saverShow = SaverShow.WAVE)
        repo.save(snapshot)
        assertEquals(snapshot, repo.load())
    }
}
