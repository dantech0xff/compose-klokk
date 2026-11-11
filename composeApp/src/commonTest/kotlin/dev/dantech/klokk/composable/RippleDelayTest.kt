package dev.dantech.klokk.composable

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RippleDelayTest {

    @Test
    fun fartherDigitCellsWaitLongerAndTheGridIsSymmetric() {
        val rows = 8
        val cols = 15
        val near = rippleDelayMs(3, 7, rows, cols, stepMs = 40)
        val corner = rippleDelayMs(0, 0, rows, cols, stepMs = 40)
        assertTrue(corner > near)
        assertEquals(near, rippleDelayMs(4, 7, rows, cols, stepMs = 40))
        assertEquals(
            rippleDelayMs(1, 2, rows, cols, stepMs = 40),
            rippleDelayMs(6, 12, rows, cols, stepMs = 40),
        )
        assertEquals(0, rippleDelayMs(3, 7, rows, cols, stepMs = 0))
    }

    @Test
    fun rippleWaitsOnlyForDigitCells() {
        assertEquals(0, cellRippleDelayMs(0, 0, 8, 15, ripple = true, background = true))
        assertEquals(0, cellRippleDelayMs(0, 0, 8, 15, ripple = false, background = false))
        val waited = cellRippleDelayMs(0, 0, 8, 15, ripple = true, background = false, stepMs = 36)
        assertEquals(rippleDelayMs(0, 0, 8, 15, 36), waited)
        assertTrue(waited > 0)
    }
}
