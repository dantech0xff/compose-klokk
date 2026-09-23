package com.theapache64.klokk.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.theapache64.klokk.movement.SaverShow

enum class KlokkTab { CLOCK, ALARM, TIMERS, FOCUS, SETTINGS }

enum class KlokkSheet { CITIES, SAVER }

enum class KlokkOverlay { RING, SAVER }

enum class RingKind { ALARM, TIMER }

/** What the paywall was opened for; highlights the matching feature row. */
enum class PaywallReason { CITIES, SHOW, WIDGETS, SETTINGS }

enum class PlusPlan(val label: String, val note: String, val price: String, val fineprint: String) {
    YEAR(
        "Yearly", "7 days free · $1.00 a month", "$11.99",
        "7 days free, then $11.99 a year. Cancel anytime in Settings."
    ),
    MONTH(
        "Monthly", "Billed every month", "$1.99",
        "$1.99 a month. Cancel anytime in Settings."
    ),
    LIFE(
        "Lifetime", "Pay once", "$24.99",
        "One payment. Yours for good on every device you sign in to."
    ),
}

/** [days] is Sunday-first (index 0 = Sunday) like Date.getDay(). */
data class Alarm(
    val id: Int,
    val h: Int,
    val m: Int,
    val label: String,
    val days: List<Boolean>,
    val on: Boolean,
)

data class City(val name: String, val tz: String)

data class FocusSession(val startMs: Long, val ms: Long)

/**
 * All app state for the redesign. Timing-driven side effects
 * (ring flash, saver stepping, tick) live in LaunchedEffects in App.kt.
 */
class KlokkState {
    var tab by mutableStateOf(KlokkTab.CLOCK)
    var sheet by mutableStateOf<KlokkSheet?>(null)
    var overlay by mutableStateOf<KlokkOverlay?>(null)
    var paywall by mutableStateOf<PaywallReason?>(null)

    // ring
    var ringKind by mutableStateOf(RingKind.ALARM)
    var ringId by mutableStateOf<Int?>(null)
    var ringOn by mutableStateOf(true)
    var snoozeUntil by mutableStateOf(0L)
    var overlayTs = 0L

    // alarms
    val alarms = mutableStateListOf(
        Alarm(1, 7, 0, "", listOf(false, true, true, true, true, true, false), true),
        Alarm(2, 9, 30, "Slow morning", listOf(true, false, false, false, false, false, true), false),
    )
    var editingId by mutableStateOf<Int?>(null)
    var nextAlarmId = 3
    var rungAlarmId by mutableStateOf(-1)

    // cities
    val cities = mutableStateListOf<City>()
    var citySearch by mutableStateOf("")
    var swipeIndex by mutableStateOf(-1)

    // timer
    var tmTotal by mutableStateOf(300)
    var tmRemaining by mutableStateOf(300)
    var tmRunning by mutableStateOf(false)
    var tmEndTs by mutableStateOf(0L)

    // focus
    var focusRunning by mutableStateOf(false)
    var focusStartTs by mutableStateOf(0L)
    val focusSessions = mutableStateListOf<FocusSession>()
    var focusCtl by mutableStateOf(false)

    // saver / plus
    var saverShow by mutableStateOf(SaverShow.SHUFFLE)
    var saverStep by mutableStateOf(-1)
    var saverPreview by mutableStateOf(SaverShow.SHUFFLE)
    var previewTs by mutableStateOf(0L)
    var plus by mutableStateOf(false)
    var plan by mutableStateOf(PlusPlan.YEAR)
    var buying by mutableStateOf(false)
    var pending: (() -> Unit)? = null

    var nowMs by mutableStateOf(kotlin.time.Clock.System.now().toEpochMilliseconds())

    fun startRing(kind: RingKind, id: Int?, nowMs: Long) {
        if (overlay == KlokkOverlay.RING) return
        overlayTs = nowMs
        overlay = KlokkOverlay.RING
        ringKind = kind
        ringId = id
        ringOn = true
        sheet = null
        paywall = null
    }

    fun stopRing() {
        overlay = null
    }

    fun snooze(nowMs: Long) {
        overlay = null
        snoozeUntil = nowMs + 9 * 60_000
        tab = KlokkTab.ALARM
    }

    fun openSaver(nowMs: Long) {
        overlayTs = nowMs
        overlay = KlokkOverlay.SAVER
        saverStep = -1
    }

    fun isPlus() = plus

    /** Run [fn] immediately when Plus, else open the paywall and stash it. */
    fun gate(reason: PaywallReason, fn: () -> Unit) {
        if (isPlus()) {
            fn()
            return
        }
        pending = fn
        paywall = reason
        sheet = null
    }

    fun unlock() {
        if (buying) return
        buying = true
    }

    fun onUnlocked() {
        val fn = pending
        pending = null
        plus = true
        buying = false
        paywall = null
        fn?.invoke()
    }
}

/** The shared catalog of searchable cities (name, region, IANA zone). */
val CITIES: List<Triple<String, String, String>> = listOf(
    Triple("Amsterdam", "Netherlands", "Europe/Amsterdam"),
    Triple("Auckland", "New Zealand", "Pacific/Auckland"),
    Triple("Bangkok", "Thailand", "Asia/Bangkok"),
    Triple("Beijing", "China", "Asia/Shanghai"),
    Triple("Berlin", "Germany", "Europe/Berlin"),
    Triple("Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires"),
    Triple("Cairo", "Egypt", "Africa/Cairo"),
    Triple("Chicago", "United States", "America/Chicago"),
    Triple("Denver", "United States", "America/Denver"),
    Triple("Dubai", "United Arab Emirates", "Asia/Dubai"),
    Triple("Hanoi", "Vietnam", "Asia/Ho_Chi_Minh"),
    Triple("Ho Chi Minh City", "Vietnam", "Asia/Ho_Chi_Minh"),
    Triple("Hong Kong", "China", "Asia/Hong_Kong"),
    Triple("Honolulu", "United States", "Pacific/Honolulu"),
    Triple("Istanbul", "Türkiye", "Europe/Istanbul"),
    Triple("Jakarta", "Indonesia", "Asia/Jakarta"),
    Triple("Johannesburg", "South Africa", "Africa/Johannesburg"),
    Triple("Kuala Lumpur", "Malaysia", "Asia/Kuala_Lumpur"),
    Triple("Lagos", "Nigeria", "Africa/Lagos"),
    Triple("Lisbon", "Portugal", "Europe/Lisbon"),
    Triple("London", "United Kingdom", "Europe/London"),
    Triple("Los Angeles", "United States", "America/Los_Angeles"),
    Triple("Madrid", "Spain", "Europe/Madrid"),
    Triple("Manila", "Philippines", "Asia/Manila"),
    Triple("Mexico City", "Mexico", "America/Mexico_City"),
    Triple("Moscow", "Russia", "Europe/Moscow"),
    Triple("Mumbai", "India", "Asia/Kolkata"),
    Triple("Nairobi", "Kenya", "Africa/Nairobi"),
    Triple("New York", "United States", "America/New_York"),
    Triple("Paris", "France", "Europe/Paris"),
    Triple("Riyadh", "Saudi Arabia", "Asia/Riyadh"),
    Triple("Rome", "Italy", "Europe/Rome"),
    Triple("San Francisco", "United States", "America/Los_Angeles"),
    Triple("São Paulo", "Brazil", "America/Sao_Paulo"),
    Triple("Seoul", "South Korea", "Asia/Seoul"),
    Triple("Shanghai", "China", "Asia/Shanghai"),
    Triple("Singapore", "Singapore", "Asia/Singapore"),
    Triple("Stockholm", "Sweden", "Europe/Stockholm"),
    Triple("Sydney", "Australia", "Australia/Sydney"),
    Triple("Taipei", "Taiwan", "Asia/Taipei"),
    Triple("Tokyo", "Japan", "Asia/Tokyo"),
    Triple("Toronto", "Canada", "America/Toronto"),
    Triple("Vancouver", "Canada", "America/Vancouver"),
    Triple("Zurich", "Switzerland", "Europe/Zurich"),
)
