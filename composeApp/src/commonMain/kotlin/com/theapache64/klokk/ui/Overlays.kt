package com.theapache64.klokk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.composable.KlokkCell
import com.theapache64.klokk.composable.KlokkGrid
import com.theapache64.klokk.state.KlokkOverlay
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.state.PaywallReason
import com.theapache64.klokk.state.PlusPlan
import com.theapache64.klokk.state.RingKind
import com.theapache64.klokk.theme.KlokkBackground
import com.theapache64.klokk.theme.KlokkBorder
import com.theapache64.klokk.theme.KlokkSurface
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.theme.KlokkTextSecondary
import kotlin.time.Clock

private val pageEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

private val PAY_LEADS = mapOf(
    PaywallReason.CITIES to "Free keeps one city. Plus keeps every city you care about.",
    PaywallReason.SHOW to "Tap the clock and the grid performs — Ripple, Trance, Wave and Shuffle.",
    PaywallReason.WIDGETS to "Put the grid on your Home Screen and Lock Screen.",
    PaywallReason.SETTINGS to "The whole grid, everywhere you look.",
)

private val PAY_FEATURES = listOf(
    Triple(PaywallReason.CITIES, "World clock", "Unlimited cities"),
    Triple(PaywallReason.SHOW, "Screensaver", "Four choreographies"),
    Triple(PaywallReason.WIDGETS, "Widgets", "Home & Lock Screen"),
)

/** Full-page Klokk Plus paywall sliding up over the app. */
@Composable
fun PaywallOverlay(
    state: KlokkState,
    payMatrix: List<List<KlokkCell>>?,
) {
    AnimatedVisibility(
        visible = state.paywall != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(360, easing = pageEasing),
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = pageEasing),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KlokkBackground),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp)
                    .padding(top = 62.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Not now",
                    color = KlokkTextSecondary,
                    fontSize = 17.sp,
                    modifier = Modifier.tap {
                        state.pending = null
                        state.paywall = null
                    },
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    payMatrix?.let { KlokkGrid(it, 16.dp, dim = false) }
                }
                Text(
                    "Klokk Plus",
                    color = KlokkTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.4).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                )
                Text(
                    PAY_LEADS[state.paywall] ?: "",
                    color = KlokkTextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                )
                Column(modifier = Modifier.padding(top = 22.dp)) {
                    PAY_FEATURES.forEach { (reason, name, detail) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .borderTop(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                name,
                                color = if (state.paywall == reason || state.paywall == PaywallReason.SETTINGS) {
                                    KlokkTextPrimary
                                } else {
                                    KlokkTextSecondary
                                },
                                fontSize = 17.sp,
                                modifier = Modifier.weight(1f).padding(end = 16.dp),
                            )
                            Text(
                                detail,
                                color = KlokkTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PlusPlan.entries.forEach { plan ->
                        val selected = state.plan == plan
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) KlokkSurface else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (selected) KlokkTextPrimary else Color(0x1fffffff),
                                    RoundedCornerShape(16.dp),
                                )
                                .tap { state.plan = plan }
                                .padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plan.label, color = KlokkTextPrimary, fontSize = 17.sp)
                                Text(plan.note, color = KlokkTextSecondary, fontSize = 13.sp)
                            }
                            Text(
                                plan.price,
                                color = KlokkTextPrimary,
                                fontSize = 17.sp,
                                style = TextStyle(fontFeatureSettings = "tnum"),
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .borderTop()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 34.dp),
            ) {
                PillButton(
                    text = when {
                        state.buying -> "Unlocking…"
                        state.plan == PlusPlan.YEAR -> "Try free for 7 days"
                        else -> "Unlock Plus"
                    },
                    onClick = { state.unlock() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (state.buying) 0.6f else 1f),
                    height = 54.dp,
                    fontSize = 17f,
                    enabled = !state.buying,
                )
                Text(
                    state.plan.fineprint,
                    color = KlokkTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
                ) {
                    Text(
                        "Restore",
                        color = KlokkTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.tap { state.unlock() },
                    )
                    Text("Terms", color = KlokkTextSecondary, fontSize = 13.sp)
                    Text("Privacy", color = KlokkTextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Alarm/timer ringing. Swipe up to stop (or any release for a timer);
 * a small-release tap snoozes an alarm for 9 minutes.
 */
@Composable
fun RingOverlay(
    state: KlokkState,
    matrix: List<List<KlokkCell>>?,
    title: String,
    label: String,
    hint: String,
) {
    AnimatedVisibility(
        visible = state.overlay == KlokkOverlay.RING,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(200)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KlokkBackground)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        var lastY = down.position.y
                        while (true) {
                            val ev = awaitPointerEvent()
                            val ch = ev.changes.firstOrNull { it.id == down.id } ?: break
                            lastY = ch.position.y
                            if (!ch.pressed) break
                        }
                        val dy = down.position.y - lastY
                        if (Clock.System.now().toEpochMilliseconds() - state.overlayTs < 400) {
                            return@awaitEachGesture
                        }
                        if (dy > 60 || state.ringKind == RingKind.TIMER) {
                            state.stopRing()
                        } else {
                            state.snooze(Clock.System.now().toEpochMilliseconds())
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                color = KlokkTextSecondary,
                fontSize = 13.sp,
                letterSpacing = 0.4.sp,
                modifier = Modifier.padding(top = 150.dp),
            )
            Box(modifier = Modifier.padding(top = 56.dp)) {
                matrix?.let { KlokkGrid(it, 24.dp, dim = false) }
            }
            Text(
                label,
                color = KlokkTextPrimary,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 22.dp),
            )
            Spacer(Modifier.weight(1f))
            ScrubTriangle(up = true)
            Text(
                hint,
                color = KlokkTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 76.dp),
            )
        }
    }
}

/** Full-screen kinetic show, the 15x8 grid rotated 90° like the design. */
@Composable
fun SaverOverlay(
    state: KlokkState,
    matrix: List<List<KlokkCell>>?,
) {
    AnimatedVisibility(
        visible = state.overlay == KlokkOverlay.SAVER,
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(200)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KlokkBackground)
                .tap {
                    if (Clock.System.now().toEpochMilliseconds() - state.overlayTs > 400) {
                        state.overlay = null
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(690.dp)
                    .height(368.dp)
                    .graphicsLayer { rotationZ = 90f },
                contentAlignment = Alignment.Center,
            ) {
                matrix?.let { KlokkGrid(it, 46.dp, dim = false) }
            }
        }
    }
}

/** The fading "End" control while a Focus session runs. */
@Composable
fun FocusEndControl(state: KlokkState, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = state.focusRunning && state.focusCtl,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300)),
        modifier = modifier,
    ) {
        CircleActionButton(
            label = "End",
            onClick = {
                val now = Clock.System.now().toEpochMilliseconds()
                state.focusSessions.add(
                    com.theapache64.klokk.state.FocusSession(
                        startMs = state.focusStartTs,
                        ms = now - state.focusStartTs,
                    )
                )
                state.focusRunning = false
                state.focusStartTs = 0
                state.focusCtl = false
            },
            filled = false,
        )
    }
}
