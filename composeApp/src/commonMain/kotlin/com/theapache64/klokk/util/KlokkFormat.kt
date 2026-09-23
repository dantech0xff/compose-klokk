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

    private fun titleCase(name: String): String =
        name.lowercase().replaceFirstChar { it.uppercase() }

    /** "Wednesday, 23 September" */
    fun dateLine(now: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): String {
        val l = now.toLocalDateTime(tz)
        return "${titleCase(l.dayOfWeek.name)}, ${l.dayOfMonth} ${titleCase(l.month.name)}"
    }

    /** "Wednesday, September 23" (lock screen ordering) */
    fun lockDate(now: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): String {
        val l = now.toLocalDateTime(tz)
        return "${titleCase(l.dayOfWeek.name)}, ${titleCase(l.month.name)} ${l.dayOfMonth}"
    }

    /** Index into Alarm.days (0 = Sunday) for the given local date-time. */
    private fun dayIndex(l: LocalDateTime): Int = (l.dayOfWeek.ordinal + 1) % 7

    fun daysText(days: List<Boolean>): String {
        val n = days.count { it }
        if (n == 7) return "Every day"
        if (n == 0) return "Once"
        val weekdays = (1..5).all { days[it] } && !days[0] && !days[6]
        if (weekdays) return "Weekdays"
        if (n == 2 && days[0] && days[6]) return "Weekend"
        val names = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return names.filterIndexed { i, _ -> days[i] }.joinToString(", ")
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

    /** "in 2d 3h" / "in 7h 20m" / "in 45m" */
    fun relTime(ms: Long): String {
        val totalM = (ms / 60000.0).roundToInt()
        val h = totalM / 60
        val m = totalM % 60
        return when {
            h >= 48 -> "in ${h / 24}d ${h % 24}h"
            h > 0 -> if (m > 0) "in ${h}h ${m}m" else "in ${h}h"
            else -> "in ${maxOf(1, m)}m"
        }
    }

    /** "45 s" / "20 min" / "1 h 5 min" */
    fun fmtDur(ms: Long): String {
        val m = (ms / 60000.0).roundToInt()
        return when {
            m < 1 -> "${floor(ms / 1000.0).toInt()} s"
            m < 60 -> "$m min"
            else -> {
                val h = m / 60
                val rem = m % 60
                if (rem == 0) "$h h" else "$h h $rem min"
            }
        }
    }

    class CityInfo(val time: String, val sub: String)

    /** Local time in [tzId] plus "Today, +7h" / "Tomorrow, −5.5h" style sub. */
    fun cityInfo(tzId: String, now: Instant): CityInfo {
        return try {
            val tz = TimeZone.of(tzId)
            val system = TimeZone.currentSystemDefault()
            val there = now.toLocalDateTime(tz)
            val here = now.toLocalDateTime(system)
            val offMin =
                (now.offsetIn(tz).totalSeconds - now.offsetIn(system).totalSeconds) / 60
            val day = when {
                there.date == here.date -> "Today"
                there.date > here.date -> "Tomorrow"
                else -> "Yesterday"
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
                sub = if (off.isEmpty()) day else "$day, $off",
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
