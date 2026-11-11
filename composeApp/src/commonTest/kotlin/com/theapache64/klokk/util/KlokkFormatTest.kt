package com.theapache64.klokk.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class KlokkFormatTest {

    @Test
    fun padAndHourMinute() {
        assertEquals("07:05", KlokkFormat.fmtHM(7, 5))
        assertEquals(listOf(1, 2, 3, 4), KlokkFormat.hm(12, 34).toList())
    }

    @Test
    fun daysTextUsesTheNamedGroups() {
        val names = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        assertEquals("Every day", KlokkFormat.daysText(List(7) { true }, "Every day", "Once", "Weekdays", "Weekend", names))
        assertEquals("Once", KlokkFormat.daysText(List(7) { false }, "Every day", "Once", "Weekdays", "Weekend", names))
        assertEquals("Weekdays", KlokkFormat.daysText(listOf(false, true, true, true, true, true, false), "Every day", "Once", "Weekdays", "Weekend", names))
        assertEquals("Weekend", KlokkFormat.daysText(listOf(true, false, false, false, false, false, true), "Every day", "Once", "Weekdays", "Weekend", names))
        assertEquals("Mon, Wed", KlokkFormat.daysText(listOf(false, true, false, true, false, false, false), "Every day", "Once", "Weekdays", "Weekend", names))
    }

    @Test
    fun dateLineAndLockDateFollowTheTemplate() {
        val now = Instant.parse("2026-11-11T00:00:00Z")
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        assertEquals(
            "Wed, 11 Nov",
            KlokkFormat.dateLine(now, TimeZone.UTC, "%1\$s, %2\$d %3\$s", days, months),
        )
        assertEquals(
            "Wed, Nov 11",
            KlokkFormat.lockDate(now, TimeZone.UTC, "%1\$s, %2\$s %3\$d", days, months),
        )
    }

    @Test
    fun relativeTimeRoundsToTheCoarserUnit() {
        assertEquals("1 m", KlokkFormat.relTime(20_000, "%1\$d d %2\$d h", "%1\$d h %2\$d m", "%1\$d h", "%1\$d m"))
        assertEquals("45 m", KlokkFormat.relTime(45 * 60_000L, "%1\$d d %2\$d h", "%1\$d h %2\$d m", "%1\$d h", "%1\$d m"))
        assertEquals("3 h", KlokkFormat.relTime(3 * 3_600_000L, "%1\$d d %2\$d h", "%1\$d h %2\$d m", "%1\$d h", "%1\$d m"))
        assertEquals("2 h 20 m", KlokkFormat.relTime((2 * 60 + 20) * 60_000L, "%1\$d d %2\$d h", "%1\$d h %2\$d m", "%1\$d h", "%1\$d m"))
        assertEquals("2 d 2 h", KlokkFormat.relTime(50 * 3_600_000L, "%1\$d d %2\$d h", "%1\$d h %2\$d m", "%1\$d h", "%1\$d m"))
    }

    @Test
    fun durationFormat() {
        assertEquals("20 s", KlokkFormat.fmtDur(20_000, "%1\$d s", "%1\$d min", "%1\$d h", "%1\$d h %2\$d min"))
        assertEquals("45 min", KlokkFormat.fmtDur(45 * 60_000L, "%1\$d s", "%1\$d min", "%1\$d h", "%1\$d h %2\$d min"))
        assertEquals("1 h", KlokkFormat.fmtDur(3_600_000, "%1\$d s", "%1\$d min", "%1\$d h", "%1\$d h %2\$d min"))
        assertEquals("1 h 5 min", KlokkFormat.fmtDur((60 + 5) * 60_000L, "%1\$d s", "%1\$d min", "%1\$d h", "%1\$d h %2\$d min"))
    }

    @Test
    fun cityInfoShowsTheZoneLocalTime() {
        val zone = TimeZone.currentSystemDefault()
        val now = Instant.parse("2026-06-15T15:30:00Z")
        val local = now.toLocalDateTime(zone)
        val info = KlokkFormat.cityInfo(zone.id, now, "Today", "Tomorrow", "Yesterday", "%1\$s, %2\$s")
        assertEquals(
            "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}",
            info.time,
        )
        assertEquals("Today", info.sub)
    }

    @Test
    fun cityInfoMarksABadZone() {
        val info = KlokkFormat.cityInfo("Not/AZone", Instant.parse("2026-06-15T15:30:00Z"), "Today", "Tomorrow", "Yesterday", "%1\$s, %2\$s")
        assertEquals("--:--", info.time)
        assertEquals("", info.sub)
    }

    @Test
    fun nightIsTenPmThroughSixAm() {
        val utc = TimeZone.UTC
        assertTrue(KlokkFormat.isNight(hour(22), utc))
        assertTrue(KlokkFormat.isNight(hour(5), utc))
        assertFalse(KlokkFormat.isNight(hour(6), utc))
        assertFalse(KlokkFormat.isNight(hour(21), utc))
    }

    private fun hour(h: Int) = LocalDateTime(2026, 1, 15, h, 0, 0).toInstant(TimeZone.UTC)
}
