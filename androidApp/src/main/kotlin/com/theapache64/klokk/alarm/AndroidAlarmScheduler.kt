package com.theapache64.klokk.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.theapache64.klokk.MainActivity
import com.theapache64.klokk.schedule.AlarmScheduler
import com.theapache64.klokk.schedule.WakeRequest

class AndroidAlarmScheduler(private val context: Context) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun replaceAll(requests: List<WakeRequest>) {
        val previous = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty().toSet()
        previous.forEach { cancel(it) }
        if (!canScheduleExact()) {
            prefs.edit().putStringSet(KEY_IDS, emptySet()).apply()
            return
        }
        val armed = mutableSetOf<String>()
        for (request in requests) {
            try {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(request.triggerEpochMs, showIntent(request)),
                    operation(request),
                )
                armed.add(request.requestId)
            } catch (_: SecurityException) {
                break
            }
        }
        prefs.edit().putStringSet(KEY_IDS, armed).apply()
    }

    private fun canScheduleExact(): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        return alarmManager.canScheduleExactAlarms()
    }

    private fun cancel(requestId: String) {
        alarmManager.cancel(operation(placeholder(requestId)))
    }

    private fun operation(request: WakeRequest): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            request.requestId.hashCode(),
            AlarmIntents.fire(context, request),
            FLAGS,
        )

    private fun showIntent(request: WakeRequest): PendingIntent =
        PendingIntent.getActivity(
            context,
            request.requestId.hashCode() xor SHOW_SALT,
            Intent(context, MainActivity::class.java),
            FLAGS,
        )

    private fun placeholder(requestId: String) = WakeRequest(requestId, 0, com.theapache64.klokk.schedule.WakeKind.ALARM, null, "")

    companion object {
        private const val PREFS = "klokk_scheduled"
        private const val KEY_IDS = "ids"
        private const val SHOW_SALT = 0x10000
        private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
