package com.theapache64.klokk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.composable.KlokkCell
import com.theapache64.klokk.composable.KlokkGrid
import com.theapache64.klokk.state.KlokkSheet
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.state.PaywallReason
import com.theapache64.klokk.state.ThemeMode
import androidx.compose.runtime.CompositionLocalProvider
import com.theapache64.klokk.theme.KlokkBackground
import com.theapache64.klokk.theme.KlokkClockFace
import com.theapache64.klokk.theme.KlokkField
import com.theapache64.klokk.theme.KlokkOnPrimary
import com.theapache64.klokk.theme.KlokkSurface
import com.theapache64.klokk.theme.LocalKlokkColors
import com.theapache64.klokk.theme.darkKlokkColors
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.theme.KlokkTextSecondary
import com.theapache64.klokk.generated.resources.Res
import com.theapache64.klokk.generated.resources.active
import com.theapache64.klokk.generated.resources.appearance
import com.theapache64.klokk.generated.resources.appearance_dark
import com.theapache64.klokk.generated.resources.appearance_light
import com.theapache64.klokk.generated.resources.appearance_system
import com.theapache64.klokk.generated.resources.included_plus
import com.theapache64.klokk.generated.resources.plus_badge
import com.theapache64.klokk.generated.resources.plus_card_sub
import com.theapache64.klokk.generated.resources.plus_title
import com.theapache64.klokk.generated.resources.saver_hint_plus
import com.theapache64.klokk.generated.resources.screensaver
import com.theapache64.klokk.generated.resources.unlock
import com.theapache64.klokk.generated.resources.widget_lock_screen
import com.theapache64.klokk.generated.resources.widget_medium
import com.theapache64.klokk.generated.resources.widget_next_alarm
import com.theapache64.klokk.generated.resources.widget_next_alarm_pill
import com.theapache64.klokk.generated.resources.widget_now
import com.theapache64.klokk.generated.resources.widget_small
import com.theapache64.klokk.generated.resources.widget_time
import com.theapache64.klokk.generated.resources.widgets
import com.theapache64.klokk.generated.resources.widgets_hint_plus
import com.theapache64.klokk.generated.resources.widgets_value_plus
import com.theapache64.klokk.util.KlokkFormat
import com.theapache64.klokk.util.lockDate
import com.theapache64.klokk.util.saverShowName
import kotlin.time.Instant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/** Settings: Klokk Plus card, screensaver picker entry, widget previews. */
@Composable
fun SettingsTab(
    state: KlokkState,
    now: Instant,
    timeMatrix: List<List<KlokkCell>>,
    isNight: Boolean,
    nextAlarmLine: String,
) {
    val plus = state.isPlus()
    val local = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val hourDeg = (local.hour % 12) * 30f + local.minute * 0.5f
    val minDeg = local.minute * 6f + local.second * 0.1f

    Column {
        // Appearance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.appearance),
                color = KlokkTextPrimary,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(KlokkField)
                    .padding(2.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    val selected = state.themeMode == mode
                    Text(
                        when (mode) {
                            ThemeMode.SYSTEM -> stringResource(Res.string.appearance_system)
                            ThemeMode.LIGHT -> stringResource(Res.string.appearance_light)
                            ThemeMode.DARK -> stringResource(Res.string.appearance_dark)
                        },
                        color = if (selected) KlokkOnPrimary else KlokkTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) KlokkTextPrimary else Color.Transparent)
                            .tap { state.themeMode = mode }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }

        if (!plus) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(KlokkSurface)
                    .tap { state.gate(PaywallReason.SETTINGS) {} }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        stringResource(Res.string.plus_title),
                        color = KlokkTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        stringResource(Res.string.plus_card_sub),
                        color = KlokkTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                PillButton(
                    stringResource(Res.string.unlock),
                    onClick = {},
                    height = 34.dp,
                    fontSize = 15f,
                    enabled = false,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 6.dp)
                    .borderBottom(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.plus_title),
                    color = KlokkTextPrimary,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(Res.string.active),
                    color = KlokkTextSecondary,
                    fontSize = 17.sp,
                )
            }
        }

        // Screensaver row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(52.dp)
                .tap {
                    state.sheet = KlokkSheet.SAVER
                    state.saverPreview = state.saverShow
                    state.previewTs = now.toEpochMilliseconds()
                    state.editingId = null
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.screensaver),
                color = KlokkTextPrimary,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                if (plus) saverShowName(state.saverShow) else stringResource(Res.string.plus_badge),
                color = KlokkTextSecondary,
                fontSize = 17.sp,
            )
        }
        Text(
            stringResource(
                if (plus) Res.string.saver_hint_plus else Res.string.included_plus,
            ),
            color = KlokkTextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )

        // Widgets row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .height(52.dp)
                .tap { state.gate(PaywallReason.WIDGETS) {} },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.widgets),
                color = KlokkTextPrimary,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                if (plus) {
                    stringResource(Res.string.widgets_value_plus)
                } else {
                    stringResource(Res.string.plus_badge)
                },
                color = KlokkTextSecondary,
                fontSize = 17.sp,
            )
        }
        Text(
            stringResource(
                if (plus) Res.string.widgets_hint_plus else Res.string.included_plus,
            ),
            color = KlokkTextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )

        // Widget previews
        Column(modifier = Modifier.padding(top = 18.dp)) {
            WidgetCard(
                title = stringResource(Res.string.widget_now),
                tag = stringResource(Res.string.widget_small),
            ) {
                CompositionLocalProvider(LocalKlokkColors provides darkKlokkColors()) {
                Box(
                    modifier = Modifier
                        .size(158.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(KlokkBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    AnalogDial(
                        hourDeg = hourDeg,
                        minDeg = minDeg,
                        size = 126.dp,
                        faceColor = KlokkClockFace,
                        handColor = KlokkTextPrimary,
                        handWidth = 4.dp,
                        hourLen = 38.dp,
                        minLen = 56.dp,
                    )
                }
                }
            }

            Spacer(Modifier.height(24.dp))

            WidgetCard(
                title = stringResource(Res.string.widget_time),
                tag = stringResource(Res.string.widget_medium),
                horizontalPadding = 12.dp,
            ) {
                CompositionLocalProvider(LocalKlokkColors provides darkKlokkColors()) {
                Box(
                    modifier = Modifier
                        .width(338.dp)
                        .height(158.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(KlokkBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    KlokkGrid(timeMatrix, 17.dp, dim = isNight)
                }
                }
            }

            Spacer(Modifier.height(24.dp))

            WidgetCard(
                title = stringResource(Res.string.widget_next_alarm),
                tag = stringResource(Res.string.widget_lock_screen),
                horizontalPadding = 12.dp,
            ) {
                CompositionLocalProvider(LocalKlokkColors provides darkKlokkColors()) {
                Column(
                    modifier = Modifier
                        .width(338.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(KlokkBackground)
                        .padding(vertical = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        lockDate(now),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp,
                    )
                    Spacer(Modifier.height(22.dp))
                    Box(contentAlignment = Alignment.Center) {
                        AnalogDial(
                            hourDeg = hourDeg,
                            minDeg = minDeg,
                            size = 168.dp,
                            faceColor = Color.White.copy(alpha = 0.08f),
                            handColor = Color.White.copy(alpha = 0.92f),
                            handWidth = 5.dp,
                            hourLen = 46.dp,
                            minLen = 66.dp,
                            centerDot = 8.dp,
                        )
                    }
                    Spacer(Modifier.height(22.dp))
                    Row(
                        modifier = Modifier
                            .height(64.dp)
                            .width(280.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(start = 24.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(Res.string.widget_next_alarm_pill),
                            color = KlokkTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                        )
                        Spacer(Modifier.width(14.dp))
                        val nx = KlokkFormat.nextAlarm(state.alarms, now)
                        val aH = nx?.let { (it.alarm.h % 12) * 30f + it.alarm.m * 0.5f } ?: 0f
                        val aM = nx?.let { it.alarm.m * 6f } ?: 0f
                        AnalogDial(
                            hourDeg = aH,
                            minDeg = aM,
                            size = 44.dp,
                            faceColor = Color.White.copy(alpha = 0.14f),
                            handColor = KlokkTextPrimary,
                            handWidth = 3.dp,
                            hourLen = 12.dp,
                            minLen = 18.dp,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            nx?.let { KlokkFormat.fmtHM(it.alarm.h, it.alarm.m) } ?: "—",
                            color = KlokkTextPrimary,
                            fontSize = 17.sp,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                        )
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun WidgetCard(
    title: String,
    tag: String,
    horizontalPadding: androidx.compose.ui.unit.Dp = 26.dp,
    content: @Composable () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(title, color = KlokkTextPrimary, fontSize = 17.sp, modifier = Modifier.weight(1f))
            Text(tag, color = KlokkTextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(KlokkSurface)
                .padding(vertical = 26.dp, horizontal = horizontalPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
