package com.theapache64.klokk.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.drawable.Icon
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.theapache64.klokk.R
import com.theapache64.klokk.persist.defaultKlokkRepository
import com.theapache64.klokk.schedule.KlokkRuntime
import com.theapache64.klokk.schedule.WakeKind
import com.theapache64.klokk.schedule.WakeRequest
import com.theapache64.klokk.schedule.afterFire
import com.theapache64.klokk.schedule.planWakes
import com.theapache64.klokk.state.RingKind
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KlokkAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    AlarmIntents.ACTION_SNOOZE -> snooze(context, intent)
                    AlarmIntents.ACTION_DISMISS -> dismiss(context, intent)
                    else -> fire(context, intent)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        const val CHANNEL = "klokk_alarms"

        suspend fun reschedule(context: Context) {
            val snapshot = defaultKlokkRepository().load()
            AndroidAlarmScheduler(context).replaceAll(
                planWakes(snapshot, Clock.System.now(), 1),
            )
        }
    }
}

private suspend fun fire(context: Context, intent: Intent) {
    val request = AlarmIntents.request(intent) ?: return
    withContext(Dispatchers.Main) {
        val state = KlokkRuntime.state
        if (state != null) {
            val now = Clock.System.now().toEpochMilliseconds()
            when (request.kind) {
                WakeKind.TIMER -> {
                    state.tmRunning = false
                    state.tmEndTs = 0
                    state.tmRemaining = 0
                    state.startRing(RingKind.TIMER, null, now)
                }
                WakeKind.SNOOZE -> {
                    state.snoozeUntil = 0
                    state.startRing(RingKind.ALARM, null, now)
                }
                WakeKind.ALARM -> {
                    val index = state.alarms.indexOfFirst { it.id == request.alarmId }
                    if (index >= 0 && state.alarms[index].days.none { it }) {
                        state.alarms[index] = state.alarms[index].copy(on = false)
                    }
                    state.startRing(RingKind.ALARM, request.alarmId, now)
                }
            }
        } else {
            val repo = defaultKlokkRepository()
            val next = afterFire(repo.load(), request)
            repo.save(next)
            AndroidAlarmScheduler(context).replaceAll(planWakes(next, Clock.System.now(), 1))
        }
    }
    AlarmNotifications.show(context, request)
}

private suspend fun snooze(context: Context, intent: Intent) {
    val now = Clock.System.now().toEpochMilliseconds()
    withContext(Dispatchers.Main) {
        val state = KlokkRuntime.state
        if (state != null) {
            state.snooze(now)
        } else {
            val repo = defaultKlokkRepository()
            val request = AlarmIntents.request(intent)
            val base = if (request == null) repo.load() else afterFire(repo.load(), request)
            val next = base.copy(snoozeEpochMs = now + 9 * 60_000)
            repo.save(next)
            AndroidAlarmScheduler(context).replaceAll(
                planWakes(next, Instant.fromEpochMilliseconds(now), 1),
            )
        }
    }
    AlarmNotifications.cancel(context, intent)
}

private suspend fun dismiss(context: Context, intent: Intent) {
    withContext(Dispatchers.Main) {
        KlokkRuntime.state?.stopRing()
    }
    AlarmNotifications.cancel(context, intent)
}

object AlarmNotifications {
    fun show(context: Context, request: WakeRequest) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                KlokkAlarmReceiver.CHANNEL,
                context.getString(R.string.alarm_channel),
                NotificationManager.IMPORTANCE_HIGH,
            )
            channel.setSound(
                sound,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            channel.enableVibration(true)
            manager.createNotificationChannel(channel)
        }
        val open = pendingActivity(context, request)
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(context, KlokkAlarmReceiver.CHANNEL)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context).setSound(sound)
        }
        builder
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(request.title.ifBlank { context.getString(R.string.alarm_channel) })
            .setContentText(context.getString(if (request.kind == WakeKind.TIMER) R.string.alarm_timer else R.string.alarm_ringing))
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .setFullScreenIntent(open, true)
            .setContentIntent(open)
            .addAction(action(context, R.string.alarm_dismiss, AlarmIntents.dismiss(context, request)))
        if (request.kind != WakeKind.TIMER) {
            builder.addAction(action(context, R.string.alarm_snooze, AlarmIntents.snooze(context, request)))
        }
        manager.notify(request.requestId.hashCode(), builder.build())
    }

    fun cancel(context: Context, intent: Intent) {
        val id = intent.getStringExtra(AlarmIntents.EXTRA_ID) ?: return
        context.getSystemService(NotificationManager::class.java).cancel(id.hashCode())
    }

    private fun pendingActivity(context: Context, request: WakeRequest): PendingIntent =
        PendingIntent.getActivity(
            context,
            request.requestId.hashCode(),
            AlarmIntents.ring(context, request).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun action(context: Context, label: Int, intent: Intent): Notification.Action =
        Notification.Action.Builder(
            Icon.createWithResource(context, android.R.drawable.ic_lock_idle_alarm),
            context.getString(label),
            pendingBroadcast(context, intent),
        ).build()

    private fun pendingBroadcast(context: Context, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            intent.action.hashCode() xor intent.getStringExtra(AlarmIntents.EXTRA_ID).orEmpty().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
