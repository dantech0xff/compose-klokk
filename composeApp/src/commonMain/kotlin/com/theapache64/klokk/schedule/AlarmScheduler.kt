package com.theapache64.klokk.schedule

import com.theapache64.klokk.state.KlokkState

interface AlarmScheduler {
    fun replaceAll(requests: List<WakeRequest>)
}

expect fun defaultAlarmScheduler(): AlarmScheduler

/** How many future fires of each alarm to arm. iOS needs more than one. */
expect fun alarmScheduleHorizon(): Int

object NoopAlarmScheduler : AlarmScheduler {
    override fun replaceAll(requests: List<WakeRequest>) = Unit
}

/**
 * The compose tree's state, when it exists. A background receiver uses it so
 * it does not write the store underneath a live [KlokkState].
 */
object KlokkRuntime {
    var state: KlokkState? = null
}
