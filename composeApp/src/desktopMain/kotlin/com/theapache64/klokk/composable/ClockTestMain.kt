package com.theapache64.klokk.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.theapache64.klokk.model.ClockAnimationType
import com.theapache64.klokk.model.ClockData
import kotlin.random.Random

@ExperimentalFoundationApi
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kinetic Wall Clock Test",
        state = rememberWindowState(
            width = 500.dp,
            height = 500.dp
        ),
        resizable = false
    ) {
        var needleOneDegree by remember { mutableStateOf(Random.nextFloat() * 360) }
        var needleTwoDegree by remember { mutableStateOf(Random.nextFloat() * 360) }

        Column(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Clock(
                ClockData(
                    degreeOne = needleOneDegree,
                    degreeTwo = needleTwoDegree,
                    animationDurationInMillis = 500,
                    clockAnimationType = ClockAnimationType.RESET_BEFORE_NEXT_TIME.value
                ),
                modifier = Modifier.size(300.dp).padding(50.dp)
            )

            Button(
                onClick = {
                    needleOneDegree = Random.nextFloat() * 360
                    needleTwoDegree = Random.nextFloat() * 360
                }
            ) {
                Text(text = "Animate")
            }
        }
    }
}
