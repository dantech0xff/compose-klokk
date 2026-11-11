package dev.dantech.klokk.schedule

import dev.dantech.klokk.persist.KlokkSnapshot
import dev.dantech.klokk.state.Alarm
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

enum class WakeKind { ALARM, TIMER, SNOOZE }

data class WakeRequest(
    val requestId: String,
    val triggerEpochMs: Long,
    val kind: WakeKind,
    val alarmId: Int?,
    val title: String,
)

private const val MAX_REQUESTS = 60

/**
 * The wakes the platform should arm.
 *
 * [occurrencesPerAlarm] is 1 on Android, where the receiver arms the following
 * day after a fire. iOS gets several occurrences because a delivered
 * notification does not run app code.
 */
fun planWakes(
    snapshot: KlokkSnapshot,
    now: Instant,
    occurrencesPerAlarm: Int = 1,
): List<WakeRequest> {
    val zone = TimeZone.currentSystemDefault()
    val requests = mutableListOf<WakeRequest>()
    for (alarm in snapshot.alarms) {
        if (!alarm.on) continue
        for (trigger in upcoming(alarm, now, zone, occurrencesPerAlarm)) {
            if (requests.size >= MAX_REQUESTS) return requests
            requests.add(
                WakeRequest(
                    requestId = "alarm-${alarm.id}-$trigger",
                    triggerEpochMs = trigger,
                    kind = WakeKind.ALARM,
                    alarmId = alarm.id,
                    title = alarm.label.ifBlank { "Alarm" },
                ),
            )
        }
    }
    val timerEnd = snapshot.timerEndEpochMs
    if (timerEnd > now.toEpochMilliseconds() && requests.size < MAX_REQUESTS) {
        requests.add(
            WakeRequest(
                requestId = "timer",
                triggerEpochMs = timerEnd,
                kind = WakeKind.TIMER,
                alarmId = null,
                title = "Timer",
            ),
        )
    }
    val snooze = snapshot.snoozeEpochMs
    if (snooze > now.toEpochMilliseconds() && requests.size < MAX_REQUESTS) {
        requests.add(
            WakeRequest(
                requestId = "snooze",
                triggerEpochMs = snooze,
                kind = WakeKind.SNOOZE,
                alarmId = null,
                title = "Alarm",
            ),
        )
    }
    return requests
}

/** State after a wake has fired. A one-shot alarm turns off. A timer or snooze clears. */
fun afterFire(snapshot: KlokkSnapshot, request: WakeRequest): KlokkSnapshot {
    return when (request.kind) {
        WakeKind.TIMER -> snapshot.copy(timerEndEpochMs = 0)
        WakeKind.SNOOZE -> snapshot.copy(snoozeEpochMs = 0)
        WakeKind.ALARM -> {
            val alarms = snapshot.alarms.map { alarm ->
                if (alarm.id == request.alarmId && alarm.days.none { it }) alarm.copy(on = false) else alarm
            }
            snapshot.copy(alarms = alarms)
        }
    }
}

private fun upcoming(alarm: Alarm, now: Instant, zone: TimeZone, limit: Int): List<Long> {
    if (limit <= 0) return emptyList()
    val today = now.toLocalDateTime(zone).date
    val anyDay = alarm.days.any { it }
    val found = mutableListOf<Long>()
    for (offset in 0..60) {
        if (found.size >= limit) break
        val date = today.plus(offset, DateTimeUnit.DAY)
        val candidate = LocalDateTime(date, LocalTime(alarm.h, alarm.m)).toInstant(zone)
        if (candidate <= now) continue
        val day = candidate.toLocalDateTime(zone)
        val index = (day.dayOfWeek.ordinal + 1) % 7
        if (!anyDay || alarm.days.getOrElse(index) { false }) {
            found.add(candidate.toEpochMilliseconds())
            if (!anyDay) break
        }
    }
    return found
}
