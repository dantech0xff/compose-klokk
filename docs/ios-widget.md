# iOS home-screen widget

The Android widgets are in the app. The iOS WidgetKit extension is not, because this machine has no Xcode and a new extension target cannot be compiled or added to `iosApp.xcodeproj` safely from here.

What remains, on a Mac:

1. In Xcode, add a Widget Extension target to `iosApp` named `KlokkWidget`. Set its bundle id to `dev.dantech.klokk.widget`.
2. The extension should be a timeline provider, not a live view. Reload the timeline at the next minute. Do not schedule a per-second refresh.
3. Match the Settings previews. A small widget is one analog face for the current time. A medium widget is the kinetic digit grid for the current minute. A lock-screen widget shows the date, a large face, and the next alarm time.
4. Read Plus from the shared app group before drawing. If Plus is off, show the words Klokk Plus and do not draw the clock. The app group id can be `group.dev.dantech.klokk`. The main app has to write the Plus flag and the next alarm into that group when they change. That write is not in the app yet, because there is no extension to read it.
5. Add the App Group capability to both the app target and the widget target in the Apple developer portal.

Until those steps exist, iOS has no home-screen widget.
