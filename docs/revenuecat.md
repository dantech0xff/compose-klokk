# Klokk Plus with RevenueCat

Billing stays off until a public SDK key is present. The app still opens, and the paywall says purchases are not available. Desktop always uses the in-app mock and ignores these keys. Do not commit a key.

## Dashboard

Create a RevenueCat project with an entitlement id `plus`.

Attach three packages to the current offering:

| Package id | Store product id |
|---|---|
| `$rc_annual` | `klokk_plus_yearly` |
| `$rc_monthly` | `klokk_plus_monthly` |
| `$rc_lifetime` | `klokk_plus_lifetime` |

Create those product ids in Play Console and App Store Connect. The yearly product is the one that can carry the 7 day trial. Prices in the paywall come from the store. The strings in the app are only the fallback when the offering has not loaded.

The Android public key starts with `goog_`. The iOS public key starts with `appl_`.

## Android

Set `REVENUECAT_ANDROID_KEY` in `~/.gradle/gradle.properties` or the environment, then rebuild. The value is copied into `BuildConfig` and is blank when unset.

## iOS

Set `REVENUECAT_IOS_KEY` in `iosApp/Configuration/Config.xcconfig` on the machine that builds, or override it in an untracked xcconfig. `Info.plist` reads it as `RevenueCatAPIKey`. Leave it empty in git.

This tree was not compiled for iOS. There is no macOS toolchain on the machine that added the wrapper.

## Sandbox

Play: license-test the account, install a build signed with the upload key, and buy the subscription from the paywall. Restore should set Plus again after a reinstall.

App Store: use a Sandbox Apple ID and a build that contains the iOS public key. Manage subscription opens the Apple subscriptions page. On Android it opens the Play subscriptions page for this application id.

A cancelled purchase leaves the paywall open and keeps the action that was waiting for Plus. A successful purchase runs that action.
