package dev.dantech.klokk.schedule

object AndroidAlarmSchedulerHolder {
    var scheduler: AlarmScheduler = NoopAlarmScheduler
}

actual fun defaultAlarmScheduler(): AlarmScheduler = AndroidAlarmSchedulerHolder.scheduler

actual fun alarmScheduleHorizon(): Int = 1
