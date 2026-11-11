package com.theapache64.klokk

import android.app.Application
import com.theapache64.klokk.alarm.AndroidAlarmScheduler
import com.theapache64.klokk.persist.initKlokkAndroid
import com.theapache64.klokk.schedule.AndroidAlarmSchedulerHolder

class KlokkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKlokkAndroid(this)
        AndroidAlarmSchedulerHolder.scheduler = AndroidAlarmScheduler(this)
    }
}
