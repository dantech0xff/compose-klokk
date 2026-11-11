package com.theapache64.klokk.schedule

import com.theapache64.klokk.persist.KlokkSnapshot
import com.theapache64.klokk.state.Alarm
import com.theapache64.klokk.util.KlokkFormat
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WakePlanTest {

    private val zone = TimeZone.currentSystemDefault()

    @Test
    fun theNextArmedAlarmMatchesNextAlarm() {
        val now = at(2026, 11, 11, 8, 0)
        val alarms = listOf(alarm(7, 0, id = 1), alarm(9, 15, id = 2, on = false), alarm(9, 0, id = 3))
        val plan = planWakes(KlokkSnapshot(alarms = alarms), now)
        val expected = KlokkFormat.nextAlarm(alarms, now)!!
        val soonest = plan.filter { it.kind == WakeKind.ALARM }.minBy { it.triggerEpochMs }
        assertEquals(3, soonest.alarmId)
        assertEquals(now.toEpochMilliseconds() + expected.ms, soonest.triggerEpochMs)
    }

    @Test
    fun aRepeatingAlarmSkipsOffDaysAndCanArmSeveralWeeks() {
        val now = at(2026, 11, 11, 8, 0)
        val today = dayIndex(now)
        val onlyToday = alarm(7, 0, days = flags(today))
        val plan = planWakes(KlokkSnapshot(alarms = listOf(onlyToday)), now, occurrencesPerAlarm = 3)
        assertEquals(3, plan.size)
        val week = 7 * 24 * 3_600_000L
        assertEquals(plan[0].triggerEpochMs + week, plan[1].triggerEpochMs)
        assertEquals(plan[1].triggerEpochMs + week, plan[2].triggerEpochMs)
    }

    @Test
    fun timerAndSnoozeAreArmedOnlyWhileTheyAreStillAhead() {
        val now = at(2026, 11, 11, 8, 0)
        val nowMs = now.toEpochMilliseconds()
        val ahead = planWakes(
            KlokkSnapshot(alarms = emptyList(), timerEndEpochMs = nowMs + 60_000, snoozeEpochMs = nowMs + 120_000),
            now,
        )
        assertEquals(setOf(WakeKind.TIMER, WakeKind.SNOOZE), ahead.map { it.kind }.toSet())
        val behind = planWakes(
            KlokkSnapshot(alarms = emptyList(), timerEndEpochMs = nowMs - 1, snoozeEpochMs = nowMs),
            now,
        )
        assertTrue(behind.isEmpty())
    }

    @Test
    fun firingAOneShotAlarmTurnsItOffAndClearsATimer() {
        val once = alarm(7, 0, id = 4)
        val repeating = alarm(8, 0, id = 5, days = flags(1, 2, 3, 4, 5))
        val snapshot = KlokkSnapshot(alarms = listOf(once, repeating), timerEndEpochMs = 50, snoozeEpochMs = 60)
        val afterAlarm = afterFire(snapshot, WakeRequest("alarm-4-1", 1, WakeKind.ALARM, 4, "Alarm"))
        assertEquals(false, afterAlarm.alarms.first { it.id == 4 }.on)
        assertEquals(true, afterAlarm.alarms.first { it.id == 5 }.on)
        val afterRepeat = afterFire(snapshot, WakeRequest("alarm-5-1", 1, WakeKind.ALARM, 5, "Alarm"))
        assertEquals(true, afterRepeat.alarms.first { it.id == 5 }.on)
        assertEquals(0, afterFire(snapshot, WakeRequest("timer", 50, WakeKind.TIMER, null, "Timer")).timerEndEpochMs)
        assertEquals(0, afterFire(snapshot, WakeRequest("snooze", 60, WakeKind.SNOOZE, null, "Alarm")).snoozeEpochMs)
    }

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int) =
        LocalDateTime(year, month, day, hour, minute).toInstant(zone)

    private fun dayIndex(now: kotlin.time.Instant): Int {
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
