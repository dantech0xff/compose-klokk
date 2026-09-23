package com.theapache64.klokk.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.theme.KlokkTextPrimary
import kotlin.math.cos
import kotlin.math.sin

/** Flat (no-ripple) click handler matching the design's button styling. */
fun Modifier.tap(onClick: () -> Unit): Modifier = composed {
    this.then(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    )
}

/** White-on-black pill button (Add city, Done, Unlock, primary CTAs). */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    fontSize: Float = 15f,
    alpha: Float = 1f,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(if (enabled) KlokkTextPrimary else KlokkTextPrimary)
            .tap { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Color.Black.copy(alpha = alpha),
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** Large 76dp circular action (Start/Pause/Resume/Cancel/End). */
@Composable
fun CircleActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    bold: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(if (filled) KlokkTextPrimary else Color.Transparent)
            .then(
                if (filled) Modifier else Modifier.border(1.dp, Color(0x38ffffff), CircleShape)
            )
            .tap(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (filled) Color.Black else KlokkTextPrimary,
            fontSize = 15.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = if (bold) 0.6.sp else 0.sp,
        )
    }
}

/** Plus glyph used on the "Add city" and "New alarm" affordances. */
@Composable
fun PlusGlyph(size: Dp = 16.dp, color: Color = KlokkTextPrimary, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val mid = this.size.minDimension / 2f
        val stroke = 1.5.dp.toPx()
        drawLine(color, Offset(0f, mid), Offset(this.size.width, mid), strokeWidth = stroke)
        drawLine(color, Offset(mid, 0f), Offset(mid, this.size.height), strokeWidth = stroke)
    }
}

/** iOS-style switch: 46x28 pill, white track + black knob when on. */
@Composable
fun KlokkToggle(checked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(46.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) KlokkTextPrimary else Color(0xff2a2a2a))
            .tap(onToggle)
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) Color.Black else Color(0xff8b8b8b))
        )
    }
}

/** The tiny chevrons shown above/below the hero grid while it is scrubbable. */
@Composable
fun ScrubTriangle(up: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(8.dp, 5.dp)) {
        val p = Path()
        if (up) {
            p.moveTo(this.size.width / 2f, 0f)
            p.lineTo(this.size.width, this.size.height)
            p.lineTo(0f, this.size.height)
        } else {
            p.moveTo(0f, 0f)
            p.lineTo(this.size.width, 0f)
            p.lineTo(this.size.width / 2f, this.size.height)
        }
        p.close()
        drawPath(p, Color(0xff8b8b8b))
    }
}

/** Analog dial used by the widget previews (hour/minute hands, no seconds). */
@Composable
fun AnalogDial(
    hourDeg: Float,
    minDeg: Float,
    size: Dp,
    faceColor: Color,
    handColor: Color,
    handWidth: Dp,
    hourLen: Dp,
    minLen: Dp,
    centerDot: Dp? = null,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        drawCircle(faceColor, radius)
        val w = handWidth.toPx()
        val hourRad = hourDeg * Math.PI / 180
        drawLine(
            handColor, center,
            Offset(
                center.x + hourLen.toPx() * sin(hourRad).toFloat(),
                center.y - hourLen.toPx() * cos(hourRad).toFloat()
            ),
            strokeWidth = w, cap = StrokeCap.Round,
        )
        val minRad = minDeg * Math.PI / 180
        drawLine(
            handColor, center,
            Offset(
                center.x + minLen.toPx() * sin(minRad).toFloat(),
                center.y - minLen.toPx() * cos(minRad).toFloat()
            ),
            strokeWidth = w, cap = StrokeCap.Round,
        )
        if (centerDot != null) {
            drawCircle(handColor, centerDot.toPx() / 2f)
        }
    }
}
