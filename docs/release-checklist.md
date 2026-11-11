# What Dan still has to do

The code can build a signed release and talk to the stores. These steps are outside the repo. Do not commit a keystore, a RevenueCat key, or a store password.

## Accounts and the listing

- Create the Play Console app and the App Store Connect app for `dev.dantech.klokk`. The old id `com.theapache64.klokk` will not update in place.
- Set the app name to Klokk.
- Publish a privacy policy and put its URL in both store listings and, on iOS, in the app privacy answers.
- Capture store screenshots on a phone. The emulator on the build machine could not start, because this user cannot use `/dev/kvm`.

## Signing

- Create an upload keystore and set `KLOKK_KEYSTORE`, `KLOKK_KEYSTORE_PASSWORD`, `KLOKK_KEY_ALIAS`, and `KLOKK_KEY_PASSWORD` as described in `docs/signing.md`.
- Enroll in Play App Signing. The first uploaded AAB is signed with that upload key.
- Create the Apple distribution certificate and provisioning profile for `dev.dantech.klokk`.

## Versions

- Android `versionCode` defaults to 2 and `versionName` to 1.0. Override them with `KLOKK_VERSION_CODE` and `KLOKK_VERSION_NAME` for each upload.
- iOS `CFBundleShortVersionString` is 1.0 and `CFBundleVersion` is 1 in `iosApp/iosApp/Info.plist`. Bump those in Xcode for each upload.

## Klokk Plus

- Follow `docs/revenuecat.md`. Create the RevenueCat project, the `plus` entitlement, and the packages `$rc_annual`, `$rc_monthly`, and `$rc_lifetime`.
- Create the store products `klokk_plus_yearly`, `klokk_plus_monthly`, and `klokk_plus_lifetime`.
- Put `REVENUECAT_ANDROID_KEY` and `REVENUECAT_IOS_KEY` only on the build machines.
- Sandbox-test a purchase, a restore, a cancel, and a pending payment on each store. This repo was not pointed at a live RevenueCat project.

## Alarms

- In Play Console, declare that exact alarms are the core feature of the app. The manifest uses `USE_EXACT_ALARM` and `SCHEDULE_EXACT_ALARM`.
- On a device, confirm an alarm rings with the app swiped away, after reboot, and after a timezone change. Snooze is 9 minutes.
- iOS notifications were not compiled here. On a Mac, confirm permission, a repeating alarm, snooze, and dismiss. The sound is the system default. AlarmKit on iOS 26 is not wired. See the note in `iosApp/iosApp/iOSApp.swift`.

## Widgets

- Android has three home-screen widgets, gated by Plus. They redraw at the next minute while one is installed, and they stop when the last one is removed.
- The iOS WidgetKit extension is not in the Xcode project. `docs/ios-widget.md` lists the remaining steps.

## Build check

```
./gradlew :composeApp:desktopTest :androidApp:assembleDebug :androidApp:lint :androidApp:assembleRelease
```

`assembleRelease` is unsigned until the keystore variables are set.
