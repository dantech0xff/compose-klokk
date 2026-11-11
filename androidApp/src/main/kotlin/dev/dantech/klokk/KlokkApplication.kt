package dev.dantech.klokk

import android.app.Application
import dev.dantech.klokk.alarm.AndroidAlarmScheduler
import dev.dantech.klokk.billing.RevenueCatKeyHolder
import dev.dantech.klokk.persist.initKlokkAndroid
import dev.dantech.klokk.schedule.AndroidAlarmSchedulerHolder
import dev.dantech.klokk.widget.HomeWidgetRefresher
import dev.dantech.klokk.widget.WidgetRefresher

class KlokkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKlokkAndroid(this)
        RevenueCatKeyHolder.value = BuildConfig.REVENUECAT_ANDROID_KEY
        AndroidAlarmSchedulerHolder.scheduler = AndroidAlarmScheduler(this)
        HomeWidgetRefresher.refresh = { WidgetRefresher.refresh(this) }
    }
}
