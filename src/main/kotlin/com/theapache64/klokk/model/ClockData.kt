package com.theapache64.klokk.model

import java.util.Calendar

/**
 * To hold individual clock data
 */
data class ClockData(
    var degreeOne: Float,
    var degreeTwo: Float,
    val animationDurationInMillis: Int = 1000,
    val clockAnimationType: Int = ClockAnimationType.NON_RESET.value,
    val timeSign: Long = Calendar.getInstance().timeInMillis
)

enum class ClockAnimationType(val value: Int) {
    NONE(0),
    NON_RESET(1),
    RESET_BEFORE_NEXT_TIME(2),
}