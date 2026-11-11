package dev.dantech.klokk.widget

/** The next whole minute. Widgets wake once then, and only while one is installed. */
fun nextMinuteEpochMs(nowMs: Long): Long {
    val minute = 60_000L
    return (nowMs / minute + 1) * minute
}

expect fun refreshHomeWidgets()
