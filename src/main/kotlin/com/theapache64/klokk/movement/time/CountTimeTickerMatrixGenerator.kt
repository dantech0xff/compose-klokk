package com.theapache64.klokk.movement.time

import com.theapache64.klokk.model.ClockAnimationType
import com.theapache64.klokk.model.ClockData
import com.theapache64.klokk.movement.StandByMatrixGenerator
import com.theapache64.klokk.movement.core.MatrixGenerator
import com.theapache64.klokk.movement.core.Movement
import java.util.*

class CountTimeTickerMatrixGenerator(countTimeTicker: Movement.CountTimeTicker) : MatrixGenerator<Movement.CountTimeTicker>(countTimeTicker) {
    companion object {

        private const val DIGIT_COLUMNS = 3
        private const val DIGIT_ROWS = 6

        private val h1Position = Pair(1, 1)
        private val h2Position = Pair(1, 4)
        private val m1Position = Pair(1, 8)
        private val m2Position = Pair(1, 11)

        private fun getMatrixFor(digit: Int): List<List<ClockData?>> {
            val digitMatrix: DigitMatrix = when (digit) {
                0 -> ZeroMatrix
                1 -> OneMatrix
                2 -> TwoMatrix
                3 -> ThreeMatrix
                4 -> FourMatrix
                5 -> FiveMatrix
                6 -> SixMatrix
                7 -> SevenMatrix
                8 -> EightMatrix
                9 -> NineMatrix
                else -> throw IllegalAccessException("Matrix not defined for $digit")
            }

            return verifyIntegrityAndReturn(digitMatrix.getMatrix())
        }

        private fun mapMatrixClockData(
            matrix: List<List<ClockData?>>,
            time: Movement.CountTimeTicker
        ): List<List<ClockData?>> {
            return matrix.map { clockList ->
                clockList.map { clockItem ->
                    clockItem?.copy(
                        animationDurationInMillis = 200,
                    )
                }
            }
        }


        private fun verifyIntegrityAndReturn(matrix: List<List<ClockData?>>): List<List<ClockData?>> {
            require(matrix.size == DIGIT_ROWS) {
                "No of digit rows should be $DIGIT_ROWS but found ${matrix.size}"
            }
            for (columns in matrix) {
                require(columns.size == DIGIT_COLUMNS) {
                    "No of digit columns should be $DIGIT_COLUMNS, but found ${columns.size}"
                }
            }
            return matrix
        }

    }

    private fun getTimeMatrix(time: Movement.CountTimeTicker): List<List<ClockData>> {

        val diff = (Calendar.getInstance().timeInMillis - time.startTime.time).coerceAtLeast(0)
        val h: Int = (diff / (1000 * 60 * 60)).toInt()
        val m: Int = (diff - h * 1000 * 60 * 60).toInt() / (1000 * 60)
        val s: Int = (diff - h * 1000 * 60 * 60 - m * 1000 * 60).toInt() / 1000

        val h1Matrix = getMatrixFor((m / 10))
        val h2Matrix = getMatrixFor((m % 10))
        val m1Matrix = getMatrixFor(s / 10)
        val m2Matrix = getMatrixFor(s % 10)

        val seconds = s

        println("Seconds: $seconds")

        return mutableListOf<List<ClockData>>().apply {
            val fullMatrix = StandByMatrixGenerator(Movement.StandBy)
                .getVerifiedMatrix()
                .map {
                    it.toMutableList().map { clockItem ->
                        clockItem.copy(
                            animationDurationInMillis = time.durationInMillis,
                            degreeOne = 360f * seconds / 60f,
                            degreeTwo = 360f * seconds / 60f,
                            clockAnimationType = ClockAnimationType.RESET_BEFORE_NEXT_TIME.value,
                            timeSign = Calendar.getInstance().timeInMillis
                        )
                    }.toMutableList()
                }
                .toMutableList()

            // First hour
            replace(fullMatrix, mapMatrixClockData(h1Matrix, time), h1Position)

            replace(fullMatrix, mapMatrixClockData(h2Matrix, time), h2Position)

            replace(fullMatrix, mapMatrixClockData(m1Matrix, time), m1Position)

            replace(fullMatrix, mapMatrixClockData(m2Matrix, time), m2Position)

            // Finally add to list
            addAll(fullMatrix)
        }
    }

    override fun generateMatrix(): List<List<ClockData>> {
        return getTimeMatrix(data)
    }

    override fun getUnitRowCount(): Int = DIGIT_ROWS
    override fun getUnitColumnCount(): Int = DIGIT_COLUMNS
}