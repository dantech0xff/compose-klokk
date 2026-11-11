package dev.dantech.klokk.composable

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedClockLightTest {

    @Test
    fun theLightIsOneUnitVectorFromTheUpperLeft() {
        val length = sqrt(
            SharedClockLight.towardX * SharedClockLight.towardX +
                SharedClockLight.towardY * SharedClockLight.towardY,
        )
        assertEquals(1f, length, 0.002f)
        assertTrue(SharedClockLight.towardX < 0f)
        assertTrue(SharedClockLight.towardY < 0f)
    }

    @Test
    fun theShadowFallsStraightAwayFromTheLight() {
        val radius = 20f
        val dx = SharedClockLight.shadowDx(radius)
        val dy = SharedClockLight.shadowDy(radius)
        val along = dx * SharedClockLight.towardX + dy * SharedClockLight.towardY
        assertTrue(along < 0f, "shadow should oppose the light, dot was $along")
        val magnitude = sqrt(dx * dx + dy * dy)
        assertEquals(radius * SharedClockLight.shadowFraction, magnitude, 0.05f)
    }

    @Test
    fun aRotatingHandMovesItsShadowedTip() {
        val length = 10f
        val dx = SharedClockLight.shadowDx(length)
        val dy = SharedClockLight.shadowDy(length)
        val noonX = SharedClockLight.tipX(0f, length)
        val noonY = SharedClockLight.tipY(0f, length)
        val threeX = SharedClockLight.tipX(90f, length)
        val threeY = SharedClockLight.tipY(90f, length)
        assertEquals(0f, noonX, 0.001f)
        assertEquals(-length, noonY, 0.001f)
        assertEquals(length, threeX, 0.001f)
        assertEquals(0f, threeY, 0.001f)
        val noonShadowed = (noonX + dx) to (noonY + dy)
        val threeShadowed = (threeX + dx) to (threeY + dy)
        assertTrue(noonShadowed != threeShadowed)
    }

    @Test
    fun theHighlightSitsInTheUpperLeftQuadrant() {
        val center = SharedClockLight.specularCenterDeg()
        assertTrue(center in 180f..270f, "specular center $center")
    }
}
