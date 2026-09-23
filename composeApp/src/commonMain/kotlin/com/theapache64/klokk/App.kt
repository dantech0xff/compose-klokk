package com.theapache64.klokk

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.composable.KlokkCell
import com.theapache64.klokk.composable.KlokkCellMode
import com.theapache64.klokk.composable.KlokkGrid
import com.theapache64.klokk.movement.KlokkMatrices
import com.theapache64.klokk.movement.SaverShow
import com.theapache64.klokk.state.Alarm
import com.theapache64.klokk.state.KlokkOverlay
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.state.KlokkTab
import com.theapache64.klokk.state.PaywallReason
import com.theapache64.klokk.state.RingKind
import com.theapache64.klokk.theme.KlokkBackground
import com.theapache64.klokk.theme.KlokkTabInactive
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.theme.KlokkTextSecondary
import com.theapache64.klokk.ui.AlarmTab
import com.theapache64.klokk.ui.CitySheet
import com.theapache64.klokk.ui.ClockTab
import com.theapache64.klokk.ui.FocusEndControl
import com.theapache64.klokk.ui.FocusTab
import com.theapache64.klokk.ui.PaywallOverlay
import com.theapache64.klokk.ui.RingOverlay
import com.theapache64.klokk.ui.SaverOverlay
import com.theapache64.klokk.ui.SaverSheet
import com.theapache64.klokk.ui.ScrubTriangle
import com.theapache64.klokk.ui.SettingsTab
import com.theapache64.klokk.ui.TimerTab
import com.theapache64.klokk.generated.resources.Res
import com.theapache64.klokk.generated.resources.caption_drag_hours
import com.theapache64.klokk.generated.resources.caption_drag_minutes
import com.theapache64.klokk.generated.resources.caption_ends_at
import com.theapache64.klokk.generated.resources.caption_focus
import com.theapache64.klokk.generated.resources.caption_next_alarm
import com.theapache64.klokk.generated.resources.caption_no_alarms
import com.theapache64.klokk.generated.resources.caption_paused
import com.theapache64.klokk.generated.resources.caption_snoozed
import com.theapache64.klokk.generated.resources.caption_started
import com.theapache64.klokk.generated.resources.ring_alarm
import com.theapache64.klokk.generated.resources.ring_hint_alarm
import com.theapache64.klokk.generated.resources.ring_hint_timer
import com.theapache64.klokk.generated.resources.ring_time_up
import com.theapache64.klokk.generated.resources.ring_timer
import com.theapache64.klokk.ui.tap
import com.theapache64.klokk.util.KlokkFormat
import com.theapache64.klokk.util.KlokkFormat.NextAlarm
import com.theapache64.klokk.util.dateLine
import com.theapache64.klokk.util.relTime
import com.theapache64.klokk.util.tabLabel
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

const val IS_DEBUG = true
const val COLUMNS = 15
const val ROWS = 8
const val PADDING = 100
const val CLOCK_SIZE = 60
const val CLOCKS_CONTAINER_WIDTH = CLOCK_SIZE * COLUMNS
const val CLOCKS_CONTAINER_HEIGHT = CLOCK_SIZE * ROWS

private val pageEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

@Composable
fun App() {
    val state = remember { KlokkState() }

    // One tick per wall-clock second; drives the clock face, timer and alarms.
    LaunchedEffect(Unit) {
        var lastSecond = -1L
        while (true) {
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val second = nowMs / 1000
            if (second != lastSecond) {
                lastSecond = second
                state.nowMs = nowMs
                onSecond(state, nowMs)
            }
            delay(100)
        }
    }

    // Ring overlay flashes the digits on and off every 700ms.
    LaunchedEffect(state.overlay == KlokkOverlay.RING) {
        while (state.overlay == KlokkOverlay.RING) {
            state.ringOn = !state.ringOn
            delay(700)
        }
    }

    // Screensaver steps through its choreography every 5s.
    LaunchedEffect(state.overlay == KlokkOverlay.SAVER) {
        if (state.overlay == KlokkOverlay.SAVER) {
            delay(600)
            state.saverStep = 0
            while (true) {
                delay(5000)
                state.saverStep++
            }
        } else {
            state.saverStep = -1
        }
    }

    // Focus "End" control fades out 3s after its last reveal.
    LaunchedEffect(state.focusCtl) {
        if (state.focusCtl) {
            delay(3000)
            state.focusCtl = false
        }
    }

    // Mock purchase settles after 900ms.
    LaunchedEffect(state.buying) {
        if (state.buying) {
            delay(900)
            state.onUnlocked()
        }
    }

    val now = Instant.fromEpochMilliseconds(state.nowMs)
    val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = local.hour
    val minute = local.minute
    val second = local.second
    val isNight = KlokkFormat.isNight(now)
    val sweep = second * 6f
    val timeMatrix = KlokkMatrices.digitsMatrix(
        KlokkFormat.hm(hour, minute),
        sweep,
        digitDuration = 1000,
        backgroundDuration = 1000,
    )
    val nextAlarm = KlokkFormat.nextAlarm(state.alarms, now)
    val editingAlarm = state.tab == KlokkTab.ALARM && state.editingId != null
    val heroHidden = state.tab == KlokkTab.ALARM && !editingAlarm
    val heroHeight by animateDpAsState(
        targetValue = if (heroHidden) 0.dp else 192.dp,
        animationSpec = tween(350, easing = pageEasing),
        label = "heroH",
    )
    val heroTopPadding by animateDpAsState(
        targetValue = if (state.focusRunning) 215.dp else 16.dp,
        animationSpec = tween(450, easing = pageEasing),
        label = "heroTop",
    )
    val heroAlpha by animateFloatAsState(
        targetValue = if (heroHidden) 0f else 1f,
        animationSpec = tween(300, easing = pageEasing),
        label = "heroA",
    )
    val scrubs = scrubEnabled(state)
    val scrubHintsAlpha by animateFloatAsState(
        targetValue = if (scrubs) 0.6f else 0f,
        animationSpec = tween(200),
        label = "hintsA",
    )
    val chromeAlpha = when {
        state.sheet != null -> 0.35f
        state.focusRunning -> 0f
        else -> 1f
    }
    val bodyAlpha = if (state.focusRunning) 0f else 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KlokkBackground)
            .pointerInput(Unit) {
                // A tap anywhere while focus runs reveals the End control.
                // requireUnconsumed=false: the focus dead zone consumes every down,
                // and consumed changes still reach this handler.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var moved = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: break
                        if (abs(change.position.y - down.position.y) > 8f) {
                            moved = true
                        }
                        if (!change.pressed) break
                    }
                    if (!moved &&
                        state.focusRunning &&
                        state.overlay == null && state.sheet == null && state.paywall == null
                    ) {
                        state.focusCtl = true
                    }
                }
            },
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 402.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Tab chrome
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(chromeAlpha)
                    .height(44.dp)
                    .padding(top = 10.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KlokkTab.entries.forEach { tab ->
                    Text(
                        tabLabel(tab),
                        color = if (state.tab == tab) KlokkTextPrimary else KlokkTabInactive,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.1).sp,
                        modifier = Modifier.tap { goTab(state, tab, now) },
                    )
                }
            }

            // Hero grid
            Box(
                modifier = Modifier
                    .padding(top = heroTopPadding)
                    .width(360.dp)
                    .height(heroHeight)
                    .alpha(heroAlpha)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val pair = if (down.position.x < size.width / 2f) 0 else 1
                            val target = scrubTarget(state)
                            val start = target?.let { pairValues(state, it) }
                            var moved = false
                            var lastValue: Int? = null
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                    ?: break
                                if (!change.pressed) break
                                if (change.positionChanged()) {
                                    val dy = down.position.y - change.position.y
                                    if (abs(dy) > 4f) {
                                        moved = true
                                        change.consume()
                                    }
                                    if (target != null && start != null) {
                                        val steps = (dy / 14f).roundToInt()
                                        val modulus = if (pair == 0) start[2] else start[3]
                                        val base = if (pair == 0) start[0] else start[1]
                                        val value = ((base + steps) % modulus + modulus) % modulus
                                        if (value != lastValue) {
                                            lastValue = value
                                            setPair(state, target, pair, value)
                                        }
                                    }
                                }
                            }
                            if (!moved &&
                                target == null &&
                                state.sheet == null &&
                                state.overlay == null &&
                                state.paywall == null &&
                                !state.focusRunning
                            ) {
                                state.gate(PaywallReason.SHOW) {
                                    state.openSaver(
                                        Clock.System.now().toEpochMilliseconds(),
                                    )
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                heroMatrix(state, now, local, timeMatrix)?.let {
                    KlokkGrid(it, clockSize = 24.dp, dim = isNight)
                }
                if (scrubHintsAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(scrubHintsAlpha),
                    ) {
                        ScrubTriangle(
                            up = true,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 80.dp, top = 2.dp),
                        )
                        ScrubTriangle(
                            up = true,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 80.dp, top = 2.dp),
                        )
                        ScrubTriangle(
                            up = false,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 80.dp, bottom = 2.dp),
                        )
                        ScrubTriangle(
                            up = false,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 80.dp, bottom = 2.dp),
                        )
                    }
                }
            }

            // Caption
            Box(
                modifier = Modifier.height(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    heroCaption(state, now, local, nextAlarm),
                    color = KlokkTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                )
            }

            // Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .alpha(bodyAlpha)
                    .padding(bottom = 56.dp)
                    .then(
                        if (state.focusRunning) {
                            // dead zone: blocks taps on the hidden body
                            Modifier.pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    down.consume()
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        var stillPressed = false
                                        event.changes.forEach {
                                            it.consume()
                                            if (it.pressed) stillPressed = true
                                        }
                                        if (!stillPressed) break
                                    }
                                }
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                ) {
                    when (state.tab) {
                        KlokkTab.CLOCK -> ClockTab(state, now)
                        KlokkTab.ALARM -> AlarmTab(state)
                        KlokkTab.TIMERS -> TimerTab(state)
                        KlokkTab.FOCUS -> FocusTab(state, now)
                        KlokkTab.SETTINGS -> SettingsTab(
                            state,
                            now,
                            timeMatrix,
                            isNight,
                            nextAlarmCaption(state, nextAlarm),
                        )
                    }
                }
            }
        }

        CitySheet(state, now)
        SaverSheet(state, saverPreviewMatrix(state, now))
        PaywallOverlay(state, paywallMatrix(state))
        RingOverlay(
            state,
            ringMatrix(state),
            ringTitle(state),
            ringLabel(state, local),
            if (state.ringKind == RingKind.TIMER) {
                stringResource(Res.string.ring_hint_timer)
            } else {
                stringResource(Res.string.ring_hint_alarm)
            },
        )
        SaverOverlay(state, saverMatrix(state, local))
        FocusEndControl(
            state,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp),
        )
    }
}

private enum class ScrubTarget { ALARM, TIMER }

private fun scrubTarget(state: KlokkState): ScrubTarget? = when {
    state.overlay != null || state.sheet != null || state.paywall != null -> null
    state.tab == KlokkTab.ALARM && state.editingId != null -> ScrubTarget.ALARM
    state.tab == KlokkTab.TIMERS &&
        !state.tmRunning && state.tmRemaining == state.tmTotal -> ScrubTarget.TIMER

    else -> null
}

private fun scrubEnabled(state: KlokkState): Boolean = scrubTarget(state) != null

/** [h or minutes, m or seconds, mod1, mod2] for the left/right drag pairs. */
private fun pairValues(state: KlokkState, target: ScrubTarget): IntArray = when (target) {
    ScrubTarget.ALARM -> {
        val alarm = state.alarms.firstOrNull { it.id == state.editingId }
            ?: return intArrayOf(0, 0, 24, 60)
        intArrayOf(alarm.h, alarm.m, 24, 60)
    }

    ScrubTarget.TIMER -> intArrayOf(state.tmTotal / 60, state.tmTotal % 60, 100, 60)
}

private fun setPair(state: KlokkState, target: ScrubTarget, pair: Int, value: Int) {
    when (target) {
        ScrubTarget.ALARM -> {
            val index = state.alarms.indexOfFirst { it.id == state.editingId }
            if (index >= 0) {
                val alarm = state.alarms[index]
                state.alarms[index] = if (pair == 0) {
                    alarm.copy(h = value)
                } else {
                    alarm.copy(m = value)
                }
            }
        }

        ScrubTarget.TIMER -> {
            val minutes = if (pair == 0) value else state.tmTotal / 60
            val seconds = if (pair == 1) value else state.tmTotal % 60
            val total = maxOf(1, minutes * 60 + seconds)
            state.tmTotal = total
            state.tmRemaining = total
        }
    }
}

private fun goTab(state: KlokkState, tab: KlokkTab, now: Instant) {
    if (state.focusRunning || state.overlay != null || state.paywall != null) return
    state.sheet = null
    state.swipeIndex = -1
    state.editingId = null
    if (tab == KlokkTab.SETTINGS) {
        state.saverPreview = state.saverShow
        state.previewTs = now.toEpochMilliseconds()
    }
    state.tab = tab
}

/** Per-second bookkeeping: timer countdown, alarm matching, snooze re-fire. */
private fun onSecond(state: KlokkState, nowMs: Long) {
    if (state.tmRunning && state.tmEndTs > 0 && nowMs >= state.tmEndTs) {
        state.tmRunning = false
        state.tmEndTs = 0L
        state.tmRemaining = 0
        state.startRing(RingKind.TIMER, null, nowMs)
        return
    }

    if (state.snoozeUntil > 0 && nowMs >= state.snoozeUntil) {
        state.snoozeUntil = 0L
        state.startRing(RingKind.ALARM, null, nowMs)
        return
    }

    val tz = TimeZone.currentSystemDefault()
    val local = Instant.fromEpochMilliseconds(nowMs).toLocalDateTime(tz)
    val day = (local.dayOfWeek.ordinal + 1) % 7
    val hit = state.alarms.firstOrNull { alarm ->
        alarm.on &&
            alarm.h == local.hour &&
            alarm.m == local.minute &&
            alarm.id != state.rungAlarmId &&
            // Alarm instants fall within the same minute as local; the
            // (minute, h) match survives a skipped :00 tick — we just also
            // skip alarms already rung for this occurrence.
            local.second < 30 &&
            (alarm.days.none { it } || alarm.days.getOrElse(day) { false })
    }
    if (hit != null) {
        if (hit.days.none { it }) {
            val index = state.alarms.indexOf(hit)
            state.alarms[index] = hit.copy(on = false)
        }
        if (state.overlay != KlokkOverlay.RING) {
            state.rungAlarmId = hit.id
            state.startRing(RingKind.ALARM, hit.id, nowMs)
        }
    }
}

/** The grid shown in the hero slot for the active tab; null while alarm rows hide it. */
private fun heroMatrix(
    state: KlokkState,
    now: Instant,
    local: LocalDateTime,
    timeMatrix: List<List<KlokkCell>>,
): List<List<KlokkCell>>? {
    val second = local.second
    return when (state.tab) {
        KlokkTab.ALARM -> {
            val alarm = state.alarms.firstOrNull { it.id == state.editingId } ?: return timeMatrix
            KlokkMatrices.digitsMatrix(
                KlokkFormat.hm(alarm.h, alarm.m),
                second * 6f,
                digitDuration = 250,
                backgroundDuration = 1000,
            )
        }

        KlokkTab.TIMERS -> {
            val remaining = timerRemaining(state, now)
            val progress = if (remaining < state.tmTotal && state.tmTotal > 0) {
                360f * (1f - remaining.toFloat() / state.tmTotal)
            } else {
                0f
            }
            val totalMinutes = minOf(99, remaining / 60)
            KlokkMatrices.digitsMatrix(
                KlokkFormat.hm(totalMinutes, remaining % 60),
                progress,
                digitDuration = if (state.tmRunning) 1000 else 250,
                backgroundDuration = 1000,
            )
        }

        KlokkTab.FOCUS -> {
            val elapsed = if (state.focusRunning) {
                now.toEpochMilliseconds() - state.focusStartTs
            } else {
                0L
            }
            val minutes = (elapsed / 60000L).toInt()
            val digits = if (minutes >= 100) {
                KlokkFormat.hm(minutes / 60, minutes % 60)
            } else {
                KlokkFormat.hm(minutes, ((elapsed / 1000L) % 60L).toInt())
            }
            KlokkMatrices.digitsMatrix(
                digits,
                if (state.focusRunning) second * 6f else 0f,
                digitDuration = 200,
                backgroundDuration = 1000,
            )
        }

        else -> timeMatrix
    }
}

@Composable
private fun heroCaption(
    state: KlokkState,
    now: Instant,
    local: LocalDateTime,
    nextAlarm: NextAlarm?,
): String = when (state.tab) {
    KlokkTab.ALARM -> {
        val alarm = state.alarms.firstOrNull { it.id == state.editingId }
        when {
            alarm != null -> stringResource(Res.string.caption_drag_hours)
            state.snoozeUntil > 0 -> {
                stringResource(Res.string.caption_snoozed, snoozeTime(state))
            }

            nextAlarm != null -> {
                stringResource(Res.string.caption_next_alarm, relTime(nextAlarm.ms))
            }

            else -> stringResource(Res.string.caption_no_alarms)
        }
    }

    KlokkTab.TIMERS -> when {
        state.tmRunning -> stringResource(Res.string.caption_ends_at, endTime(state, now))
        state.tmRemaining != state.tmTotal -> stringResource(Res.string.caption_paused)
        else -> stringResource(Res.string.caption_drag_minutes)
    }

    KlokkTab.FOCUS -> if (state.focusRunning) {
        stringResource(Res.string.caption_started, startTime(state))
    } else {
        stringResource(Res.string.caption_focus)
    }

    else -> dateLine(now)
}

private fun snoozeTime(state: KlokkState): String {
    val local = Instant.fromEpochMilliseconds(state.snoozeUntil)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return KlokkFormat.fmtHM(local.hour, local.minute)
}

private fun endTime(state: KlokkState, now: Instant): String {
    val end = Instant.fromEpochMilliseconds(
        now.toEpochMilliseconds() + timerRemaining(state, now) * 1000L,
    ).toLocalDateTime(TimeZone.currentSystemDefault())
    return KlokkFormat.fmtHM(end.hour, end.minute)
}

private fun startTime(state: KlokkState): String {
    val local = Instant.fromEpochMilliseconds(state.focusStartTs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return KlokkFormat.fmtHM(local.hour, local.minute)
}

@Composable
private fun nextAlarmCaption(state: KlokkState, nextAlarm: NextAlarm?): String =
    if (nextAlarm != null) {
        stringResource(Res.string.caption_next_alarm, relTime(nextAlarm.ms))
    } else {
        stringResource(Res.string.caption_no_alarms)
    }

private fun timerRemaining(state: KlokkState, now: Instant): Int =
    if (state.tmRunning && state.tmEndTs > 0) {
        ((state.tmEndTs - now.toEpochMilliseconds() + 999L) / 1000L).toInt().coerceAtLeast(0)
    } else {
        state.tmRemaining
    }

@Composable
private fun ringTitle(state: KlokkState): String = stringResource(
    if (state.ringKind == RingKind.TIMER) Res.string.ring_timer else Res.string.ring_alarm,
)

@Composable
private fun ringLabel(state: KlokkState, local: LocalDateTime): String = when {
    state.ringKind == RingKind.TIMER -> stringResource(Res.string.ring_time_up)
    else -> {
        val alarm = state.alarms.firstOrNull { it.id == state.ringId }
        alarm?.label?.takeIf { it.isNotBlank() }
            ?: KlokkFormat.fmtHM(local.hour, local.minute)
    }
}

private fun ringMatrix(state: KlokkState): List<List<KlokkCell>>? {
    if (state.overlay != KlokkOverlay.RING) return null
    if (!state.ringOn) {
        return KlokkMatrices.uniform(0f, 0f, 350, KlokkCellMode.SHORTEST)
    }
    val digits = when (state.ringKind) {
        RingKind.TIMER -> intArrayOf(0, 0, 0, 0)
        else -> {
            val alarm = state.alarms.firstOrNull { it.id == state.ringId }
            if (alarm != null) {
                KlokkFormat.hm(alarm.h, alarm.m)
            } else {
                val tz = TimeZone.currentSystemDefault()
                val local = Instant.fromEpochMilliseconds(state.nowMs).toLocalDateTime(tz)
                KlokkFormat.hm(local.hour, local.minute)
            }
        }
    }
    return KlokkMatrices.digitsMatrix(
        digits,
        backgroundDegree = 0f,
        digitDuration = 350,
        backgroundDuration = 350,
    )
}

private fun saverMatrix(
    state: KlokkState,
    local: LocalDateTime,
): List<List<KlokkCell>>? {
    if (state.overlay != KlokkOverlay.SAVER) return null
    if (state.saverStep < 0) {
        return KlokkMatrices.digitsMatrix(
            KlokkFormat.hm(local.hour, local.minute),
            local.second * 6f,
            digitDuration = 0,
            backgroundDuration = 0,
            mode = KlokkCellMode.SNAP,
        )
    }
    val sequence = KlokkMatrices.sequence(state.saverShow)
    return sequence[state.saverStep % sequence.size]
}

private fun saverPreviewMatrix(
    state: KlokkState,
    now: Instant,
): List<List<KlokkCell>>? {
    if (state.sheet != com.theapache64.klokk.state.KlokkSheet.SAVER) return null
    val sequence = KlokkMatrices.sequence(state.saverPreview)
    val step = floor(
        maxOf(0L, now.toEpochMilliseconds() - state.previewTs) / 5000.0,
    ).toInt()
    return sequence[step % sequence.size]
}

private fun paywallMatrix(state: KlokkState): List<List<KlokkCell>>? {
    if (state.paywall == null) return null
    val sequence = KlokkMatrices.sequence(SaverShow.SHUFFLE)
    val step = floor(state.nowMs / 5000.0).toInt()
    return sequence[step % sequence.size]
}
