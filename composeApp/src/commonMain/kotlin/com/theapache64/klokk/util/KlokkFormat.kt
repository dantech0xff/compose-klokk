package com.theapache64.klokk.util

import com.theapache64.klokk.state.Alarm
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.offsetIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.time.Instant

object KlokkFormat {

    fun pad(n: Int): String = n.toString().padStart(2, '0')

    fun fmtHM(h: Int, m: Int): String = "${pad(h)}:${pad(m)}"

    /** Substitutes %1$s/%2$d-style placeholders; resources carry the word order. */
    internal fun f(template: String, vararg args: Any): String {
        var r = template
        args.forEachIndexed { i, a ->
            r = r.replace("%${i + 1}\$s", a.toString())
                .replace("%${i + 1}\$d", a.toString())
        }
        return r
    }

    /**
     * Localized "Wednesday, 23 September". [dayNames] is Mon-first
     * (DayOfWeek.ordinal), [monthNames] Jan-first (Month.ordinal).
     */
    fun dateLine(
        now: Instant,
        tz: TimeZone,
        template: String,
        dayNames: List<String>,
        monthNames: List<String>,
    ): String {
        val l = now.toLocalDateTime(tz)
        return f(template, dayNames[l.dayOfWeek.ordinal], l.day, monthNames[l.month.ordinal])
    }

    /** Localized "Wednesday, September 23" (lock screen ordering). */
    fun lockDate(
        now: Instant,
        tz: TimeZone,
        template: String,
        dayNames: List<String>,
        monthNames: List<String>,
    ): String {
        val l = now.toLocalDateTime(tz)
        return f(template, dayNames[l.dayOfWeek.ordinal], monthNames[l.month.ordinal], l.day)
    }

    /** Index into Alarm.days (0 = Sunday) for the given local date-time. */
    private fun dayIndex(l: LocalDateTime): Int = (l.dayOfWeek.ordinal + 1) % 7

    /** [shortDayNames] is Sun-first, matching [Alarm.days]. */
    fun daysText(
        days: List<Boolean>,
        every: String,
        once: String,
        weekdays: String,
        weekend: String,
        shortDayNames: List<String>,
    ): String {
        val n = days.count { it }
        if (n == 7) return every
        if (n == 0) return once
        val allWeekdays = (1..5).all { days[it] } && !days[0] && !days[6]
        if (allWeekdays) return weekdays
        if (n == 2 && days[0] && days[6]) return weekend
        return shortDayNames.filterIndexed { i, _ -> days[i] }.joinToString(", ")
    }

    class NextAlarm(val alarm: Alarm, val ms: Long)

    /** Soonest future firing of any enabled alarm within the next 8 days. */
    fun nextAlarm(alarms: List<Alarm>, now: Instant): NextAlarm? {
        val tz = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date
        var best: NextAlarm? = null
        for (a in alarms) {
            if (!a.on) continue
            val anyDay = a.days.any { it }
            for (o in 0..7) {
                val date = today.plus(o, DateTimeUnit.DAY)
                val candidate = LocalDateTime(date, LocalTime(a.h, a.m, 0))
                    .toInstant(tz)
                if (candidate <= now) continue
                if (!anyDay || a.days[dayIndex(candidate.toLocalDateTime(tz))]) {
                    val ms = candidate.toEpochMilliseconds() - now.toEpochMilliseconds()
                    if (best == null || ms < best.ms) best = NextAlarm(a, ms)
                    break
                }
            }
        }
        return best
    }

    /** Localized "in 2d 3h" / "in 7h 20m" / "in 45m"; args are templates. */
    fun relTime(
        ms: Long,
        daysHours: String,
        hoursMinutes: String,
        hours: String,
        minutes: String,
    ): String {
        val totalM = (ms / 60000.0).roundToInt()
        val h = totalM / 60
        val m = totalM % 60
        return when {
            h >= 48 -> f(daysHours, h / 24, h % 24)
            h > 0 -> if (m > 0) f(hoursMinutes, h, m) else f(hours, h)
            else -> f(minutes, maxOf(1, m))
        }
    }

    /** Localized "45 s" / "20 min" / "1 h 5 min"; args are templates. */
    fun fmtDur(
        ms: Long,
        seconds: String,
        minutes: String,
        hours: String,
        hoursMinutes: String,
    ): String {
        val m = (ms / 60000.0).roundToInt()
        return when {
            m < 1 -> f(seconds, floor(ms / 1000.0).toInt())
            m < 60 -> f(minutes, m)
            else -> {
                val h = m / 60
                val rem = m % 60
                if (rem == 0) f(hours, h) else f(hoursMinutes, h, rem)
            }
        }
    }

    class CityInfo(val time: String, val sub: String)

    /**
     * Local time in [tzId] plus a localized "Today, +7h" / "Tomorrow, −5.5h"
     * style sub built from [today]/[tomorrow]/[yesterday] and the
     * [dayOffset] template.
     */
    fun cityInfo(
        tzId: String,
        now: Instant,
        today: String,
        tomorrow: String,
        yesterday: String,
        dayOffset: String,
    ): CityInfo {
        return try {
            val tz = TimeZone.of(tzId)
            val system = TimeZone.currentSystemDefault()
            val there = now.toLocalDateTime(tz)
            val here = now.toLocalDateTime(system)
            val offMin =
                (now.offsetIn(tz).totalSeconds - now.offsetIn(system).totalSeconds) / 60
            val day = when {
                there.date == here.date -> today
                there.date > here.date -> tomorrow
                else -> yesterday
            }
            val off = if (offMin == 0) "" else {
                val h = (offMin / 30f).roundToInt() / 2f
                val sign = if (h > 0) "+" else "−"
                val absH = kotlin.math.abs(h)
                val text = if (absH % 1f == 0f) absH.toInt().toString() else absH.toString()
                "$sign${text}h"
            }
            CityInfo(
                time = "${pad(there.hour)}:${pad(there.minute)}",
                sub = if (off.isEmpty()) day else f(dayOffset, day, off),
            )
        } catch (e: Exception) {
            CityInfo("--:--", "")
        }
    }

    /** True between 22:00 and 06:00 local — background cells dim, digits stay lit. */
    fun isNight(now: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val h = now.toLocalDateTime(tz).hour
        return h >= 22 || h < 6
    }

    fun hm(a: Int, b: Int): IntArray =
        intArrayOf(a / 10, a % 10, b / 10, b % 10)
}
