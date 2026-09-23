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
import com.theapache64.klokk.theme.KlokkBackground
import com.theapache64.klokk.theme.KlokkSurface
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.theme.KlokkTextSecondary
import com.theapache64.klokk.theme.CodGray
import com.theapache64.klokk.util.KlokkFormat
import kotlin.time.Instant
import kotlinx.datetime.toLocalDateTime

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
                    Text("Klokk Plus", color = KlokkTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Screensaver, widgets and unlimited cities",
                        color = KlokkTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                PillButton("Unlock", onClick = {}, height = 34.dp, fontSize = 15f, enabled = false)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(top = 6.dp)
                    .borderBottom(KlokkBorderColor()),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Klokk Plus", color = KlokkTextPrimary, fontSize = 17.sp, modifier = Modifier.weight(1f))
                Text("Active", color = KlokkTextSecondary, fontSize = 17.sp)
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
            Text("Screensaver", color = KlokkTextPrimary, fontSize = 17.sp, modifier = Modifier.weight(1f))
            Text(
                if (plus) state.saverShow.label else "Plus",
                color = KlokkTextSecondary,
                fontSize = 17.sp,
            )
        }
        Text(
            if (plus) "Tap the clock to start." else "Included with Klokk Plus.",
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
            Text("Widgets", color = KlokkTextPrimary, fontSize = 17.sp, modifier = Modifier.weight(1f))
            Text(
                if (plus) "Home & Lock Screen" else "Plus",
                color = KlokkTextSecondary,
                fontSize = 17.sp,
            )
        }
        Text(
            if (plus) "Hold your Home or Lock Screen to add." else "Included with Klokk Plus.",
            color = KlokkTextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )

        // Widget previews
        Column(modifier = Modifier.padding(top = 18.dp)) {
            WidgetCard(title = "Now", tag = "Small") {
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
                        faceColor = CodGray,
                        handColor = KlokkTextPrimary,
                        handWidth = 4.dp,
                        hourLen = 38.dp,
                        minLen = 56.dp,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            WidgetCard(title = "Time", tag = "Medium", horizontalPadding = 12.dp) {
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

            Spacer(Modifier.height(24.dp))

            WidgetCard(title = "Next alarm", tag = "Lock Screen", horizontalPadding = 12.dp) {
                Column(
                    modifier = Modifier
                        .width(338.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(KlokkBackground)
                        .padding(vertical = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        KlokkFormat.lockDate(now),
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
                            "NEXT ALARM",
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

private fun KlokkBorderColor() = Color(0x14ffffff)

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
