package dev.dantech.klokk.schedule

import dev.dantech.klokk.persist.KlokkSnapshot
import dev.dantech.klokk.persist.applyTo
import dev.dantech.klokk.persist.defaultKlokkRepository
import kotlin.time.Clock
import kotlin.time.Instant

/** Called from Swift when the user taps Snooze, Dismiss, or the notification. */
fun onIosAlarmAction(requestId: String, action: String) {
    val repo = defaultKlokkRepository()
    kotlinx.coroutines.runBlocking {
        val now = Clock.System.now()
        val snapshot = repo.load()
        val request = requestFor(snapshot, requestId, now.toEpochMilliseconds()) ?: return@runBlocking
        val fired = afterFire(snapshot, request)
        val next = if (action == "SNOOZE" && request.kind != WakeKind.TIMER) {
            fired.copy(snoozeEpochMs = now.toEpochMilliseconds() + 9 * 60_000)
        } else {
            fired.copy(snoozeEpochMs = 0)
        }
        repo.save(next)
        KlokkRuntime.state?.let { next.applyTo(it) }
        defaultAlarmScheduler().replaceAll(planWakes(next, now, alarmScheduleHorizon()))
    }
}

private fun requestFor(snapshot: KlokkSnapshot, requestId: String, nowMs: Long): WakeRequest? {
    val planned = planWakes(snapshot, Instant.fromEpochMilliseconds(nowMs - 60_000), alarmScheduleHorizon())
        .firstOrNull { it.requestId == requestId }
    if (planned != null) return planned
    return when {
        requestId == "timer" -> WakeRequest(requestId, nowMs, WakeKind.TIMER, null, "Timer")
        requestId == "snooze" -> WakeRequest(requestId, nowMs, WakeKind.SNOOZE, null, "Alarm")
        requestId.startsWith("alarm-") -> {
            val id = requestId.removePrefix("alarm-").substringBefore("-").toIntOrNull()
            WakeRequest(requestId, nowMs, WakeKind.ALARM, id, "Alarm")
        }
        else -> null
    }
}
