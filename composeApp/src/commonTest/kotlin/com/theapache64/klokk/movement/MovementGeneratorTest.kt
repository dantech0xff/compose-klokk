package com.theapache64.klokk.movement

import com.theapache64.klokk.COLUMNS
import com.theapache64.klokk.ROWS
import com.theapache64.klokk.model.ClockData
import com.theapache64.klokk.movement.core.Movement
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MovementGeneratorTest {

    @Test
    fun timeMatrixIsTheClockGrid() {
        val matrix = time(10, 0, 0)
        assertEquals(ROWS, matrix.size)
        assertTrue(matrix.all { it.size == COLUMNS })
    }

    @Test
    fun changingOneDigitLeavesTheOtherDigitsAlone() {
        val ten = time(10, 0, 0)
        val eleven = time(11, 0, 0)
        assertEquals(region(ten, 1, 1, 6, 3), region(eleven, 1, 1, 6, 3))
        assertNotEquals(region(ten, 1, 4, 6, 3), region(eleven, 1, 4, 6, 3))
    }

    @Test
    fun backgroundHandsTrackTheSecond() {
        val atZero = time(10, 0, 0)[0][0]
        val atFifteen = time(10, 0, 15)[0][0]
        assertEquals(0f, atZero.degreeOne)
        assertEquals(0f, atZero.degreeTwo)
        assertEquals(90f, atFifteen.degreeOne)
        assertEquals(90f, atFifteen.degreeTwo)
    }

    @Test
    fun zeroGlyphOccupiesTheHourTensCell() {
        val cell = time(10, 0, 0)[1][3]
        assertEquals(180f, cell.degreeOne)
        assertEquals(270f, cell.degreeTwo)
    }

    @Test
    fun standByPointsEveryHandAtRest() {
        val matrix = Movement.StandBy.getMatrixGenerator().getVerifiedMatrix()
        assertEquals(ROWS, matrix.size)
        assertTrue(matrix.all { row -> row.all { it.degreeOne == 0f && it.degreeTwo == 0f } })
    }

    @Test
    fun rippleStartAndEndAreDifferentGrids() {
        assertChoreographyDiffers(
            Movement.Ripple(Movement.Ripple.To.START),
            Movement.Ripple(Movement.Ripple.To.END),
        )
    }

    @Test
    fun tranceSquareAndFlowerAreDifferentGrids() {
        assertChoreographyDiffers(
            Movement.Trance(Movement.Trance.To.SQUARE),
            Movement.Trance(Movement.Trance.To.FLOWER),
        )
    }

    @Test
    fun waveStartAndEndAreDifferentGrids() {
        assertChoreographyDiffers(
            Movement.Wave(Movement.Wave.State.START),
            Movement.Wave(Movement.Wave.State.END),
        )
    }

    @Test
    fun letterAReplacesOnlyItsInkCells() {
        val matrix = Movement.Text("A").getMatrixGenerator().getVerifiedMatrix()
        assertEquals(ROWS, matrix.size)
        assertEquals(0f to 0f, hands(matrix[1][0]))
        assertEquals(135f to 225f, hands(matrix[1][1]))
        assertEquals(0f to 0f, hands(matrix[1][2]))
    }

    @Test
    fun unknownLetterIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            Movement.Text("B").getMatrixGenerator().getVerifiedMatrix()
        }
    }

    private fun time(hour: Int, minute: Int, second: Int): List<List<ClockData>> {
        val instant = LocalDateTime(2026, 11, 11, hour, minute, second)
            .toInstant(TimeZone.currentSystemDefault())
        return Movement.Time(instant).getMatrixGenerator().getVerifiedMatrix()
    }

    private fun region(
        matrix: List<List<ClockData>>,
        row: Int,
        col: Int,
        height: Int,
        width: Int,
    ): List<Pair<Float, Float>> {
        return (row until row + height).flatMap { r ->
            (col until col + width).map { c -> hands(matrix[r][c]) }
        }
    }

    private fun hands(cell: ClockData) = cell.degreeOne to cell.degreeTwo

    private fun assertChoreographyDiffers(a: Movement, b: Movement) {
        val left = a.getMatrixGenerator().getVerifiedMatrix()
        val right = b.getMatrixGenerator().getVerifiedMatrix()
        assertEquals(ROWS, left.size)
        assertEquals(ROWS, right.size)
        assertTrue(left.flatten().zip(right.flatten()).any { (l, r) ->
            l.degreeOne != r.degreeOne || l.degreeTwo != r.degreeTwo
        })
    }
}
