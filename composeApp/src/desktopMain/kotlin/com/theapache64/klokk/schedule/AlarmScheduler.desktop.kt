package com.theapache64.klokk.schedule

actual fun defaultAlarmScheduler(): AlarmScheduler = NoopAlarmScheduler

actual fun alarmScheduleHorizon(): Int = 1
