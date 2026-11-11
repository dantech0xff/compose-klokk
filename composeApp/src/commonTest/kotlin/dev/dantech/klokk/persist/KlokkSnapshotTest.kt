package dev.dantech.klokk.persist

import dev.dantech.klokk.movement.SaverShow
import dev.dantech.klokk.state.City
import dev.dantech.klokk.state.ClockStyle
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
    fun clockStyleRoundTripsAndAnOldSaveStaysFlat() {
        val styled = KlokkSnapshot(clockStyle = ClockStyle.NEON, rippleDigits = true)
        val decoded = decodeSnapshot(encodeSnapshot(styled))
        assertEquals(ClockStyle.NEON, decoded.clockStyle)
        assertTrue(decoded.rippleDigits)
        val state = KlokkState()
        decoded.applyTo(state)
        assertEquals(ClockStyle.NEON, state.clockStyle)
        assertTrue(state.rippleDigits)
        assertEquals(ClockStyle.NEON, state.capture().clockStyle)

        val legacy = """{"schema":1,"themeMode":"LIGHT"}"""
        val old = decodeSnapshot(legacy)
        assertEquals(ClockStyle.FLAT, old.clockStyle)
        assertFalse(old.rippleDigits)
        assertEquals(ClockStyle.FLAT, KlokkState().clockStyle)
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
