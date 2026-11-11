package dev.dantech.klokk.widget

import kotlin.test.Test
import kotlin.test.assertEquals

class WidgetCadenceTest {
    @Test
    fun theNextWakeIsTheFollowingMinute() {
        assertEquals(60_000L, nextMinuteEpochMs(0))
        assertEquals(120_000L, nextMinuteEpochMs(60_000L))
        assertEquals(120_000L, nextMinuteEpochMs(61_000L))
    }
}
