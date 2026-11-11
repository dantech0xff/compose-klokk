package dev.dantech.klokk.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import dev.dantech.klokk.R
import dev.dantech.klokk.movement.core.Movement
import dev.dantech.klokk.persist.defaultKlokkRepository
import dev.dantech.klokk.util.KlokkFormat
import java.text.DateFormat
import java.util.Date
import java.util.Calendar
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KlokkSmallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val now = Clock.System.now()
        val clock = Calendar.getInstance()
        val bitmap = analogBitmap(
            hourDeg = clock.get(Calendar.HOUR) * 30f + clock.get(Calendar.MINUTE) * 0.5f,
            minuteDeg = clock.get(Calendar.MINUTE) * 6f,
            size = 320,
        )
        val plus = defaultKlokkRepository().load().plus
        val shown = if (plus) bitmap else textBitmap(listOf(context.getString(R.string.widget_plus)), 320, 320)
        provideContent { Picture(shown, context.getString(R.string.widget_now)) }
    }
}

class KlokkMediumWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val matrix = Movement.Time(Clock.System.now()).getMatrixGenerator().getVerifiedMatrix()
        val bitmap = kineticBitmap(matrix, 600, 320)
        val plus = defaultKlokkRepository().load().plus
        val shown = if (plus) bitmap else textBitmap(listOf(context.getString(R.string.widget_plus)), 600, 320)
        provideContent { Picture(shown, context.getString(R.string.widget_time)) }
    }
}

class KlokkAlarmWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val now = Clock.System.now()
        val clock = Calendar.getInstance()
        val snapshot = defaultKlokkRepository().load()
        val next = KlokkFormat.nextAlarm(snapshot.alarms, now)
        val whenText = next?.let { KlokkFormat.fmtHM(it.alarm.h, it.alarm.m) }
            ?: context.getString(R.string.widget_no_alarm)
        val date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(now.toEpochMilliseconds()))
        val shown = if (snapshot.plus) {
            alarmCardBitmap(date, context.getString(R.string.widget_next_alarm_pill), whenText, clock.get(Calendar.HOUR) * 30f + clock.get(Calendar.MINUTE) * 0.5f, clock.get(Calendar.MINUTE) * 6f)
        } else {
            textBitmap(listOf(context.getString(R.string.widget_plus)), 640, 480)
        }
        provideContent { Picture(shown, context.getString(R.string.widget_next_alarm)) }
    }
}

class KlokkSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = KlokkSmallWidget()
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdates.schedule(context)
    }
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdates.considerStop(context)
    }
}

class KlokkMediumReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = KlokkMediumWidget()
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdates.schedule(context)
    }
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdates.considerStop(context)
    }
}

class KlokkAlarmReceiverWidget : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = KlokkAlarmWidget()
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdates.schedule(context)
    }
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdates.considerStop(context)
    }
}

@androidx.compose.runtime.Composable
private fun Picture(bitmap: android.graphics.Bitmap, description: String) {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(ImageProvider(bitmap), contentDescription = description)
    }
}

object WidgetRefresher {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun refresh(context: Context) {
        scope.launch {
            KlokkSmallWidget().updateAll(context)
            KlokkMediumWidget().updateAll(context)
            KlokkAlarmWidget().updateAll(context)
        }
    }
}
