package com.theapache64.klokk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.composable.KlokkCell
import com.theapache64.klokk.composable.KlokkGrid
import com.theapache64.klokk.movement.SaverShow
import com.theapache64.klokk.state.CITIES
import com.theapache64.klokk.state.City
import com.theapache64.klokk.state.KlokkSheet
import com.theapache64.klokk.state.KlokkState
import com.theapache64.klokk.state.PaywallReason
import com.theapache64.klokk.theme.KlokkBackground
import com.theapache64.klokk.theme.KlokkField
import com.theapache64.klokk.theme.KlokkSurface
import com.theapache64.klokk.theme.KlokkTextPrimary
import com.theapache64.klokk.theme.KlokkTextSecondary
import com.theapache64.klokk.util.KlokkFormat
import kotlin.time.Instant

private val sheetEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/**
 * Bottom-sheet chrome shared by "Add city" and the screensaver picker:
 * transparent tap area above, rounded dark sheet sliding up from the bottom.
 */
@Composable
private fun SheetFrame(
    visible: Boolean,
    height: androidx.compose.ui.unit.Dp,
    title: String,
    onCancel: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(320, easing = sheetEasing),
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = sheetEasing),
        ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tap-to-dismiss region above the sheet
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .tap(onCancel)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(KlokkSurface)
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x2effffff))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        color = KlokkTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "Cancel",
                        color = KlokkTextPrimary,
                        fontSize = 17.sp,
                        modifier = Modifier.tap(onCancel),
                    )
                }
                content()
            }
        }
    }
}

/** "Add city" — searchable list of 45 world cities. */
@Composable
fun CitySheet(state: KlokkState, now: Instant) {
    SheetFrame(
        visible = state.sheet == KlokkSheet.CITIES,
        height = 560.dp,
        title = "Add city",
        onCancel = { state.sheet = null },
    ) {
        BasicTextField(
            value = state.citySearch,
            onValueChange = { state.citySearch = it },
            textStyle = TextStyle(color = KlokkTextPrimary, fontSize = 17.sp),
            cursorBrush = SolidColor(KlokkTextPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KlokkField)
                .padding(horizontal = 14.dp),
            singleLine = true,
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (state.citySearch.isEmpty()) {
                        Text("Search city or country", color = Color(0xff5f5f5f), fontSize = 17.sp)
                    }
                    inner()
                }
            },
        )

        val q = state.citySearch.trim().lowercase()
        val options = CITIES.filter { (name, region) ->
            q.isEmpty() || name.lowercase().contains(q) || region.lowercase().contains(q)
        }

        if (options.isEmpty()) {
            Text(
                "No city found",
                color = KlokkTextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            )
        }

        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(options, key = { it.first }) { (name, region, tz) ->
                val added = state.cities.any { it.name == name }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .alpha(if (added) 0.4f else 1f)
                        .tap {
                            if (!added) {
                                state.cities.add(City(name, tz))
                                state.sheet = null
                            }
                        }
                        .borderBottom(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = KlokkTextPrimary, fontSize = 17.sp)
                        Text(region, color = KlokkTextSecondary, fontSize = 13.sp)
                    }
                    Text(
                        if (added) "Added" else KlokkFormat.cityInfo(tz, now).time,
                        color = KlokkTextSecondary,
                        fontSize = 15.sp,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                    )
                }
            }
        }
    }
}

private val SAVER_DESCRIPTIONS = mapOf(
    SaverShow.SHUFFLE to "Every choreography, one after another.",
    SaverShow.RIPPLE to "Hands fan out from the centre in slow rings.",
    SaverShow.TRANCE to "Tiles morph through square, flower, fly and star.",
    SaverShow.WAVE to "Columns tilt back and forth like a tide.",
)

/** Screensaver picker — live preview cycling the selected choreography. */
@Composable
fun SaverSheet(
    state: KlokkState,
    previewMatrix: List<List<KlokkCell>>?,
) {
    val plus = state.isPlus()
    SheetFrame(
        visible = state.sheet == KlokkSheet.SAVER,
        height = 640.dp,
        title = "Screensaver",
        onCancel = { state.sheet = null },
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(228.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(KlokkBackground),
                contentAlignment = Alignment.Center,
            ) {
                previewMatrix?.let { KlokkGrid(it, 22.dp, dim = false) }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(52.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                    24.dp,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SaverShow.entries.forEach { show ->
                    Text(
                        show.label,
                        color = if (state.saverPreview == show) KlokkTextPrimary else KlokkTextSecondary,
                        fontSize = 17.sp,
                        modifier = Modifier.tap {
                            state.saverPreview = show
                            state.previewTs = kotlin.time.Clock.System.now().toEpochMilliseconds()
                        },
                    )
                }
            }
            Text(
                SAVER_DESCRIPTIONS.getValue(state.saverPreview),
                color = KlokkTextSecondary,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Spacer(Modifier.weight(1f))
            val inUse = plus && state.saverPreview == state.saverShow
            PillButton(
                text = when {
                    !plus -> "Unlock with Plus"
                    inUse -> "In use"
                    else -> "Use ${state.saverPreview.label}"
                },
                onClick = {
                    if (!inUse) {
                        state.gate(PaywallReason.SHOW) {
                            state.saverShow = state.saverPreview
                            state.sheet = null
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .alpha(if (inUse) 0.4f else 1f),
                height = 54.dp,
                fontSize = 17f,
            )
            Text(
                if (plus) "Tap the clock on the Clock tab to start" else "Preview free · Klokk Plus to use",
                color = KlokkTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 34.dp),
            )
        }
    }
}
