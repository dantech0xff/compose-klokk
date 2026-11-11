package com.theapache64.klokk.schedule

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSNotificationCenter

/**
 * Swift owns UserNotifications. Kotlin publishes the plan, and Swift reads
 * [payload] when it sees KlokkScheduleAlarms. A delivered notification does
 * not run this process, so [alarmScheduleHorizon] arms more than the next fire.
 *
 * AlarmKit (iOS 26) is not called. This tree is not compiled against the iOS 26
 * SDK here, and a wrong AlarmKit call would break the Xcode build. The Swift
 * side documents what remains.
 */
object IosScheduleBus {
    var payload: String = "[]"
}

private val busJson = Json { encodeDefaults = true }

@Serializable
private data class IosWake(
    val id: String,
    val at: Long,
    val kind: String,
    val title: String,
)

actual fun defaultAlarmScheduler(): AlarmScheduler = IosAlarmScheduler

actual fun alarmScheduleHorizon(): Int = 8

private object IosAlarmScheduler : AlarmScheduler {
    override fun replaceAll(requests: List<WakeRequest>) {
        val encoded = requests.map {
            IosWake(it.requestId, it.triggerEpochMs, it.kind.name, it.title)
        }
        IosScheduleBus.payload = busJson.encodeToString(encoded)
        NSNotificationCenter.defaultCenter.postNotificationName("KlokkScheduleAlarms", null)
    }
}
