package dev.dantech.klokk.alarm

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.dantech.klokk.R
import dev.dantech.klokk.schedule.WakeKind
import dev.dantech.klokk.ui.KlokkRingScreen

class KlokkRingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        val request = AlarmIntents.request(intent)
        setContent {
            KlokkRingScreen(
                title = request?.title?.ifBlank { getString(R.string.alarm_channel) }
                    ?: getString(R.string.alarm_channel),
                allowSnooze = request?.kind != WakeKind.TIMER,
                snoozeLabel = getString(R.string.alarm_snooze),
                dismissLabel = getString(R.string.alarm_dismiss),
                onSnooze = {
                    request?.let { sendBroadcast(AlarmIntents.snooze(this@KlokkRingActivity, it)) }
                    finish()
                },
                onDismiss = {
                    request?.let { sendBroadcast(AlarmIntents.dismiss(this@KlokkRingActivity, it)) }
                    finish()
                },
            )
        }
    }
}
