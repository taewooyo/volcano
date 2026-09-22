// xcode: set sdk=iOS

import SwiftUI
import VolcanoIosApp

@main
struct VolcanoIosApp: App {
    var body: some Scene {
        WindowGroup {
            VolcanoComposeView()
                .ignoresSafeArea()
        }
    }
}

private struct VolcanoComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.mainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
