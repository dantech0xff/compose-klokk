package com.theapache64.klokk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.generated.resources.Res
import com.theapache64.klokk.generated.resources.delete
import com.theapache64.klokk.generated.resources.done
import com.theapache64.klokk.generated.resources.label
import com.theapache64.klokk.generated.resources.new_alarm
import com.theapache64.klokk.generated.resources.preview
import com.theapache64.klokk.state.Alarm
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.state.RingKind
import com.theapache64.klokk.theme.KlokkBorder
import com.theapache64.klokk.theme.KlokkBorderStrong
import com.theapache64.klokk.theme.KlokkOnPrimary
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.theme.KlokkTextSecondary
import com.theapache64.klokk.util.KlokkFormat
import com.theapache64.klokk.util.dayChipNames
import com.theapache64.klokk.util.daysText
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

private val DAY_ORDER = listOf(1, 2, 3, 4, 5, 6, 0)

@Composable
fun Modifier.borderBottom(color: Color = KlokkBorder): Modifier = this.drawBehind {
    drawLine(
        color,
        Offset(0f, size.height),
        Offset(size.width, size.height),
        strokeWidth = 1f,
    )
}

/** Alarm list: tap a row to edit (day chips, label, delete/preview/done). */
@Composable
fun AlarmTab(state: KlokkState) {
    Column {
        Spacer(Modifier.height(6.dp))
        val editing = state.editingId != null

        state.alarms.forEach { alarm ->
            val selected = state.editingId == alarm.id
            Column(
                modifier = Modifier.alpha(if (editing && !selected) 0.35f else 1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .borderTop()
                        .tap { state.editingId = alarm.id },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            KlokkFormat.fmtHM(alarm.h, alarm.m),
                            color = if (alarm.on || selected) KlokkTextPrimary else KlokkTextSecondary,
                            fontSize = 22.sp,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                        )
                        Text(
                            if (alarm.label.isNotEmpty()) {
                                "${alarm.label} · ${daysText(alarm.days)}"
                            } else {
                                daysText(alarm.days)
                            },
                            color = KlokkTextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                    KlokkToggle(
                        checked = alarm.on,
                        onToggle = {
                            state.alarms.indexOfFirst { it.id == alarm.id }
                                .takeIf { it >= 0 }
                                ?.let { i ->
                                    state.alarms[i] = state.alarms[i].copy(on = !state.alarms[i].on)
                                }
                        },
                    )
                }

                if (selected) {
                    AlarmEditor(state, alarm)
                }
            }
        }

        if (!editing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .borderTop()
                    .tap {
                        val id = state.nextAlarmId++
                        state.alarms.add(
                            Alarm(
                                id = id,
                                h = 7,
                                m = 0,
                                label = "",
                                days = listOf(false, true, true, true, true, true, false),
                                on = true,
                            )
                        )
                        state.editingId = id
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlusGlyph(14.dp)
                Spacer(Modifier.width(14.dp))
                Text(
                    stringResource(Res.string.new_alarm),
                    color = KlokkTextPrimary,
                    fontSize = 17.sp,
                )
            }
        }
    }
}

@Composable
private fun AlarmEditor(state: KlokkState, alarm: Alarm) {
    Column(
        modifier = Modifier
            .padding(top = 4.dp, bottom = 26.dp)
    ) {
        // Day chips M T W T F S S
        val chipNames = dayChipNames()
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_ORDER.forEachIndexed { i, day ->
                val on = alarm.days[day]
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (on) KlokkTextPrimary else Color.Transparent)
                        .border(
                            1.dp,
                            if (on) KlokkTextPrimary else KlokkBorderStrong,
                            CircleShape,
                        )
                        .tap {
                            state.alarms.indexOfFirst { it.id == alarm.id }
                                .takeIf { it >= 0 }
                                ?.let { i ->
                                    val a = state.alarms[i]
                                    state.alarms[i] = a.copy(
                                        days = a.days.mapIndexed { k, v -> if (k == day) !v else v }
                                    )
                                }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        chipNames[day],
                        color = if (on) KlokkOnPrimary else KlokkTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (i < DAY_ORDER.lastIndex) Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(22.dp))

        // Label field
        BasicTextField(
            value = alarm.label,
            onValueChange = { v ->
                state.alarms.indexOfFirst { it.id == alarm.id }
                    .takeIf { it >= 0 }
                    ?.let { i ->
                        state.alarms[i] = state.alarms[i].copy(label = v)
                    }
            },
            textStyle = TextStyle(
                color = KlokkTextPrimary,
                fontSize = 17.sp,
            ),
            cursorBrush = SolidColor(KlokkTextPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .borderBottom(),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (alarm.label.isEmpty()) {
                        Text(
                            stringResource(Res.string.label),
                            color = KlokkTextSecondary,
                            fontSize = 17.sp,
                        )
                    }
                    inner()
                }
            },
        )

        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.delete),
                color = KlokkTextSecondary,
                fontSize = 15.sp,
                modifier = Modifier.tap {
                    state.alarms.removeAll { it.id == alarm.id }
                    state.editingId = null
                },
            )
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.preview),
                color = KlokkTextSecondary,
                fontSize = 15.sp,
                modifier = Modifier.tap {
                    state.startRing(
                        RingKind.ALARM,
                        alarm.id,
                        Clock.System.now().toEpochMilliseconds(),
                    )
                },
            )
            Spacer(Modifier.width(18.dp))
            PillButton(
                stringResource(Res.string.done),
                onClick = { state.editingId = null },
                modifier = Modifier.width(86.dp),
            )
        }
    }
}
