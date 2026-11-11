package dev.dantech.klokk.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.dantech.klokk.widget.WidgetUpdates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KlokkRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> Unit
            else -> return
        }
        val pending = goAsync()
        scope.launch {
            try {
                KlokkAlarmReceiver.reschedule(context)
                if (WidgetUpdates.installed(context) > 0) WidgetUpdates.schedule(context)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
