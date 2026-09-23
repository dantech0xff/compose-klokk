package com.theapache64.klokk.util

import androidx.compose.runtime.Composable
import com.theapache64.klokk.generated.resources.Res
import com.theapache64.klokk.generated.resources.city_day_offset
import com.theapache64.klokk.generated.resources.city_today
import com.theapache64.klokk.generated.resources.city_tomorrow
import com.theapache64.klokk.generated.resources.city_yesterday
import com.theapache64.klokk.generated.resources.date_line
import com.theapache64.klokk.generated.resources.date_lock
import com.theapache64.klokk.generated.resources.day_fri
import com.theapache64.klokk.generated.resources.day_mon
import com.theapache64.klokk.generated.resources.day_sat
import com.theapache64.klokk.generated.resources.day_sun
import com.theapache64.klokk.generated.resources.day_thu
import com.theapache64.klokk.generated.resources.day_tue
import com.theapache64.klokk.generated.resources.day_wed
import com.theapache64.klokk.generated.resources.days_every
import com.theapache64.klokk.generated.resources.days_once
import com.theapache64.klokk.generated.resources.days_weekdays
import com.theapache64.klokk.generated.resources.days_weekend
import com.theapache64.klokk.generated.resources.dow_chip_fri
import com.theapache64.klokk.generated.resources.dow_chip_mon
import com.theapache64.klokk.generated.resources.dow_chip_sat
import com.theapache64.klokk.generated.resources.dow_chip_sun
import com.theapache64.klokk.generated.resources.dow_chip_thu
import com.theapache64.klokk.generated.resources.dow_chip_tue
import com.theapache64.klokk.generated.resources.dow_chip_wed
import com.theapache64.klokk.generated.resources.dow_fri
import com.theapache64.klokk.generated.resources.dow_mon
import com.theapache64.klokk.generated.resources.dow_sat
import com.theapache64.klokk.generated.resources.dow_sun
import com.theapache64.klokk.generated.resources.dow_thu
import com.theapache64.klokk.generated.resources.dow_tue
import com.theapache64.klokk.generated.resources.dow_wed
import com.theapache64.klokk.generated.resources.dur_hours
import com.theapache64.klokk.generated.resources.dur_hours_minutes
import com.theapache64.klokk.generated.resources.dur_minutes
import com.theapache64.klokk.generated.resources.dur_seconds
import com.theapache64.klokk.generated.resources.month_1
import com.theapache64.klokk.generated.resources.month_10
import com.theapache64.klokk.generated.resources.month_11
import com.theapache64.klokk.generated.resources.month_12
import com.theapache64.klokk.generated.resources.month_2
import com.theapache64.klokk.generated.resources.month_3
import com.theapache64.klokk.generated.resources.month_4
import com.theapache64.klokk.generated.resources.month_5
import com.theapache64.klokk.generated.resources.month_6
import com.theapache64.klokk.generated.resources.month_7
import com.theapache64.klokk.generated.resources.month_8
import com.theapache64.klokk.generated.resources.month_9
import com.theapache64.klokk.generated.resources.paywall_lead_cities
import com.theapache64.klokk.generated.resources.paywall_lead_settings
import com.theapache64.klokk.generated.resources.paywall_lead_show
import com.theapache64.klokk.generated.resources.paywall_lead_widgets
import com.theapache64.klokk.generated.resources.plan_lifetime
import com.theapache64.klokk.generated.resources.plan_lifetime_note
import com.theapache64.klokk.generated.resources.plan_monthly
import com.theapache64.klokk.generated.resources.plan_monthly_note
import com.theapache64.klokk.generated.resources.plan_yearly
import com.theapache64.klokk.generated.resources.plan_yearly_note
import com.theapache64.klokk.generated.resources.price_lifetime
import com.theapache64.klokk.generated.resources.price_monthly
import com.theapache64.klokk.generated.resources.price_yearly
import com.theapache64.klokk.generated.resources.fineprint_lifetime
import com.theapache64.klokk.generated.resources.fineprint_monthly
import com.theapache64.klokk.generated.resources.fineprint_yearly
import com.theapache64.klokk.generated.resources.rel_days_hours
import com.theapache64.klokk.generated.resources.rel_hours
import com.theapache64.klokk.generated.resources.rel_hours_minutes
import com.theapache64.klokk.generated.resources.rel_minutes
import com.theapache64.klokk.generated.resources.saver_desc_ripple
import com.theapache64.klokk.generated.resources.saver_desc_shuffle
import com.theapache64.klokk.generated.resources.saver_desc_trance
import com.theapache64.klokk.generated.resources.saver_desc_wave
import com.theapache64.klokk.generated.resources.show_ripple
import com.theapache64.klokk.generated.resources.show_shuffle
import com.theapache64.klokk.generated.resources.show_trance
import com.theapache64.klokk.generated.resources.show_wave
import com.theapache64.klokk.generated.resources.tab_alarm
import com.theapache64.klokk.generated.resources.tab_clock
import com.theapache64.klokk.generated.resources.tab_focus
import com.theapache64.klokk.generated.resources.tab_settings
import com.theapache64.klokk.generated.resources.tab_timer
import com.theapache64.klokk.movement.SaverShow
import com.theapache64.klokk.state.KlokkTab
import com.theapache64.klokk.state.PaywallReason
import com.theapache64.klokk.state.PlusPlan
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/**
 * Localized strings for the UI. Everything here is @Composable so the
 * resolved values follow the platform locale; the pure logic stays in
 * [KlokkFormat], which takes the localized pieces as parameters.
 */

@Composable
fun tabLabel(tab: KlokkTab): String = stringResource(
    when (tab) {
        KlokkTab.CLOCK -> Res.string.tab_clock
        KlokkTab.ALARM -> Res.string.tab_alarm
        KlokkTab.TIMERS -> Res.string.tab_timer
        KlokkTab.FOCUS -> Res.string.tab_focus
        KlokkTab.SETTINGS -> Res.string.tab_settings
    },
)

/** Mon-first full day names, indexed by [DayOfWeek.ordinal]. */
@Composable
fun dayFullNames(): List<String> = listOf(
    stringResource(Res.string.day_mon),
    stringResource(Res.string.day_tue),
    stringResource(Res.string.day_wed),
    stringResource(Res.string.day_thu),
    stringResource(Res.string.day_fri),
    stringResource(Res.string.day_sat),
    stringResource(Res.string.day_sun),
)

/** Sun-first short day names, indexed by `Alarm.days` (0 = Sunday). */
@Composable
fun dayShortNames(): List<String> = listOf(
    stringResource(Res.string.dow_sun),
    stringResource(Res.string.dow_mon),
    stringResource(Res.string.dow_tue),
    stringResource(Res.string.dow_wed),
    stringResource(Res.string.dow_thu),
    stringResource(Res.string.dow_fri),
    stringResource(Res.string.dow_sat),
)

/** Sun-first single/short letters for the alarm day chips (0 = Sunday). */
@Composable
fun dayChipNames(): List<String> = listOf(
    stringResource(Res.string.dow_chip_sun),
    stringResource(Res.string.dow_chip_mon),
    stringResource(Res.string.dow_chip_tue),
    stringResource(Res.string.dow_chip_wed),
    stringResource(Res.string.dow_chip_thu),
    stringResource(Res.string.dow_chip_fri),
    stringResource(Res.string.dow_chip_sat),
)

/** Full month names indexed by [Month.ordinal]. */
@Composable
fun monthFullNames(): List<String> = listOf(
    stringResource(Res.string.month_1),
    stringResource(Res.string.month_2),
    stringResource(Res.string.month_3),
    stringResource(Res.string.month_4),
    stringResource(Res.string.month_5),
    stringResource(Res.string.month_6),
    stringResource(Res.string.month_7),
    stringResource(Res.string.month_8),
    stringResource(Res.string.month_9),
    stringResource(Res.string.month_10),
    stringResource(Res.string.month_11),
    stringResource(Res.string.month_12),
)

@Composable
fun saverShowName(show: SaverShow): String = stringResource(
    when (show) {
        SaverShow.SHUFFLE -> Res.string.show_shuffle
        SaverShow.RIPPLE -> Res.string.show_ripple
        SaverShow.TRANCE -> Res.string.show_trance
        SaverShow.WAVE -> Res.string.show_wave
    },
)

@Composable
fun saverDescription(show: SaverShow): String = stringResource(
    when (show) {
        SaverShow.SHUFFLE -> Res.string.saver_desc_shuffle
        SaverShow.RIPPLE -> Res.string.saver_desc_ripple
        SaverShow.TRANCE -> Res.string.saver_desc_trance
        SaverShow.WAVE -> Res.string.saver_desc_wave
    },
)

@Composable
fun planLabel(plan: PlusPlan): String = stringResource(
    when (plan) {
        PlusPlan.YEAR -> Res.string.plan_yearly
        PlusPlan.MONTH -> Res.string.plan_monthly
        PlusPlan.LIFE -> Res.string.plan_lifetime
    },
)

@Composable
fun planNote(plan: PlusPlan): String = stringResource(
    when (plan) {
        PlusPlan.YEAR -> Res.string.plan_yearly_note
        PlusPlan.MONTH -> Res.string.plan_monthly_note
        PlusPlan.LIFE -> Res.string.plan_lifetime_note
    },
)

@Composable
fun planPrice(plan: PlusPlan): String = stringResource(
    when (plan) {
        PlusPlan.YEAR -> Res.string.price_yearly
        PlusPlan.MONTH -> Res.string.price_monthly
        PlusPlan.LIFE -> Res.string.price_lifetime
    },
)

@Composable
fun planFineprint(plan: PlusPlan): String = stringResource(
    when (plan) {
        PlusPlan.YEAR -> Res.string.fineprint_yearly
        PlusPlan.MONTH -> Res.string.fineprint_monthly
        PlusPlan.LIFE -> Res.string.fineprint_lifetime
    },
)

@Composable
fun paywallLead(reason: PaywallReason): String = stringResource(
    when (reason) {
        PaywallReason.CITIES -> Res.string.paywall_lead_cities
        PaywallReason.SHOW -> Res.string.paywall_lead_show
        PaywallReason.WIDGETS -> Res.string.paywall_lead_widgets
        PaywallReason.SETTINGS -> Res.string.paywall_lead_settings
    },
)

// ---- localized formatting helpers (thin composable wrappers over KlokkFormat) ----

/** "Wednesday, 23 September" — word order comes from `date_line`. */
@Composable
fun dateLine(now: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): String =
    KlokkFormat.dateLine(now, tz, stringResource(Res.string.date_line), dayFullNames(), monthFullNames())

/** "Wednesday, September 23" — lock screen ordering via `date_lock`. */
@Composable
fun lockDate(now: Instant, tz: TimeZone = TimeZone.currentSystemDefault()): String =
    KlokkFormat.lockDate(now, tz, stringResource(Res.string.date_lock), dayFullNames(), monthFullNames())

/** "Every day" / "Once" / "Weekdays" / "Weekend" / "Mon, Wed" */
@Composable
fun daysText(days: List<Boolean>): String = KlokkFormat.daysText(
    days,
    every = stringResource(Res.string.days_every),
    once = stringResource(Res.string.days_once),
    weekdays = stringResource(Res.string.days_weekdays),
    weekend = stringResource(Res.string.days_weekend),
    shortDayNames = dayShortNames(),
)

/** "in 2d 3h" / "in 7h 20m" / "in 45m" */
@Composable
fun relTime(ms: Long): String = KlokkFormat.relTime(
    ms,
    daysHours = stringResource(Res.string.rel_days_hours),
    hoursMinutes = stringResource(Res.string.rel_hours_minutes),
    hours = stringResource(Res.string.rel_hours),
    minutes = stringResource(Res.string.rel_minutes),
)

/** "45 s" / "20 min" / "1 h 5 min" */
@Composable
fun fmtDur(ms: Long): String = KlokkFormat.fmtDur(
    ms,
    seconds = stringResource(Res.string.dur_seconds),
    minutes = stringResource(Res.string.dur_minutes),
    hours = stringResource(Res.string.dur_hours),
    hoursMinutes = stringResource(Res.string.dur_hours_minutes),
)

/** Local time in [tzId] plus a "Today, +7h" style sub. */
@Composable
fun cityInfo(tzId: String, now: Instant): KlokkFormat.CityInfo = KlokkFormat.cityInfo(
    tzId,
    now,
    today = stringResource(Res.string.city_today),
    tomorrow = stringResource(Res.string.city_tomorrow),
    yesterday = stringResource(Res.string.city_yesterday),
    dayOffset = stringResource(Res.string.city_day_offset),
)
