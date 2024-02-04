package com.theapache64.klokk.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.theapache64.klokk.CLOCKS_CONTAINER_HEIGHT
import com.theapache64.klokk.CLOCKS_CONTAINER_WIDTH
import com.theapache64.klokk.PADDING
import com.theapache64.klokk.model.ClockAnimationType
import com.theapache64.klokk.model.ClockData
import com.theapache64.klokk.movement.core.Movement
import com.theapache64.klokk.theme.CodGray
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


private val NEEDLE_COLOR = Color.White
val CLOCK_BACKGROUND = CodGray


@Composable
fun Clock(
    clockData: ClockData,
    easing: Easing = LinearEasing, modifier: Modifier = Modifier) {

    var timeSign by remember { mutableStateOf(clockData.timeSign) }
    var needleOneDegree by remember { mutableStateOf(clockData.degreeOne) }
    var needleTwoDegree by remember { mutableStateOf(clockData.degreeTwo) }
    var durationInMillis by remember { mutableStateOf(clockData.animationDurationInMillis) }

    val animatableRadiantNeedleOne = remember { Animatable(0f) }
    val animatableRadianNeedleTwo = remember { Animatable(0f) }

    needleOneDegree = clockData.degreeOne
    needleTwoDegree = clockData.degreeTwo
    durationInMillis = clockData.animationDurationInMillis
    timeSign = clockData.timeSign

    LaunchedEffect(timeSign) {
        launch {
            val needleOneRadian = (needleOneDegree * Math.PI / 180).toFloat()
            animatableRadiantNeedleOne.animateTo(
                needleOneRadian,
                animationSpec = tween(durationMillis = durationInMillis, easing = easing)
            )
        }

        launch {
            val needleTwoRadian = (needleTwoDegree * Math.PI / 180).toFloat()
            animatableRadianNeedleTwo.animateTo(
                needleTwoRadian,
                animationSpec = tween(durationMillis = durationInMillis, easing = easing)
            )
        }
    }

    Canvas(modifier = modifier) {

        val needleWidth = size.minDimension * 0.05f

        // Background
        val radius = size.minDimension / 2f

        drawCircle(
            color = CLOCK_BACKGROUND,
            radius = radius
        )

        val radius2 = (radius - needleWidth / 2f) * 0.98f

        // Needle One
        drawLine(
            color = NEEDLE_COLOR,
            start = center,
            end = Offset(
                // Finding end coordinate for the given radian
                x = center.x + radius2 * sin(animatableRadiantNeedleOne.value),
                y = center.y - radius2 * cos(animatableRadiantNeedleOne.value),
            ),
            strokeWidth = needleWidth,
            cap = StrokeCap.Round
        )


        // Needle two
        drawLine(
            color = NEEDLE_COLOR,
            start = center,
            end = Offset(
                // Finding end coordinate for the given degree
                x = center.x + radius2 * sin(animatableRadianNeedleTwo.value),
                y = center.y - radius2 * cos(animatableRadianNeedleTwo.value),
            ),
            strokeWidth = needleWidth,
            cap = StrokeCap.Round
        )
    }
}


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
