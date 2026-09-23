package com.theapache64.klokk.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.theapache64.klokk.state.City
import com.theapache64.klokk.state.KlokkSheet
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.state.PaywallReason
import com.theapache64.klokk.theme.KlokkBackground
import com.theapache64.klokk.theme.KlokkBorder
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.generated.resources.Res
import com.theapache64.klokk.generated.resources.add_city
import com.theapache64.klokk.generated.resources.add_city_hint
import com.theapache64.klokk.generated.resources.add_city_hint_free
import com.theapache64.klokk.generated.resources.delete
import com.theapache64.klokk.generated.resources.no_cities
import com.theapache64.klokk.generated.resources.world_clock
import com.theapache64.klokk.theme.KlokkTextSecondary
import com.theapache64.klokk.util.cityInfo
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Instant

private val settleEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/** World clock list: header + add, empty state, swipe-to-delete rows. */
@Composable
fun ClockTab(state: KlokkState, now: Instant) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.world_clock),
                color = KlokkTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.3).sp,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .tap {
                        val open = {
                            state.sheet = KlokkSheet.CITIES
                            state.citySearch = ""
                            state.swipeIndex = -1
                        }
                        if (state.cities.size >= 1) {
                            state.gate(PaywallReason.CITIES, open)
                        } else open()
                    },
                contentAlignment = Alignment.Center,
            ) {
                PlusGlyph(16.dp)
            }
        }

        if (state.cities.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .borderTop()
                    .padding(top = 28.dp, bottom = 8.dp),
            ) {
                Text(
                    stringResource(Res.string.no_cities),
                    color = KlokkTextPrimary,
                    fontSize = 17.sp,
                )
                Text(
                    stringResource(
                        if (state.isPlus()) {
                            Res.string.add_city_hint
                        } else {
                            Res.string.add_city_hint_free
                        },
                    ),
                    color = KlokkTextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                PillButton(
                    stringResource(Res.string.add_city),
                    onClick = {
                        val open = {
                            state.sheet = KlokkSheet.CITIES
                            state.citySearch = ""
                            state.swipeIndex = -1
                        }
                        if (state.cities.size >= 1) {
                            state.gate(PaywallReason.CITIES, open)
                        } else open()
                    },
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        state.cities.forEachIndexed { i, city ->
            key(city.tz) {
                CityRow(
                    city = city,
                    index = i,
                    state = state,
                    now = now,
                )
            }
        }
    }
}

/** Thin top divider used by the list rows (design: rgba(255,255,255,.08)). */
fun Modifier.borderTop(color: Color = KlokkBorder): Modifier = this.drawBehind {
    drawLine(
        color,
        Offset(0f, 0f),
        Offset(size.width, 0f),
        strokeWidth = 1f,
    )
}

@Composable
private fun CityRow(
    city: City,
    index: Int,
    state: KlokkState,
    now: Instant,
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val info = cityInfo(city.tz, now)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .borderTop(),
    ) {
        // Delete target revealed by the swipe
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(88.dp)
                .height(62.dp)
                .background(KlokkTextPrimary)
                .tap {
                    state.cities.removeAt(index)
                    state.swipeIndex = -1
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.delete),
                color = androidx.compose.ui.graphics.Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.weight(1f))
        }

        val dragState = rememberDraggableState { dx ->
            scope.launch {
                offsetX.snapTo((offsetX.value + dx).coerceIn(-110f, 0f))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(KlokkBackground)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { state.swipeIndex = index },
                    onDragStopped = {
                        scope.launch {
                            offsetX.animateTo(
                                if (offsetX.value < -44) -88f else 0f,
                                animationSpec = tween(300, easing = settleEasing),
                            )
                        }
                    },
                )
                .pointerInput(index) {
                    detectTapGestures {
                        if (offsetX.value != 0f) {
                            scope.launch { offsetX.animateTo(0f, tween(300, easing = settleEasing)) }
                        }
                    }
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    city.name,
                    color = KlokkTextPrimary,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    info.sub,
                    color = KlokkTextSecondary,
                    fontSize = 13.sp,
                )
            }
            Text(
                info.time,
                color = KlokkTextPrimary,
                fontSize = 22.sp,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
        }
    }
}
