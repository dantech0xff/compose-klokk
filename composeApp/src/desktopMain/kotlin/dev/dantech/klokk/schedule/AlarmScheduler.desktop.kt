package dev.dantech.klokk.schedule

actual fun defaultAlarmScheduler(): AlarmScheduler = NoopAlarmScheduler

actual fun alarmScheduleHorizon(): Int = 1
