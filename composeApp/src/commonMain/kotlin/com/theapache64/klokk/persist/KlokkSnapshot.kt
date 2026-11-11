package com.theapache64.klokk.persist

import com.theapache64.klokk.movement.SaverShow
import com.theapache64.klokk.state.Alarm
import com.theapache64.klokk.state.City
import com.theapache64.klokk.state.DEFAULT_NEXT_ALARM_ID
import com.theapache64.klokk.state.FocusSession
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.state.ThemeMode
import com.theapache64.klokk.state.defaultAlarms
import kotlin.time.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The document written to disk. [schema] is the version of this shape.
 * The open tab and the paywall are not part of it.
 * [timerEndEpochMs] and [snoozeEpochMs] are absolute times so a dead process
 * can still be woken. Zero means none.
 */
@Serializable
data class KlokkSnapshot(
    val schema: Int = SCHEMA,
    val alarms: List<Alarm> = defaultAlarms(),
    val nextAlarmId: Int = DEFAULT_NEXT_ALARM_ID,
    val cities: List<City> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val saverShow: SaverShow = SaverShow.SHUFFLE,
    val focusSessions: List<FocusSession> = emptyList(),
    val plus: Boolean = false,
    val timerEndEpochMs: Long = 0,
    val timerTotalSeconds: Int = 300,
    val snoozeEpochMs: Long = 0,
) {
    companion object {
        const val SCHEMA = 1

        fun fresh() = KlokkSnapshot()
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

fun encodeSnapshot(snapshot: KlokkSnapshot): String = json.encodeToString(snapshot)

/**
 * Decode a stored document.
 *
 * A missing or unreadable payload returns [KlokkSnapshot.fresh]. Missing
 * fields use this version's defaults. Unknown keys are skipped. The next
 * save writes only the fields this version knows.
 */
fun decodeSnapshot(raw: String?): KlokkSnapshot {
    if (raw.isNullOrBlank()) return KlokkSnapshot.fresh()
    return try {
        json.decodeFromString<KlokkSnapshot>(raw)
    } catch (_: Exception) {
        KlokkSnapshot.fresh()
    }
}

fun KlokkState.capture(): KlokkSnapshot = KlokkSnapshot(
    alarms = alarms.toList(),
    nextAlarmId = nextAlarmId,
    cities = cities.toList(),
    themeMode = themeMode,
    saverShow = saverShow,
    focusSessions = focusSessions.toList(),
    plus = plus,
    timerEndEpochMs = if (tmRunning && tmEndTs > 0) tmEndTs else 0,
    timerTotalSeconds = tmTotal,
    snoozeEpochMs = snoozeUntil,
)

fun KlokkSnapshot.applyTo(state: KlokkState) {
    state.alarms.clear()
    state.alarms.addAll(alarms)
    state.nextAlarmId = nextAlarmId
    state.cities.clear()
    state.cities.addAll(cities)
    state.themeMode = themeMode
    state.saverShow = saverShow
    state.focusSessions.clear()
    state.focusSessions.addAll(focusSessions)
    state.plus = plus
    state.snoozeUntil = snoozeEpochMs
    state.tmTotal = timerTotalSeconds
    val now = Clock.System.now().toEpochMilliseconds()
    if (timerEndEpochMs > now) {
        state.tmRunning = true
        state.tmEndTs = timerEndEpochMs
        state.tmRemaining = ((timerEndEpochMs - now) / 1000).toInt().coerceAtLeast(0)
    } else {
        state.tmRunning = false
        state.tmEndTs = 0
        state.tmRemaining = timerTotalSeconds
    }
}
