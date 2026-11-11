# Sign a Klokk release

The release build turns on R8 and resource shrinking. It stays unsigned until you supply a keystore. Do not commit the keystore or the passwords.

Put the values in `~/.gradle/gradle.properties`, in the repo's untracked `gradle.properties` only if that file stays off git, or in the environment.

| Name | Meaning |
|---|---|
| `KLOKK_KEYSTORE` | Absolute path to the `.jks` or `.keystore` file |
| `KLOKK_KEYSTORE_PASSWORD` | Store password |
| `KLOKK_KEY_ALIAS` | Key alias |
| `KLOKK_KEY_PASSWORD` | Key password |
| `KLOKK_VERSION_CODE` | Optional integer. Default is 2 |
| `KLOKK_VERSION_NAME` | Optional name. Default is 1.0 |

Environment variables use the same names and override nothing that Gradle properties already set. A missing keystore leaves the release APK unsigned so CI can still run `assembleRelease`.

```
./gradlew :androidApp:assembleRelease
```

iOS version strings stay in `iosApp/iosApp/Info.plist` (`CFBundleShortVersionString` 1.0, `CFBundleVersion` 1). The bundle id is `dev.dantech.klokk`.
