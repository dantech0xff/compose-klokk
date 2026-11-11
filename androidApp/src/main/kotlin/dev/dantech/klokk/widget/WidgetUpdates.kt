package dev.dantech.klokk.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object WidgetUpdates {
    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val trigger = nextMinuteEpochMs(System.currentTimeMillis())
        val pending = tick(context)
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, trigger, pending)
            } else {
                alarmManager.setExact(AlarmManager.RTC, trigger, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, trigger, pending)
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(tick(context))
    }

    fun considerStop(context: Context) {
        if (installed(context) == 0) cancel(context)
    }

    fun installed(context: Context): Int {
        val manager = AppWidgetManager.getInstance(context)
        return receivers.sumOf { manager.getAppWidgetIds(ComponentName(context, it)).size }
    }

    private fun tick(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST,
            Intent(context, WidgetTickReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private val receivers = listOf(
        KlokkSmallReceiver::class.java,
        KlokkMediumReceiver::class.java,
        KlokkAlarmReceiverWidget::class.java,
    )

    private const val REQUEST = 42
}

class WidgetTickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        scope.launch {
            try {
                if (WidgetUpdates.installed(context) == 0) {
                    WidgetUpdates.cancel(context)
                    return@launch
                }
                WidgetRefresher.refresh(context)
                WidgetUpdates.schedule(context)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
