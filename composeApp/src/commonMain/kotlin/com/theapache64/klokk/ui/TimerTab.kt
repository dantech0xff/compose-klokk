package com.theapache64.klokk.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.generated.resources.Res
import com.theapache64.klokk.generated.resources.cancel
import com.theapache64.klokk.generated.resources.pause
import com.theapache64.klokk.generated.resources.resume
import com.theapache64.klokk.generated.resources.start
import com.theapache64.klokk.generated.resources.timer_preset
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.theme.KlokkTextSecondary
import org.jetbrains.compose.resources.stringResource
import kotlin.math.ceil
import kotlin.time.Clock

private val PRESETS = listOf(60, 300, 600, 1500)

/** Timer: preset row plus the Cancel / Start-Pause-Resume button pair. */
@Composable
fun TimerTab(state: KlokkState) {
    val tmActive = state.tmRunning || state.tmRemaining < state.tmTotal

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PRESETS.forEach { p ->
                val active = !state.tmRunning && state.tmTotal == p && state.tmRemaining == p
                Text(
                    stringResource(Res.string.timer_preset, p / 60),
                    color = if (active) KlokkTextPrimary else KlokkTextSecondary,
                    fontSize = 15.sp,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.tap {
                        if (!state.tmRunning) {
                            state.tmTotal = p
                            state.tmRemaining = p
                        }
                    },
                )
            }
        }

        val cancelWidth by animateDpAsState(
            if (tmActive) 76.dp else 0.dp,
            animationSpec = tween(300),
            label = "cancelW",
        )
        val cancelAlpha by animateFloatAsState(
            if (tmActive) 1f else 0f,
            animationSpec = tween(250),
            label = "cancelO",
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (cancelWidth > 0.dp) {
                Box(
                    modifier = Modifier
                        .width(cancelWidth)
                        .height(76.dp)
                        .alpha(cancelAlpha)
                        .clip(RectangleShape),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    CircleActionButton(
                        label = stringResource(Res.string.cancel),
                        onClick = {
                            state.tmRunning = false
                            state.tmRemaining = state.tmTotal
                        },
                        filled = false,
                    )
                }
            }
            CircleActionButton(
                label = when {
                    state.tmRunning -> stringResource(Res.string.pause)
                    state.tmRemaining < state.tmTotal -> stringResource(Res.string.resume)
                    else -> stringResource(Res.string.start)
                },
                onClick = {
                    if (state.tmRunning) {
                        state.tmRunning = false
                        state.tmRemaining = maxOf(
                            0,
                            ceil((state.tmEndTs - Clock.System.now().toEpochMilliseconds()) / 1000.0).toInt(),
                        )
                    } else {
                        state.tmEndTs =
                            Clock.System.now().toEpochMilliseconds() + state.tmRemaining * 1000L
                        state.tmRunning = true
                    }
                },
            )
        }
    }
}
