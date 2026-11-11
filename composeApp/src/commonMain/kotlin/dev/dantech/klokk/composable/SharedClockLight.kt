package dev.dantech.klokk.composable

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal object SharedClockLight {
    const val towardX = -0.6402f
    const val towardY = -0.7682f
    const val shadowFraction = 0.18f

    fun shadowDx(radius: Float): Float = -towardX * radius * shadowFraction

    fun shadowDy(radius: Float): Float = -towardY * radius * shadowFraction

    /** Center of the rim highlight, degrees clockwise from 3 o'clock (drawArc). */
    fun specularCenterDeg(): Float {
        var deg = atan2(towardY.toDouble(), towardX.toDouble()) * 180.0 / PI
        if (deg < 0.0) deg += 360.0
        return deg.toFloat()
    }

    fun tipX(angleDeg: Float, length: Float): Float =
        (sin(angleDeg * PI / 180.0) * length).toFloat()

    fun tipY(angleDeg: Float, length: Float): Float =
        (-cos(angleDeg * PI / 180.0) * length).toFloat()
}
