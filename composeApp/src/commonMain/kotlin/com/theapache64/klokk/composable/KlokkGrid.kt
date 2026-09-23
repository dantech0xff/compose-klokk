package com.theapache64.klokk.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.theapache64.klokk.theme.LocalKlokkColors
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * One cell of the kinetic grid: the two target hand angles, how long the tween
 * takes, how it rotates (see [KlokkCellMode]) and whether it's a background cell
 * (dims when [KlokkGrid] is dimmed).
 */
data class KlokkCell(
    val degreeOne: Float,
    val degreeTwo: Float,
    val durationMs: Int,
    val mode: Int = KlokkCellMode.SHORTEST,
    val isBackground: Boolean = false,
)

object KlokkCellMode {
    /** Rotate through the smallest delta to the target. */
    const val SHORTEST = 0

    /** Raw tween to the angle (values >360 / <0 keep their extra turns). */
    const val ABSOLUTE = 1

    /** Jump to the target without a transform transition. */
    const val SNAP = 2
}

/**
 * Dumb grid of two-hand clocks, fed by a rows×cols [matrix].
 */
@Composable
fun KlokkGrid(
    matrix: List<List<KlokkCell>>,
    clockSize: Dp,
    dim: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        for (row in matrix) {
            Row {
                for (cell in row) {
                    KlokkCellView(
                        cell = cell,
                        dim = dim,
                        modifier = Modifier.requiredSize(clockSize)
                    )
                }
            }
        }
    }
}

@Composable
private fun KlokkCellView(
    cell: KlokkCell,
    dim: Boolean,
    modifier: Modifier = Modifier,
) {
    val handOne = remember { Animatable(0f) }
    val handTwo = remember { Animatable(0f) }

    LaunchedEffect(cell) {
        val targetOne = handTarget(handOne.value, cell.degreeOne, cell.mode)
        val targetTwo = handTarget(handTwo.value, cell.degreeTwo, cell.mode)
        if (cell.mode == KlokkCellMode.SNAP) {
            launch { handOne.snapTo(targetOne) }
            launch { handTwo.snapTo(targetTwo) }
        } else {
            launch {
                handOne.animateTo(
                    targetOne,
                    animationSpec = tween(durationMillis = cell.durationMs, easing = LinearEasing)
                )
            }
            launch {
                handTwo.animateTo(
                    targetTwo,
                    animationSpec = tween(durationMillis = cell.durationMs, easing = LinearEasing)
                )
            }
        }
    }

    val handAlpha by animateFloatAsState(
        targetValue = if (dim && cell.isBackground) 0.28f else 1f,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "handAlpha"
    )
    val faceColor = LocalKlokkColors.current.clockFace
    val handColor = LocalKlokkColors.current.clockHand

    Canvas(modifier = modifier) {
        val needleWidth = size.minDimension * 0.05f
        val radius = size.minDimension / 2f

        drawCircle(color = faceColor, radius = radius)

        val radius2 = (radius - needleWidth / 2f) * 0.98f
        val style = Stroke(width = needleWidth, cap = StrokeCap.Round)

        val radOne = handOne.value * Math.PI / 180
        drawLine(
            color = handColor,
            start = center,
            end = Offset(
                x = center.x + radius2 * sin(radOne).toFloat(),
                y = center.y - radius2 * cos(radOne).toFloat(),
            ),
            alpha = handAlpha,
            strokeWidth = style.width,
            cap = StrokeCap.Round,
        )

        val radTwo = handTwo.value * Math.PI / 180
        drawLine(
            color = handColor,
            start = center,
            end = Offset(
                x = center.x + radius2 * sin(radTwo).toFloat(),
                y = center.y - radius2 * cos(radTwo).toFloat(),
            ),
            alpha = handAlpha,
            strokeWidth = style.width,
            cap = StrokeCap.Round,
        )
    }
}

private fun handTarget(current: Float, degree: Float, mode: Int): Float {
    return if (mode == KlokkCellMode.ABSOLUTE) {
        floor(current / 360f) * 360f + degree
    } else {
        val c = ((current % 360f) + 360f) % 360f
        var delta = (degree - c) % 360f
        if (delta > 180f) delta -= 360f
        if (delta <= -180f) delta += 360f
        current + delta
    }
}
