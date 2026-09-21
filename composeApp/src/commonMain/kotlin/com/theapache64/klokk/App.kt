package com.theapache64.klokk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.theapache64.klokk.composable.Clock
import com.theapache64.klokk.movement.core.Movement
import com.theapache64.klokk.theme.Black
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

const val IS_DEBUG = true
const val COLUMNS = 15
const val ROWS = 8
const val PADDING = 100
const val CLOCK_SIZE = 60
const val CLOCKS_CONTAINER_WIDTH = CLOCK_SIZE * COLUMNS
const val CLOCKS_CONTAINER_HEIGHT = CLOCK_SIZE * ROWS

private val BACKGROUND_COLOR = Black

enum class ClockMode(val value: Int) {
    TIME(0), COUNT_TIME_TICKER(1)
}

@Composable
fun App() {
    var activeMovement by remember { mutableStateOf<Movement>(Movement.StandBy) }

    // Generating degree matrix using the active movement
    val degreeMatrix = activeMovement.getMatrixGenerator().getVerifiedMatrix()

    var clockMode by remember { mutableStateOf(0) }

    var currentTime by remember { mutableStateOf(Clock.System.now()) }

    // The animation loop
    LaunchedEffect(Unit) {
        while (true) {
            while (clockMode == ClockMode.TIME.value) {
                currentTime = Clock.System.now()

                val movementTime = Movement.Time(currentTime)
                activeMovement = movementTime
                delay(Movement.Time.MILLIS_PER_SECOND)
            }

            val tickerStart = Clock.System.now()
            while (clockMode == ClockMode.COUNT_TIME_TICKER.value) {
                currentTime = Clock.System.now()

                val movementCountTime =
                    Movement.CountTimeTicker(Movement.CountTimeTicker.MILLIS_PER_SECOND.toInt(), tickerStart)
                activeMovement = movementCountTime
                delay(Movement.CountTimeTicker.MILLIS_PER_SECOND)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(BACKGROUND_COLOR),
        contentAlignment = Alignment.Center
    ) {
        // The dial grid is designed at a fixed 900x480 dp; scale it down to fit smaller screens (e.g. phones)
        val scale = minOf(
            maxWidth.value / (CLOCKS_CONTAINER_WIDTH + PADDING),
            maxHeight.value / (CLOCKS_CONTAINER_HEIGHT + 120)
        ).coerceAtMost(1f)

        Column(
            modifier = Modifier
                .requiredWidth((CLOCKS_CONTAINER_WIDTH + PADDING).dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            repeat(ROWS) { i ->
                Row {
                    repeat(COLUMNS) { j ->
                        val clockData = degreeMatrix[i][j]
                        Clock(
                            clockData,
                            modifier = Modifier.requiredSize(CLOCK_SIZE.dp)
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    clockMode = if (clockMode == ClockMode.COUNT_TIME_TICKER.value) {
                        ClockMode.TIME.value
                    } else {
                        ClockMode.COUNT_TIME_TICKER.value
                    }
                }) {
                    Text(
                        if (clockMode == ClockMode.COUNT_TIME_TICKER.value) {
                            "Klokk Time"
                        } else {
                            "Klokk Ticker"
                        }, color = Color.Black, modifier = Modifier.padding(4.dp)
                    )
                }
                Text("Current Time ${currentTime.displayTime()}", color = Color.White, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

private fun Instant.displayTime(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.date} ${local.hour.twoDigits()}:${local.minute.twoDigits()}:${local.second.twoDigits()}"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')
