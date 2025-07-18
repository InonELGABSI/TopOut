import SwiftUI
import Shared
import Firebase

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        FirebaseApp.configure()
        return true
    }
}


@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init() {
        KoinKt.doInitKoin()
        Task { try? await (KoinKt.getKoin().get(objCClass: EnsureAnonymousUser.self) as! EnsureAnonymousUser).invoke() }

    }
    
    var body: some Scene {
        WindowGroup {
            TabView {
                
            }
        }
    }
}
