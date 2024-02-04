package com.theapache64.klokk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.theapache64.klokk.composable.Clock
import com.theapache64.klokk.movement.core.Movement
import com.theapache64.klokk.theme.Black
import kotlinx.coroutines.delay
import java.util.*

private val BACKGROUND_COLOR = Black

enum class ClockMode(val value: Int){
    TIME(0), COUNT_TIME_TICKER(1)
}

@Composable
fun App() {
    var activeMovement by remember { mutableStateOf<Movement>(Movement.StandBy) }

    // Generating degree matrix using the active movement
    val degreeMatrix = activeMovement.getMatrixGenerator().getVerifiedMatrix()

    var clockMode by remember { mutableStateOf(0) }

    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }

    // The animation loop
    LaunchedEffect(clockMode) {
        while (clockMode == ClockMode.TIME.value) {
            currentTime = Calendar.getInstance().time

            val now = Calendar.getInstance()
            val date = now.time
            val movementTime = Movement.Time(date)
            activeMovement = movementTime
            delay(Movement.Time.MILLIS_PER_SECOND)
        }

        val date = Calendar.getInstance().time
        while (clockMode == ClockMode.COUNT_TIME_TICKER.value) {
            currentTime = Calendar.getInstance().time

            val movementCountTime =
                Movement.CountTimeTicker(Movement.CountTimeTicker.MILLIS_PER_SECOND.toInt(), date)
            activeMovement = movementCountTime
            delay(Movement.CountTimeTicker.MILLIS_PER_SECOND)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BACKGROUND_COLOR),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
            Text("Current Time $currentTime", color = Color.White, modifier = Modifier.padding(8.dp))
        }
    }
}