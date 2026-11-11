package dev.dantech.klokk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.dantech.klokk.state.ClockStyle
import dev.dantech.klokk.state.ThemeMode
import dev.dantech.klokk.theme.KlokkTheme
import java.io.File

fun main() = application {
    val style = System.getenv("KLOKK_CLOCK_STYLE")?.let { ClockStyle.valueOf(it) }
    val ripple = System.getenv("KLOKK_RIPPLE")?.let { raw ->
        raw == "1" || raw.equals("true", ignoreCase = true)
    }
    val theme = System.getenv("KLOKK_THEME")?.let { ThemeMode.valueOf(it) }
    val frameLog = System.getenv("KLOKK_FRAME_LOG")
    Window(
        onCloseRequest = ::exitApplication,
        title = "Klokk",
        state = rememberWindowState(
            width = 402.dp,
            height = 874.dp
        ),
        resizable = false
    ) {
        KlokkTheme {
            App(onReady = { state ->
                if (style != null) state.clockStyle = style
                if (ripple != null) state.rippleDigits = ripple
                if (theme != null) state.themeMode = theme
            })
            if (frameLog != null) FrameProbe(frameLog)
        }
    }
}

@Composable
private fun FrameProbe(path: String) {
    LaunchedEffect(path) {
        val deltas = ArrayList<Long>(200)
        var last = 0L
        if (System.getenv("KLOKK_FRAME_SYNC") == "minute") {
            while (java.time.LocalTime.now().second < 58) {
                withFrameNanos { }
            }
        }
        repeat(40) { withFrameNanos { } }
        repeat(180) {
            withFrameNanos { now ->
                if (last != 0L) deltas.add(now - last)
                last = now
            }
        }
        deltas.sort()
        val ms = deltas.map { it / 1_000_000.0 }
        val median = ms[ms.size / 2]
        val p95 = ms[(ms.size * 95) / 100]
        File(path).writeText(
            "frames=${ms.size} medianMs=$median p95Ms=$p95 minMs=${ms.first()} maxMs=${ms.last()}\n",
        )
    }
}
