# A fork from Klokk of theapache64, customized by Dan Tech

# 🕒 klokk

> A kinetic wall clock app — iOS-style redesign, built with Compose Multiplatform.

A grid of tiny analog clocks animates in sync to form digits, ripples and
waves — now as a full clock app with five tabs.

## ✨ Features

- **Clock** — live hero grid, world clock cities, swipe-to-delete
- **Alarm** — scheduled alarms with repeat days, scrub-to-edit hero grid,
  snooze, ring overlay
- **Timer** — preset/scrub durations, countdown progress ring, ring on expiry
- **Focus** — focus sessions with an ambient countdown grid and a
  tap-to-reveal End control; sessions logged per day
- **Settings** — screensaver show picker (Shuffle / Ripple / Trance / Wave),
  widget previews, night dimming
- **Klokk Plus** — paywall gating for extra cities, screensavers and widgets
  (mock purchase flow)
- **Localization** — English and Vietnamese via Compose Resources

## 🔮 Demo

![](demo.webp)

## 🏃 Run

- **Desktop**: Clone the repo and run `./gradlew :composeApp:run`
- **Android**: `./gradlew :androidApp:installDebug` with an emulator/device connected (requires Android SDK)
- **iOS**: Open `iosApp/iosApp.xcodeproj` in Xcode on macOS and run (requires a macOS host — iOS targets are only enabled when building on macOS)

## 💡 Inspiration

- Nezih Yılmaz's kinetic countdown timer
- A million times humans since 1982

## ✍️ Author

👤 **theapache64** (original) — forked and customized by **Dan Tech**

* Twitter: <a href="https://twitter.com/theapache64" target="_blank">@theapache64</a>
* Email: theapache64@gmail.com

## 🤝 Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

1. Open an issue first to discuss what you would like to change.
1. Fork the Project
1. Create your feature branch (`git checkout -b feature/amazing-feature`)
1. Commit your changes (`git commit -m 'Add some amazing feature'`)
1. Push to the branch (`git push origin feature/amazing-feature`)
1. Open a pull request

Please make sure to update tests as appropriate.

## ❤ Show your support

Give a ⭐️ if this project helped you!

## ☑️ TODO

- [ ] Persist alarms, cities and Plus unlock across launches
- [ ] Real billing integration for Klokk Plus
- [ ] Dark Theme Support
- [ ] Tornado Movement
- [ ] Background Music
- [x] Add second movement to border clocks
- [ ] Add alphabets

## 📝 License

```
Copyright © 2021 - theapache64

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
