package com.theapache64.klokk.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.theme.KlokkTextSecondary
import com.theapache64.klokk.util.KlokkFormat
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.toLocalDateTime

/** Focus: a Start button that hands the whole screen to the ticking grid. */
@Composable
fun FocusTab(state: KlokkState, now: Instant) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(30.dp))
        CircleActionButton(
            label = "Start",
            onClick = {
                state.focusRunning = true
                state.focusStartTs = Clock.System.now().toEpochMilliseconds()
                state.focusCtl = true
            },
            bold = true,
        )
        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date
        val total = state.focusSessions
            .filter {
                Instant.fromEpochMilliseconds(it.startMs).toLocalDateTime(tz).date == today
            }
            .sumOf { it.ms }
        val note = when {
            state.focusRunning -> "Counting up until you end it"
            total > 0 -> "Today · ${KlokkFormat.fmtDur(total)}"
            else -> ""
        }
        Text(
            note,
            color = KlokkTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp),
        )
        state.focusSessions.asReversed().take(6).forEach { session ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .borderTop(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val start = Instant.fromEpochMilliseconds(session.startMs)
                val local = start.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                Text(
                    KlokkFormat.fmtHM(local.hour, local.minute),
                    color = KlokkTextSecondary,
                    fontSize = 15.sp,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    KlokkFormat.fmtDur(session.ms),
                    color = KlokkTextPrimary,
                    fontSize = 15.sp,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                )
            }
        }
    }
}
