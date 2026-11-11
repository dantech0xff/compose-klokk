import SwiftUI
import UserNotifications
import ComposeApp

/// iOS cannot run code when a scheduled notification is delivered.
/// [KlokkAlarmCenter] therefore keeps the next eight occurrences of each
/// repeating alarm (under the 64 pending-notification cap). Snooze and
/// dismiss run only after the user taps an action, which launches the app.
/// The sound is the system default. A missing custom file would play silence.
/// AlarmKit on iOS 26 is not called. This project is not built against that
/// SDK here, so a direct AlarmKit reference would fail older Xcode.
@main
struct iOSApp: App {
    init() {
        KlokkAlarmCenter.shared.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

final class KlokkAlarmCenter: NSObject, UNUserNotificationCenterDelegate {
    static let shared = KlokkAlarmCenter()
    private var armed: [String] = []

    func start() {
        let center = UNUserNotificationCenter.current()
        center.delegate = self
        let snooze = UNNotificationAction(identifier: "SNOOZE", title: "Snooze", options: [])
        let dismiss = UNNotificationAction(identifier: "DISMISS", title: "Dismiss", options: [.destructive])
        let category = UNNotificationCategory(
            identifier: "KLOKK_RING",
            actions: [snooze, dismiss],
            intentIdentifiers: [],
            options: []
        )
        center.setNotificationCategories([category])
        NotificationCenter.default.addObserver(
            forName: Notification.Name("KlokkScheduleAlarms"),
            object: nil,
            queue: .main
        ) { _ in
            self.replace(json: IosScheduleBus.shared.payload)
        }
    }

    private func replace(json: String) {
        guard let data = json.data(using: .utf8),
              let rows = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] else {
            return
        }
        let center = UNUserNotificationCenter.current()
        center.requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
        center.removePendingNotificationRequests(withIdentifiers: armed)
        var next: [String] = []
        for row in rows {
            guard let id = row["id"] as? String,
                  let at = row["at"] as? NSNumber,
                  let title = row["title"] as? String else { continue }
            let content = UNMutableNotificationContent()
            content.title = title
            content.sound = .default
            content.categoryIdentifier = "KLOKK_RING"
            let date = Date(timeIntervalSince1970: at.doubleValue / 1000)
            let units: Set<Calendar.Component> = [.year, .month, .day, .hour, .minute, .second]
            let components = Calendar.current.dateComponents(units, from: date)
            let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
            let request = UNNotificationRequest(identifier: id, content: content, trigger: trigger)
            center.add(request)
            next.append(id)
        }
        armed = next
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .sound, .list]
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse
    ) async {
        let action: String
        switch response.actionIdentifier {
        case "SNOOZE": action = "SNOOZE"
        case "DISMISS", UNNotificationDefaultActionIdentifier: action = "DISMISS"
        default: return
        }
        IosAlarmActionsKt.onIosAlarmAction(requestId: response.notification.request.identifier, action: action)
    }
}