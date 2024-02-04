package com.theapache64.klokk.movement.core

import com.theapache64.klokk.movement.StandByMatrixGenerator
import com.theapache64.klokk.movement.TranceMatrixGenerator
import com.theapache64.klokk.movement.alphabet.TextMatrixGenerator
import com.theapache64.klokk.movement.ripple.RippleMatrixGenerator
import com.theapache64.klokk.movement.snake.WaveMatrixGenerator
import com.theapache64.klokk.movement.time.CountTimeTickerMatrixGenerator
import com.theapache64.klokk.movement.time.TimeMatrixGenerator
import java.util.*

/**
 * Dynamic animation can't be supported until this fixed -> https://issuetracker.google.com/issues/183220315
 */
const val DEFAULT_ANIMATION_DURATION = 5000


sealed class Movement(
    open val durationInMillis: Int,
) {

    abstract fun getMatrixGenerator(): MatrixGenerator<Movement>

    /**
     * To move clocks to stand by position
     */
    object StandBy : Movement(durationInMillis = DEFAULT_ANIMATION_DURATION) {
        override fun getMatrixGenerator(): MatrixGenerator<Movement> {
            return StandByMatrixGenerator(this)
        }

        override fun toString(): String {
            return "StandBy"
        }
    }

    /**
     * To play trance looking animations
     */
    data class Trance(
        val to: To = To.SQUARE,
    ) : Movement(
        durationInMillis = DEFAULT_ANIMATION_DURATION
    ) {
        enum class To {
            SQUARE, FLOWER, FLY, STAR
        }

        override fun getMatrixGenerator(): MatrixGenerator<Movement> {
            return TranceMatrixGenerator(this)
        }
    }

    /**
     * Ripple effect
     */
    data class Ripple(
        val to: To = To.START,
    ) : Movement(durationInMillis = DEFAULT_ANIMATION_DURATION) {

        override fun getMatrixGenerator(): MatrixGenerator<Movement> {
            return RippleMatrixGenerator(this)
        }

        enum class To {
            START, END, TIME_TABLE
        }
    }

    /**
     * To show time
     */
    data class Time(
        val date: Date = Date()
    ) : Movement(MILLIS_PER_SECOND.toInt()) {

        companion object {
            const val MILLIS_PER_SECOND = 1000L
        }

        override fun getMatrixGenerator(): MatrixGenerator<Time> {
            return TimeMatrixGenerator(this)
        }
    }

    data class CountTimeTicker(
        override val durationInMillis: Int,
        val startTime: Date = Calendar.getInstance().time,
        val sign: String = UUID.randomUUID().toString()
    ) : Movement(durationInMillis) {

        companion object {
            const val MILLIS_PER_SECOND = 1000L
        }

        override fun getMatrixGenerator(): MatrixGenerator<CountTimeTicker> {
            return CountTimeTickerMatrixGenerator(this)
        }
    }

    /**
     * TODO :WIP
     */
    data class Text(
        val text: String,
    ) : Movement(durationInMillis = DEFAULT_ANIMATION_DURATION) {
        override fun getMatrixGenerator(): MatrixGenerator<Movement> {
            return TextMatrixGenerator(this)
        }

        enum class Alignment {
            CENTER, LEFT
        }
    }

    data class Wave(
        val state: State,
    ) : Movement(durationInMillis = DEFAULT_ANIMATION_DURATION) {
        override fun getMatrixGenerator(): MatrixGenerator<Movement> {
            return WaveMatrixGenerator(this)
        }

        enum class State {
            START, END
        }
    }
}