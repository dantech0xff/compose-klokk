package com.theapache64.klokk.movement

import com.theapache64.klokk.COLUMNS
import com.theapache64.klokk.ROWS
import com.theapache64.klokk.composable.KlokkCell
import com.theapache64.klokk.composable.KlokkCellMode
import com.theapache64.klokk.model.ClockData
import com.theapache64.klokk.movement.core.Movement
import com.theapache64.klokk.movement.ripple.RippleMatrixGenerator
import com.theapache64.klokk.movement.snake.WaveMatrixGenerator

/**
 * Which choreography the screensaver plays.
 */
enum class SaverShow(val label: String) {
    SHUFFLE("Shuffle"),
    RIPPLE("Ripple"),
    TRANCE("Trance"),
    WAVE("Wave"),
}

private const val SHOW_DURATION_MS = 5000

/**
 * Builders for the flat hand-degree matrices consumed by `KlokkGrid`.
 *
 * Ported from the design's logic: [digitsMatrix] stamps the 3x6 digit glyphs
 * used by the time matrix generators, while [trance]/[wave]/[ripple] delegate
 * to the existing generators and mark every cell absolute-timed.
 */
object KlokkMatrices {

    // Hand pairs from BaseMatrix.kt; '.' = untouched background clock
    private val H = mapOf(
        'a' to floatArrayOf(90f, 180f),
        'b' to floatArrayOf(90f, 270f),
        'c' to floatArrayOf(180f, 270f),
        'd' to floatArrayOf(0f, 180f),
        'e' to floatArrayOf(0f, 270f),
        'f' to floatArrayOf(0f, 90f),
        'g' to floatArrayOf(180f, 180f),
        'h' to floatArrayOf(0f, 0f),
        'i' to floatArrayOf(0f, 225f),
        'j' to floatArrayOf(45f, 180f),
        'k' to floatArrayOf(0f, 135f),
        'l' to floatArrayOf(180f, 315f),
    )

    private val DIGITS = listOf(
        "abc dgd ddd ddd dhd fbe",
        "abc fcd .dd .dd .dd .fe",
        "abc fcd aed dae dfc fbe",
        "abc fcd aed fcd aed fbe",
        "acc ddd dfd fcd .dd .fe",
        "abc dae dfc fcd aed fbe",
        "abc dae dfc dgd dhd fbe",
        "abc fcd .ii jj. dd. fe.",
        "abc dgd khi jgl dhd fbe",
        "abc dgd dhd fcd aed fbe",
    )

    // (row, col) top-left origin of each of the 4 digits
    private val POS = listOf(
        Pair(1, 1), Pair(1, 4), Pair(1, 8), Pair(1, 11)
    )

    /**
     * [digits] = 4 digits (HHMM or MMSS); [bgDeg] rotates every background cell
     * (the seconds sweep / timer progress ring); [mode] applies to all cells.
     */
    fun digitsMatrix(
        digits: IntArray,
        backgroundDegree: Float,
        digitDuration: Int,
        backgroundDuration: Int,
        mode: Int = KlokkCellMode.SHORTEST,
    ): List<List<KlokkCell>> {
        require(digits.size == 4) { "Expected 4 digits, got ${digits.size}" }
        val cells = MutableList(ROWS) {
            MutableList(COLUMNS) {
                KlokkCell(
                    backgroundDegree,
                    backgroundDegree,
                    backgroundDuration,
                    mode,
                    isBackground = true,
                )
            }
        }
        digits.forEachIndexed { di, digit ->
            val rows = DIGITS[digit].split(' ')
            val (r0, c0) = POS[di]
            rows.forEachIndexed { i, row ->
                for (j in 0 until 3) {
                    val ch = row[j]
                    if (ch != '.') {
                        val p = H.getValue(ch)
                        cells[r0 + i][c0 + j] = KlokkCell(p[0], p[1], digitDuration, mode)
                    }
                }
            }
        }
        return cells.map { it.toList() }
    }

    fun uniform(d1: Float, d2: Float, dur: Int, mode: Int): List<List<KlokkCell>> =
        List(ROWS) { List(COLUMNS) { KlokkCell(d1, d2, dur, mode) } }

    fun trance(to: Movement.Trance.To): List<List<KlokkCell>> =
        TranceMatrixGenerator.getTranceMatrix(Movement.Trance(to)).toCells()

    fun wave(state: Movement.Wave.State): List<List<KlokkCell>> =
        WaveMatrixGenerator(Movement.Wave(state)).getVerifiedMatrix().toCells()

    fun ripple(to: Movement.Ripple.To): List<List<KlokkCell>> =
        RippleMatrixGenerator.getRippleMatrix(Movement.Ripple(to)).toCells()

    /** The ordered choreography frames for a screensaver mode (5s each). */
    fun sequence(show: SaverShow): List<List<List<KlokkCell>>> = when (show) {
        SaverShow.SHUFFLE -> listOf(
            ripple(Movement.Ripple.To.START),
            ripple(Movement.Ripple.To.END),
            trance(Movement.Trance.To.SQUARE),
            trance(Movement.Trance.To.FLOWER),
            trance(Movement.Trance.To.FLY),
            trance(Movement.Trance.To.STAR),
            wave(Movement.Wave.State.START),
            wave(Movement.Wave.State.END),
            ripple(Movement.Ripple.To.TIME_TABLE),
        )

        SaverShow.RIPPLE -> listOf(
            ripple(Movement.Ripple.To.START),
            ripple(Movement.Ripple.To.END),
            ripple(Movement.Ripple.To.TIME_TABLE),
        )

        SaverShow.TRANCE -> Movement.Trance.To.entries.map { trance(it) }

        SaverShow.WAVE -> Movement.Wave.State.entries.map { wave(it) }
    }

    private fun List<List<ClockData>>.toCells(): List<List<KlokkCell>> =
        map { row ->
            row.map {
                KlokkCell(
                    it.degreeOne,
                    it.degreeTwo,
                    SHOW_DURATION_MS,
                    KlokkCellMode.ABSOLUTE,
                )
            }
        }
}
