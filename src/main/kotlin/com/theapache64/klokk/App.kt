package com.theapache64.klokk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.theapache64.klokk.composable.Clock
import com.theapache64.klokk.movement.core.Movement
import com.theapache64.klokk.theme.Black
import kotlinx.coroutines.delay
import java.util.*

private val BACKGROUND_COLOR = Black

@Composable
fun App() {
    var activeMovement by remember { mutableStateOf<Movement>(Movement.StandBy) }

    // Generating degree matrix using the active movement
    val degreeMatrix = activeMovement.getMatrixGenerator().getVerifiedMatrix()

    // The animation loop
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val date = now.time
            val movementTime = Movement.Time(date)
            activeMovement = movementTime
            val remainingMilliSeconds = movementTime.getDelayMillisNextUpdate()
            println("Remaining milli seconds : $remainingMilliSeconds")
            delay(remainingMilliSeconds.toLong())
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
    }
}