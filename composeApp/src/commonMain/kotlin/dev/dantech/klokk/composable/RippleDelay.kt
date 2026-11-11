package dev.dantech.klokk.composable

import kotlin.math.sqrt

internal const val RIPPLE_STEP_MS = 36

internal fun cellRippleDelayMs(
    row: Int,
    col: Int,
    rows: Int,
    cols: Int,
    ripple: Boolean,
    background: Boolean,
    stepMs: Int = RIPPLE_STEP_MS,
): Int {
    if (!ripple || background) return 0
    return rippleDelayMs(row, col, rows, cols, stepMs)
}

internal fun rippleDelayMs(row: Int, col: Int, rows: Int, cols: Int, stepMs: Int): Int {
    val originRow = (rows - 1) * 0.5f
    val originCol = (cols - 1) * 0.5f
    val dRow = row - originRow
    val dCol = col - originCol
    return (sqrt(dRow * dRow + dCol * dCol) * stepMs).toInt()
}
