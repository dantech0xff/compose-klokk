package com.theapache64.klokk.util

import com.theapache64.klokk.state.Alarm
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class AlarmOccurrenceTest {

    private val zone = TimeZone.currentSystemDefault()

    @Test
    fun nextAlarmPicksTheSoonestEnabledFutureTime() {
        val now = at(2026, 11, 11, 8, 0)
        val later = alarm(9, 30, id = 2)
        val sooner = alarm(9, 0, id = 1)
        val disabled = alarm(8, 30, id = 3, on = false)
        val next = KlokkFormat.nextAlarm(listOf(later, disabled, sooner), now)
        assertEquals(1, next!!.alarm.id)
        assertEquals(at(2026, 11, 11, 9, 0).toEpochMilliseconds() - now.toEpochMilliseconds(), next.ms)
    }

    @Test
    fun aPastOnceAlarmMovesToTomorrow() {
        val now = at(2026, 11, 11, 8, 0)
        val next = KlokkFormat.nextAlarm(listOf(alarm(7, 0)), now)
        assertEquals(at(2026, 11, 12, 7, 0).toEpochMilliseconds() - now.toEpochMilliseconds(), next!!.ms)
    }

    @Test
    fun anExactNowOnceAlarmAlsoMovesToTomorrow() {
        val now = at(2026, 11, 11, 8, 0)
        val next = KlokkFormat.nextAlarm(listOf(alarm(8, 0)), now)
        assertEquals(at(2026, 11, 12, 8, 0).toEpochMilliseconds() - now.toEpochMilliseconds(), next!!.ms)
    }

    @Test
    fun aRepeatingAlarmSkipsDaysThatAreOff() {
        val now = at(2026, 11, 11, 8, 0)
        val today = dayIndex(now)
        val tomorrow = (today + 1) % 7
        val next = KlokkFormat.nextAlarm(listOf(alarm(9, 0, days = flags(tomorrow))), now)
        assertEquals(at(2026, 11, 12, 9, 0).toEpochMilliseconds() - now.toEpochMilliseconds(), next!!.ms)
    }

    @Test
    fun aRepeatingAlarmWhoseTimeAlreadyPassedWaitsAWeek() {
        val now = at(2026, 11, 11, 8, 0)
        val today = dayIndex(now)
        val next = KlokkFormat.nextAlarm(listOf(alarm(7, 0, days = flags(today))), now)
        val sameWeekday = LocalDateTime(2026, 11, 11, 7, 0).date.plus(7, DateTimeUnit.DAY)
        val expected = LocalDateTime(sameWeekday, kotlinx.datetime.LocalTime(7, 0))
            .toInstant(zone)
            .toEpochMilliseconds() - now.toEpochMilliseconds()
        assertEquals(expected, next!!.ms)
    }

    @Test
    fun noEnabledAlarmMeansNoNextOccurrence() {
        val now = at(2026, 11, 11, 8, 0)
        assertNull(KlokkFormat.nextAlarm(listOf(alarm(9, 0, on = false)), now))
        assertNull(KlokkFormat.nextAlarm(emptyList(), now))
    }

    @Test
    fun firesThisMinuteMatchesTheFirstHalfOfTheMinute() {
        val now = at(2026, 11, 11, 7, 30, 10)
        val local = now.toLocalDateTime(zone)
        val ringing = alarm(7, 30)
        assertTrue(KlokkFormat.firesThisMinute(ringing, local, now.toEpochMilliseconds(), null))
        assertFalse(
            KlokkFormat.firesThisMinute(
                ringing,
                at(2026, 11, 11, 7, 30, 30).toLocalDateTime(zone),
                at(2026, 11, 11, 7, 30, 30).toEpochMilliseconds(),
                null,
            ),
        )
    }

    @Test
    fun firesThisMinuteRespectsEnabledRepeatAndAlreadyRung() {
        val now = at(2026, 11, 11, 7, 30, 5)
        val local = now.toLocalDateTime(zone)
        val ms = now.toEpochMilliseconds()
        val today = dayIndex(now)
        assertFalse(KlokkFormat.firesThisMinute(alarm(7, 30, on = false), local, ms, null))
        assertFalse(KlokkFormat.firesThisMinute(alarm(7, 31), local, ms, null))
        assertFalse(KlokkFormat.firesThisMinute(alarm(7, 30, days = flags((today + 1) % 7)), local, ms, null))
        assertTrue(KlokkFormat.firesThisMinute(alarm(7, 30, days = flags(today)), local, ms, null))
        assertFalse(KlokkFormat.firesThisMinute(alarm(7, 30), local, ms, ms / 60_000))
        assertTrue(KlokkFormat.firesThisMinute(alarm(7, 30), local, ms, ms / 60_000 - 1))
    }

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0): Instant {
        return LocalDateTime(year, month, day, hour, minute, second).toInstant(zone)
    }

    private fun dayIndex(now: Instant): Int {
        val local = now.toLocalDateTime(zone)
        return (local.dayOfWeek.ordinal + 1) % 7
    }

    private fun flags(vararg on: Int) = List(7) { it in on }

    private fun alarm(
        hour: Int,
        minute: Int,
        on: Boolean = true,
        days: List<Boolean> = List(7) { false },
        id: Int = 1,
    ) = Alarm(id, hour, minute, "", days, on)
}
