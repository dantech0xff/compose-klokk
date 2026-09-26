import UIKit
import SwiftUI
import ComposeApp

/// Container VC that owns the status-bar appearance. The shared Compose code
/// posts "KlokkStatusBarStyleDark"/"KlokkStatusBarStyleLight" instead of the
/// removed UIApplication.setStatusBarStyle API.
final class KlokkRootViewController: UIViewController {

    private var statusBarStyle: UIStatusBarStyle = .lightContent {
        didSet { setNeedsStatusBarAppearanceUpdate() }
    }

    override var preferredStatusBarStyle: UIStatusBarStyle { statusBarStyle }

    override func viewDidLoad() {
        super.viewDidLoad()
        for (name, style) in [
            ("KlokkStatusBarStyleDark", UIStatusBarStyle.lightContent),
            ("KlokkStatusBarStyleLight", UIStatusBarStyle.darkContent),
        ] {
            NotificationCenter.default.addObserver(
                forName: Notification.Name(name),
                object: nil,
                queue: .main
            ) { [weak self] _ in
                self?.statusBarStyle = style
            }
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let container = KlokkRootViewController()
        let composeVC = MainViewControllerKt.MainViewController()
        container.addChild(composeVC)
        composeVC.view.frame = container.view.bounds
        composeVC.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        container.view.addSubview(composeVC.view)
        composeVC.didMove(toParent: container)
        return container
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.all) // Compose has own insets handler
    }
}
