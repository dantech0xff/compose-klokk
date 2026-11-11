package dev.dantech.klokk.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import dev.dantech.klokk.state.ClockStyle
import dev.dantech.klokk.theme.LocalKlokkColors
import kotlinx.coroutines.launch
import kotlin.math.PI
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

val LocalClockStyle = compositionLocalOf { ClockStyle.FLAT }
val LocalClockRipple = compositionLocalOf { false }

private val NeonField = Color(0xFF05060A)
private val NeonCore = Color(0xFFF7FEFF)
private val NeonMid = Color(0xFF9AEEFF)
private val NeonHalo = Color(0xFF3AD4FF)

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
    val style = LocalClockStyle.current
    val ripple = LocalClockRipple.current
    val rows = matrix.size
    val gridModifier = if (style == ClockStyle.NEON) modifier.background(NeonField) else modifier
    Column(modifier = gridModifier) {
        matrix.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    KlokkCellView(
                        cell = cell,
                        dim = dim,
                        row = rowIndex,
                        col = colIndex,
                        rows = rows,
                        cols = row.size,
                        style = style,
                        ripple = ripple,
                        modifier = Modifier.requiredSize(clockSize),
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
    row: Int,
    col: Int,
    rows: Int,
    cols: Int,
    style: ClockStyle,
    ripple: Boolean,
    modifier: Modifier = Modifier,
) {
    val handOne = remember { Animatable(0f) }
    val handTwo = remember { Animatable(0f) }
    val delay = cellRippleDelayMs(row, col, rows, cols, ripple, cell.isBackground)

    LaunchedEffect(cell, delay) {
        val targetOne = handTarget(handOne.value, cell.degreeOne, cell.mode)
        val targetTwo = handTarget(handTwo.value, cell.degreeTwo, cell.mode)
        if (cell.mode == KlokkCellMode.SNAP) {
            launch { handOne.snapTo(targetOne) }
            launch { handTwo.snapTo(targetTwo) }
        } else {
            val spec = tween<Float>(durationMillis = cell.durationMs, delayMillis = delay, easing = LinearEasing)
            launch { handOne.animateTo(targetOne, animationSpec = spec) }
            launch { handTwo.animateTo(targetTwo, animationSpec = spec) }
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
        val length = (radius - needleWidth / 2f) * 0.98f
        when (style) {
            ClockStyle.FLAT -> {
                drawCircle(color = faceColor, radius = radius)
                drawHand(handColor, handOne.value, length, needleWidth, handAlpha)
                drawHand(handColor, handTwo.value, length, needleWidth, handAlpha)
            }
            ClockStyle.HANDS -> {
                drawHand(handColor, handOne.value, length, needleWidth, handAlpha)
                drawHand(handColor, handTwo.value, length, needleWidth, handAlpha)
            }
            ClockStyle.NEON -> {
                drawGlowHand(handOne.value, length, needleWidth, handAlpha)
                drawGlowHand(handTwo.value, length, needleWidth, handAlpha)
            }
        }
    }
}

private fun DrawScope.drawGlowHand(angleDeg: Float, length: Float, width: Float, alpha: Float) {
    drawHand(NeonHalo, angleDeg, length, width * 3.2f, 0.20f * alpha)
    drawHand(NeonMid, angleDeg, length, width * 1.65f, 0.45f * alpha)
    drawHand(NeonCore, angleDeg, length, width, alpha)
}

private fun DrawScope.drawHand(
    color: Color,
    angleDeg: Float,
    length: Float,
    width: Float,
    alpha: Float,
) {
    val rad = angleDeg * PI / 180.0
    drawLine(
        color = color,
        start = center,
        end = Offset(
            x = center.x + (length * sin(rad)).toFloat(),
            y = center.y - (length * cos(rad)).toFloat(),
        ),
        alpha = alpha,
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
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
