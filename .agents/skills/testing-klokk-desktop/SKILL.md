---
name: testing-klokk-desktop
description: How to launch and E2E-test the Klokk Compose Multiplatform desktop app on this VM (launch quirks, window geometry, gesture thresholds, state reset).
---

# Testing the Klokk desktop app

## Launch (VM quirks — must follow exactly)
```
cd /Users/devin/repos/compose-klokk && unset DISPLAY && nohup ./gradlew :composeApp:run --no-daemon > /tmp/klokk_run.log 2>&1 &
```
- `unset DISPLAY` is REQUIRED even on macOS — the Gradle daemon env can leak a stale DISPLAY into the JVM and make AWT headless (window never appears).
- `:composeApp:desktopRun` fails ("No main class"); use `:composeApp:run`.
- First build ~30-60s. Check `pgrep -f "com.theapache64.klokk"` and tail the log for `:composeApp:run`.
- Kill/restart for fresh state (state is in-memory only): `pkill -f "com.theapache64.klokk"`. Fresh state = plus=false, 0 cities, default alarms, Clock tab — REQUIRED to test paywall gating (any purchase makes the rest of the session Plus).
- Timezone-dependent behavior (night dimming 22:00-06:00, next-alarm captions): launch with `TZ=<zone>` to fake another local time — the app reads `TimeZone.currentSystemDefault()` from the JVM which honors TZ.

## Window geometry (macOS VM, 1024x768 tool coords)
- Fixed-size 402x874dp window at top-left, non-resizable, title "Klokk". Appears ~x32..288, y50..610 (≈0.64 px/dp). Never maximize — it's fixed-size.
- Screenshot PNGs save at 1600x1200 (×1.5625 tool coords) — useful for pixel-level checks.
- Tab row y≈87: Clock x≈59, Alarm x≈99, Timer x≈148, Focus x≈195, Settings x≈250.
- Hero grid ≈ x45..277, y107..231 (center x≈161). Caption y≈242. Body y≈265..600.

## Gestures
- Hero scrub: only while an alarm editor is open (left half=hours mod24, right half=minutes mod60) or the timer is idle at full value (left=minutes mod100, right=seconds mod60). 1 step per 14dp ≈ **9px screen**. Drag UP = +, DOWN = − (mod wraps). A drag >4px also suppresses the tap→saver/paywall gate.
- City row: drag LEFT reveals Delete (settles at −88px if dragged past −44px).
- Ring overlay: release-based. dy>60dp swipe-up = stop; a plain tap on an ALARM ring = snooze +9min (jumps to Alarm tab); TIMER ring stops on ANY release. Releases <400ms after open are ignored (grace).
- Sheets cover the whole screen with an invisible tap-to-dismiss layer ABOVE the sheet — the tab row is unreachable while a sheet is open (tab taps just dismiss the sheet).
- Focus "End" button auto-hides 3s after reveal — click it in the SAME tool call as the reveal tap or the window lapses and the tap only re-reveals it (dead zone eats the click).

## Verification tricks
- Digits vs real time: compare hero digits with `date "+%H:%M"` (±1 min).
- Night dim: background-cell hands render at 28% alpha. Pixel-check: in a dimmed hero screenshot a pure-background strip (hero rows 0/7) tops out ~130 brightness vs 255 in non-dim grids (ring overlay, saver preview).
- Real alarm fire: New alarm → all day chips off ("Once") → scrub to now+2min → it rings at the minute and auto-disables.
- Snooze re-fire: tap a ring → snooze → it re-rings exactly +9min later (label falls back to current time).
- Paywall "Unlocking…" lasts only ~900ms — screenshot in the same tool call right after clicking the CTA.
- Mock purchase runs the pending gated action after unlock (e.g. city sheet re-opens automatically).

## Known quirks (from code + testing)
- "Resume" on a finished (00:00) timer instantly re-rings — tmEndTs=now.
- PaywallReason.WIDGETS has no UI trigger (Settings "Widgets" row is display-only).
- Saved-state persistence does NOT exist: every restart is a clean slate.
