package com.theapache64.klokk.alarm

import android.content.Context
import android.content.Intent
import com.theapache64.klokk.schedule.WakeKind
import com.theapache64.klokk.schedule.WakeRequest

object AlarmIntents {
    const val ACTION_FIRE = "com.theapache64.klokk.action.FIRE"
    const val ACTION_SNOOZE = "com.theapache64.klokk.action.SNOOZE"
    const val ACTION_DISMISS = "com.theapache64.klokk.action.DISMISS"
    const val EXTRA_ID = "id"
    const val EXTRA_KIND = "kind"
    const val EXTRA_TITLE = "title"
    const val EXTRA_ALARM_ID = "alarmId"
    const val EXTRA_TRIGGER = "trigger"

    fun request(intent: Intent): WakeRequest? {
        val id = intent.getStringExtra(EXTRA_ID) ?: return null
        val kind = intent.getStringExtra(EXTRA_KIND)?.let { runCatching { WakeKind.valueOf(it) }.getOrNull() } ?: return null
        val alarmId = if (intent.hasExtra(EXTRA_ALARM_ID)) intent.getIntExtra(EXTRA_ALARM_ID, -1).takeIf { it >= 0 } else null
        return WakeRequest(
            requestId = id,
            triggerEpochMs = intent.getLongExtra(EXTRA_TRIGGER, 0),
            kind = kind,
            alarmId = alarmId,
            title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
        )
    }

    fun fire(context: Context, request: WakeRequest): Intent =
        Intent(context, KlokkAlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            fill(request)
        }

    fun snooze(context: Context, request: WakeRequest): Intent =
        Intent(context, KlokkAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            fill(request)
        }

    fun dismiss(context: Context, request: WakeRequest): Intent =
        Intent(context, KlokkAlarmReceiver::class.java).apply {
            action = ACTION_DISMISS
            fill(request)
        }

    fun ring(context: Context, request: WakeRequest): Intent =
        Intent(context, KlokkRingActivity::class.java).apply {
            fill(request)
        }

    private fun Intent.fill(request: WakeRequest) {
        putExtra(EXTRA_ID, request.requestId)
        putExtra(EXTRA_KIND, request.kind.name)
        putExtra(EXTRA_TITLE, request.title)
        putExtra(EXTRA_TRIGGER, request.triggerEpochMs)
        request.alarmId?.let { putExtra(EXTRA_ALARM_ID, it) }
    }
}
