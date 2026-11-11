package dev.dantech.klokk.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import dev.dantech.klokk.state.ClockDepth
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

val LocalClockDepth = compositionLocalOf { ClockDepth.RAISED }

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
    val depth = LocalClockDepth.current
    val faceColor = LocalKlokkColors.current.clockFace
    val handColor = LocalKlokkColors.current.clockHand

    Spacer(
        modifier.drawWithCache {
            val dial = if (depth == ClockDepth.RAISED) raisedDial(size, faceColor) else null
            onDrawBehind {
                if (dial == null) {
                    drawFlat(faceColor, handColor, handOne.value, handTwo.value, handAlpha)
                } else {
                    drawRaised(dial, handColor, handOne.value, handTwo.value, handAlpha)
                }
            }
        }
    )
}

private class RaisedDial(
    val face: Brush,
    val well: Brush,
    val rim: Brush,
    val rimStroke: Stroke,
    val specStroke: Stroke,
    val specAlpha: Float,
    val specStart: Float,
    val radius: Float,
    val shadow: Offset,
    val handWidth: Float,
    val handLength: Float,
    val shadowAlpha: Float,
    val highlight: Color,
    val inset: Float,
)

private fun raisedDial(size: Size, face: Color): RaisedDial {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val lightFace = face.red + face.green + face.blue > 1.8f
    val light = Offset(SharedClockLight.towardX, SharedClockLight.towardY)
    val shift = light * (radius * 0.28f)
    val lit = face.mix(Color.White, if (lightFace) 0.72f else 0.34f)
    val shade = face.mix(Color.Black, if (lightFace) 0.2f else 0.5f)
    val rimWidth = size.minDimension * 0.085f
    val handWidth = size.minDimension * 0.095f
    return RaisedDial(
        face = Brush.radialGradient(
            colors = listOf(lit, face, shade),
            center = center + shift,
            radius = radius * 1.05f,
        ),
        well = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = if (lightFace) 0.22f else 0.48f),
            ),
            center = center - shift,
            radius = radius * 0.98f,
        ),
        rim = Brush.linearGradient(
            colors = listOf(
                face.mix(Color.White, if (lightFace) 0.95f else 0.82f),
                face.mix(Color.Black, if (lightFace) 0.45f else 0.72f),
            ),
            start = center + light * radius,
            end = center - light * radius,
        ),
        rimStroke = Stroke(width = rimWidth),
        specStroke = Stroke(width = rimWidth * 0.4f, cap = StrokeCap.Round),
        specAlpha = if (lightFace) 0.5f else 0.7f,
        specStart = SharedClockLight.specularCenterDeg() - 40f,
        radius = radius,
        shadow = Offset(SharedClockLight.shadowDx(radius), SharedClockLight.shadowDy(radius)),
        handWidth = handWidth,
        handLength = (radius - rimWidth - handWidth * 0.45f).coerceAtLeast(radius * 0.45f),
        shadowAlpha = if (lightFace) 0.38f else 0.62f,
        highlight = handHighlight(face),
        inset = rimWidth * 0.45f,
    )
}

private fun handHighlight(face: Color): Color {
    val lightFace = face.red + face.green + face.blue > 1.8f
    return if (lightFace) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.42f)
}

private fun DrawScope.drawFlat(
    faceColor: Color,
    handColor: Color,
    angleOne: Float,
    angleTwo: Float,
    handAlpha: Float,
) {
    val needleWidth = size.minDimension * 0.05f
    val radius = size.minDimension / 2f
    drawCircle(color = faceColor, radius = radius)
    val radius2 = (radius - needleWidth / 2f) * 0.98f
    drawNeedle(handColor, angleOne, radius2, needleWidth, handAlpha, Offset.Zero)
    drawNeedle(handColor, angleTwo, radius2, needleWidth, handAlpha, Offset.Zero)
}

private fun DrawScope.drawRaised(
    dial: RaisedDial,
    handColor: Color,
    angleOne: Float,
    angleTwo: Float,
    handAlpha: Float,
) {
    drawCircle(brush = dial.face, radius = dial.radius)
    drawCircle(brush = dial.well, radius = dial.radius * 0.94f)
    drawCircle(
        brush = dial.rim,
        radius = dial.radius - dial.rimStroke.width / 2f,
        style = dial.rimStroke,
    )
    val inset = dial.inset
    drawArc(
        color = Color.White.copy(alpha = dial.specAlpha),
        startAngle = dial.specStart,
        sweepAngle = 80f,
        useCenter = false,
        topLeft = Offset(inset, inset),
        size = Size(size.width - inset * 2f, size.height - inset * 2f),
        style = dial.specStroke,
    )
    val nudge = Offset(SharedClockLight.towardX, SharedClockLight.towardY) * (dial.handWidth * 0.22f)
    drawHandStack(dial, handColor, angleOne, handAlpha, nudge)
    drawHandStack(dial, handColor, angleTwo, handAlpha, nudge)
    val hub = dial.handWidth * 0.55f
    drawCircle(
        color = Color.Black,
        radius = hub,
        center = center + dial.shadow * 0.45f,
        alpha = dial.shadowAlpha * handAlpha,
    )
    drawCircle(color = handColor, radius = hub, alpha = handAlpha)
    drawCircle(color = dial.highlight, radius = hub * 0.45f, center = center + nudge, alpha = handAlpha)
}

private fun DrawScope.drawHandStack(
    dial: RaisedDial,
    handColor: Color,
    angle: Float,
    handAlpha: Float,
    nudge: Offset,
) {
    drawNeedle(
        Color.Black,
        angle,
        dial.handLength,
        dial.handWidth * 1.45f,
        dial.shadowAlpha * handAlpha,
        dial.shadow,
    )
    drawNeedle(handColor, angle, dial.handLength, dial.handWidth, handAlpha, Offset.Zero)
    drawNeedle(
        dial.highlight,
        angle,
        dial.handLength * 0.92f,
        dial.handWidth * 0.38f,
        handAlpha,
        nudge,
    )
}

private fun DrawScope.drawNeedle(
    color: Color,
    angleDeg: Float,
    length: Float,
    width: Float,
    alpha: Float,
    shift: Offset,
) {
    val end = Offset(
        x = center.x + SharedClockLight.tipX(angleDeg, length),
        y = center.y + SharedClockLight.tipY(angleDeg, length),
    )
    drawLine(
        color = color,
        start = center + shift,
        end = end + shift,
        alpha = alpha,
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
}

private fun Color.mix(other: Color, amount: Float) = Color(
    red = red + (other.red - red) * amount,
    green = green + (other.green - green) * amount,
    blue = blue + (other.blue - blue) * amount,
    alpha = alpha + (other.alpha - alpha) * amount,
)

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
